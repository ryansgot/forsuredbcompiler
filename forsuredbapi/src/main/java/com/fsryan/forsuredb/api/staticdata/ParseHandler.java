/*
   forsuredbcompiler, an annotation processor and code generator for the forsuredb project

   Copyright 2015 Ryan Scott

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.fsryan.forsuredb.api.staticdata;

import com.fsryan.forsuredb.api.RecordContainer;
import com.fsryan.forsuredb.api.TypedRecordContainer;
import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.MigrationSet;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * <p>Handler for {@link SAXParser SAXParser} that is capable of handling static data XML
 */
class ParseHandler extends DefaultHandler {

    private static Pattern hexPattern = Pattern.compile("([0-9a-fA-f]{2})+");   // <-- hexadecimal digits must come in pairs

    private final DateFormat dateFormat;
    private final String tableName;
    private final List<MigrationSet> migrationSets;
    private final OnRecordRetrievedListener recordListener;
    private final Stack<String> tagStack = new Stack<>();
    private int lowestVersionToHonor;

    private MigrationSet currentMigrationSet;
    private final Map<Integer, List<RecordContainer>> recordQueue = new HashMap<>();

    /*package*/ ParseHandler(DateFormat dateFormat,
                             String tableName,
                             List<MigrationSet> migrationSets,
                             OnRecordRetrievedListener recordListener) {
        this.dateFormat = dateFormat;
        this.tableName = tableName;
        this.migrationSets = migrationSets;
        this.recordListener = recordListener;
        lowestVersionToHonor = migrationSets.get(0).dbVersion();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        switch (qName) {
            case "records":
                setCurrentMigrationSet(Integer.parseInt(attributes.getValue("db_version")));
                break;
            case "record":
                if (currentMigrationSet != null) {
                    // TODO: work on logic to update early rather than pushing each record to the queue
                    pushToQueue(currentMigrationSet.dbVersion(), createRecord(attributes));
                }
                break;
            default:
        }
        tagStack.push(qName);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        String ended = tagStack.pop();
        if ("records".equals(ended)) {
            currentMigrationSet = null;
        }
    }

    @Override
    public void endDocument() {
        recordListener.onRecord(recordQueue);
    }

    private void pushToQueue(int version, RecordContainer recordContainer) {
        List<RecordContainer> records = recordQueue.get(version);
        if (records == null) {
            records = new LinkedList<>();
            recordQueue.put(version, records);
        }
        records.add(recordContainer);
    }

    private RecordContainer createRecord(Attributes attributes) {
        RecordContainer ret = new TypedRecordContainer();
        for (int idx = 0; idx < attributes.getLength(); idx++) {
            String column = attributes.getQName(idx);
            String value = attributes.getValue(idx);

            TableInfo table = currentMigrationSet.targetSchema().get(tableName);
            if (!table.hasColumn(column)) {
                Map<String, ColumnInfo> columnMap = table.columnMap();
                List<String> columns = new ArrayList<>(columnMap.keySet());
                Collections.sort(columns);
                throw new IllegalStateException("Table '" + tableName + "' does not have column '" + column + "'; db_version: " + currentMigrationSet.dbVersion() + "; columns: " + columns);
            }
            String qualifiedTypeString = table.getColumn(column).qualifiedType();
            if (qualifiedTypeString == null) {
                throw new IllegalStateException("Column '" + column + "' exists without a qualified type; db_version: " + currentMigrationSet.dbVersion() + "; table: " + tableName);
            }

            switch (qualifiedTypeString) {
                case "java.lang.String":
                    ret.put(column, value);
                    break;
                case "boolean":
                case "java.lang.Boolean":
                    ret.put(column, Boolean.parseBoolean(value) ? 1 : 0);
                    break;
                case "int":
                case "java.lang.Integer":
                    ret.put(column, Integer.parseInt(value));
                    break;
                case "long":
                case "java.lang.Long":
                    ret.put(column, Long.parseLong(value));
                    break;
                case "float":
                case "java.lang.Float":
                    ret.put(column, Float.parseFloat(value));
                    break;
                case "double":
                case "java.lang.Double":
                    ret.put(column, Double.parseDouble(value));
                    break;
                case "java.util.Date":
                    try {
                        dateFormat.parse(value);
                    } catch (ParseException pe) {
                        throw new IllegalStateException("could not parse date '" + value + "'; db_version: " + currentMigrationSet.dbVersion() + "; table: " + tableName + "; column: " + column, pe);
                    }
                    ret.put(column, value);
                    break;
                case "java.math.BigDecimal":
                    ret.put(column, new BigDecimal(value).toPlainString());
                    break;
                case "BigInteger":
                case "java.math.BigInteger":
                    ret.put(column, new BigInteger(value).toString(10));
                    break;
                case "byte[]":
                case "Byte[]":
                    ret.put(column, hexStringToByteArray(value));
                    break;
                default:
                    throw new IllegalStateException("Unsupported type: '" + qualifiedTypeString + "; table: " + tableName + "; column '" + column + "'");
            }
        }
        return ret;
    }

    private void setCurrentMigrationSet(int dbVersion) {
        if (dbVersion < lowestVersionToHonor) {
            return;
        }

        for (MigrationSet migrationSet : migrationSets) {
            if (migrationSet.dbVersion() == dbVersion) {
                currentMigrationSet = migrationSet;
                return;
            }
        }
        throw new IllegalArgumentException("Schema for DB version " + dbVersion + " not found");
    }

    private static byte[] hexStringToByteArray(String s) {
        if (s.isEmpty() || !hexPattern.matcher(s).matches()) {
            throw new IllegalArgumentException("byte arrays must be represented in static data as hexadecimal strings; your entry: " + s);
        }

        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
