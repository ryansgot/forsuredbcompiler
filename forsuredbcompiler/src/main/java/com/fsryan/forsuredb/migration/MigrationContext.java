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
package com.fsryan.forsuredb.migration;

import com.fsryan.forsuredb.api.info.ColumnInfo;
import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.api.info.TableInfo;
import com.fsryan.forsuredb.annotationprocessor.util.APLog;
import com.fsryan.forsuredb.api.migration.Migration;
import com.fsryan.forsuredb.api.migration.MigrationRetriever;
import com.fsryan.forsuredb.api.migration.MigrationSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MigrationContext implements TableContext {

    private static final String LOG_TAG = MigrationContext.class.getSimpleName();

    private final MigrationRetriever mr;
    private Map<String, TableInfo> tableMap;

    public MigrationContext(MigrationRetriever mr) {
        this.mr = mr;
    }

    @Override
    public boolean hasTable(String tableName) {
        createTableMapIfNull();
        return tableMap.containsKey(tableName);
    }

    @Override
    public TableInfo getTable(String tableName) {
        createTableMapIfNull();
        return tableMap.get(tableName);
    }

    @Override
    public Collection<TableInfo> allTables() {
        createTableMapIfNull();
        return tableMap.values();
    }

    @Override
    public Map<String, TableInfo> tableMap() {
        createTableMapIfNull();
        return tableMap;
    }

    private Map<String, TableInfo> createTables() {
        Map<String, TableInfo.Builder> tableBuilderMap = new HashMap<>();
        Map<String, ColumnInfo.Builder> columnBuilderMap = new HashMap<>();
        for (MigrationSet migrationSet : mr.getMigrationSets()) {
            for (Migration m : migrationSet.getOrderedMigrations()) {
                update(migrationSet.getTargetSchema().get(m.getTableName()), m, tableBuilderMap, columnBuilderMap);
            }
        }

        for (Map.Entry<String, Map<String, ColumnInfo>> entry : createTableBuilderKeyToColumnMapMap(columnBuilderMap).entrySet()) {
            TableInfo.Builder tb = tableBuilderMap.get(entry.getKey());
            tb.columnMap(entry.getValue());
        }

        Map<String, TableInfo> retMap = new HashMap<>();
        for (Map.Entry<String, TableInfo.Builder> entry : tableBuilderMap.entrySet()) {
            retMap.put(entry.getKey(), entry.getValue().build());
        }

        APLog.i(LOG_TAG, "created tables: " + retMap.toString());
        return retMap;
    }

    private Map<String, Map<String, ColumnInfo>> createTableBuilderKeyToColumnMapMap(Map<String, ColumnInfo.Builder> columnBuilderMap) {
        Map<String, Map<String, ColumnInfo>> retMap = new HashMap<>();
        for (Map.Entry<String, ColumnInfo.Builder> entry : columnBuilderMap.entrySet()) {
            String tableBuilderKey = tableKeyFromColumnKey(entry.getKey());
            Map<String, ColumnInfo> columnMap = retMap.get(tableBuilderKey);
            if (columnMap == null) {
                columnMap = new HashMap<>();
                retMap.put(tableBuilderKey, columnMap);
            }
            ColumnInfo column = entry.getValue().build();
            columnMap.put(column.getColumnName(), column);
        }
        return retMap;
    }

    private void update(TableInfo table, Migration m, Map<String, TableInfo.Builder> tableBuilderMap, Map<String, ColumnInfo.Builder> columnBuilderMap) {
        switch (m.getType()) {
            case CREATE_TABLE:
                handleCreateTable(m, tableBuilderMap, columnBuilderMap);
                break;
            case ADD_FOREIGN_KEY_REFERENCE:
                handleAddForeignKeyReference(table, m, columnBuilderMap);
                break;
            case ALTER_TABLE_ADD_COLUMN:
                handleAddColumn(table, m, columnBuilderMap);
                break;
            case ALTER_TABLE_ADD_UNIQUE:
                handleAddUniqueColumn(table, m, columnBuilderMap);
        }
    }

    private void handleAddForeignKeyReference(TableInfo table, Migration m, Map<String, ColumnInfo.Builder> columnBuilderMap) {
        ColumnInfo column = table.getColumn(m.getColumnName());
        columnBuilderMap.put(columnKey(m), ColumnInfo.builder().columnName(m.getColumnName())
                .qualifiedType(column.getQualifiedType())
                .foreignKeyInfo(column.getForeignKeyInfo()));
    }

    private void handleAddUniqueColumn(TableInfo table, Migration m, Map<String, ColumnInfo.Builder> columnBuilderMap) {
        handleAddColumn(table, m, true, columnBuilderMap);
    }

    private void handleAddColumn(TableInfo table, Migration m, Map<String, ColumnInfo.Builder> columnBuilderMap) {
        handleAddColumn(table, m, false, columnBuilderMap);
    }

    private void handleAddColumn(TableInfo table, Migration m, boolean unique, Map<String, ColumnInfo.Builder> columnBuilderMap) {
        ColumnInfo.Builder b = columnBuilderMap.get(columnKey(m));
        if (b == null) {
            b = ColumnInfo.builder().columnName(m.getColumnName())
                    .qualifiedType(table.getColumn(m.getColumnName()).getQualifiedType())
                    .unique(unique);
            columnBuilderMap.put(columnKey(m), b);
        }
    }

    private void handleCreateTable(Migration m, Map<String, TableInfo.Builder> tableBuilderMap, Map<String, ColumnInfo.Builder> columnBuilderMap) {
        TableInfo.Builder tb = tableBuilderMap.get(tableKey(m));
        if (tb == null) {
            tb = TableInfo.builder().tableName(m.getTableName());
            tableBuilderMap.put(tableKey(m), tb);
        }
    }

    private String tableKey(Migration m) {
        return m.getTableName();
    }

    private String columnKey(Migration m) {
        return tableKey(m) + "." + m.getColumnName();
    }

    private String tableKeyFromColumnKey(String columnBuilderMapKey) {
        return columnBuilderMapKey.split("\\.")[0];
    }

    private void createTableMapIfNull() {
        if (tableMap == null) {
            tableMap = createTables();
        }
    }
}
