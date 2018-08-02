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
package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.annotations.ForeignKey;
import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.ForeignKeyInfo;
import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.info.TableInfo;

import java.io.*;
import java.util.*;

public class TestData {

    public static final String TEST_RES = "src/test/resources";
    public static final ColumnInfo[] DEFAULT_COLUMNS = new ColumnInfo[] {
            idCol(),
            createdCol(),
            deletedCol(),
            modifiedCol()
    };

    // Convenience constants
    public static final String TABLE_NAME = "test_table";
    public static final String TABLE_CLASS_NAME = "com.fsryan.test.TestTable";

    public static String resourceText(String resourceName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(TEST_RES + File.separator + resourceName));
        String line;
        StringBuilder out = new StringBuilder();
        while (null != (line = br.readLine())) {
            out.append(line).append("\n");
        }
        br.close();
        return out.toString();
    }

    public static InputStream resourceStream(String resource) {
        return TestData.class.getClassLoader().getResourceAsStream(resource);
    }

    // Convenience methods for making data to go into the tests
    public static TableInfo.BuilderCompat table() {
        return TableInfo.builder().tableName(TABLE_NAME)
                .qualifiedClassName(TABLE_CLASS_NAME);
    }

    public static TableInfo.BuilderCompat table(String name) {
        return table().tableName(name);
    }

    public static Map<String, ColumnInfo> columnMapOf(ColumnInfo... columns) {
        Map<String, ColumnInfo> retMap = new HashMap<>();
        for (ColumnInfo column : columns) {
            retMap.put(column.getColumnName(), column);
        }
        return retMap;
    }

    public static Map<String, TableInfo> tableMapOf(TableInfo... tables) {
        Map<String, TableInfo> retMap = new HashMap<>();
        for (TableInfo table : tables) {
            retMap.put(table.tableName(), table);
        }
        return retMap;
    }

    public static <K, V> Map<K, V> mapOf() {
        return new HashMap<>();
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1) {
        Map<K, V> ret = mapOf();
        ret.put(k1, v1);
        return ret;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        Map<K, V> ret = mapOf(k1, v1);
        ret.put(k2, v2);
        return ret;
    }

    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        Map<K, V> ret = mapOf(k1, v1, k2, v2);
        ret.put(k3, v3);
        return ret;
    }

    public static TableForeignKeyInfo.Builder foreignKeyTo(String foreignTableName) {
        return TableForeignKeyInfo.builder()
                .foreignTableName(foreignTableName)
                .deleteChangeAction("CASCADE")
                .updateChangeAction("CASCADE")
                .localToForeignColumnMap(mapOf("local", "foreign"))
                .foreignTableApiClassName(TestData.class.getName());
    }

    public static ColumnInfo idCol() {
        return ColumnInfo.builder().columnName("_id")
                .methodName("id")
                .qualifiedType("long")
                .primaryKey(true)
                .build();
    }

    public static ColumnInfo createdCol() {
        return ColumnInfo.builder().columnName("created")
                .methodName("created")
                .qualifiedType("java.util.Date")
                .defaultValue("CURRENT_TIMESTAMP")
                .build();
    }

    public static ColumnInfo deletedCol() {
        return ColumnInfo.builder().columnName("deleted")
                .methodName("deleted")
                .qualifiedType("boolean")
                .defaultValue("0")
                .build();
    }

    public static ColumnInfo modifiedCol() {
        return ColumnInfo.builder().columnName("modified")
                .methodName("modified")
                .qualifiedType("java.util.Date")
                .defaultValue("CURRENT_TIMESTAMP")
                .build();
    }

    public static ColumnInfo.Builder stringCol() {
        return columnFrom("java.lang.String");
    }

    public static ColumnInfo.Builder intCol() {
        return columnFrom("int");
    }

    public static ColumnInfo.Builder longCol() {
        return columnFrom("long");
    }

    public static ColumnInfo.Builder doubleCol() {
        return columnFrom("double");
    }

    public static ColumnInfo.Builder booleanCol() {
        return columnFrom("boolean");
    }

    public static ColumnInfo.Builder bigDecimalCol() {
        return columnFrom("java.math.BigDecimal");
    }

    public static ColumnInfo.Builder dateCol() {
        return columnFrom("java.util.Date");
    }

    public static ForeignKeyInfo.Builder cascadeFKI(String foreignKeyTableName) {
        return ForeignKeyInfo.builder().updateAction(ForeignKey.ChangeAction.CASCADE.name())
                .deleteAction(ForeignKey.ChangeAction.CASCADE.name())
                .columnName("_id")
                .tableName(foreignKeyTableName);
    }

    public static ForeignKeyInfo.Builder noActionFKI(String foreignKeyTableName) {
        return ForeignKeyInfo.builder().updateAction(ForeignKey.ChangeAction.NO_ACTION.name())
                .deleteAction(ForeignKey.ChangeAction.NO_ACTION.name())
                .columnName("_id")
                .tableName(foreignKeyTableName);
    }

    public static ForeignKeyInfo.Builder setNullFKI(String foreignKeyTableName) {
        return ForeignKeyInfo.builder().updateAction(ForeignKey.ChangeAction.SET_NULL.name())
                .deleteAction(ForeignKey.ChangeAction.SET_NULL.name())
                .columnName("_id")
                .tableName(foreignKeyTableName);
    }

    public static ForeignKeyInfo.Builder setDefaultFKI(String foreignKeyTableName) {
        return ForeignKeyInfo.builder().updateAction(ForeignKey.ChangeAction.SET_DEFAULT.name())
                .deleteAction(ForeignKey.ChangeAction.SET_DEFAULT.name())
                .columnName("_id")
                .tableName(foreignKeyTableName);
    }

    public static ForeignKeyInfo.Builder restrictFKI(String foreignKeyTableName) {
        return ForeignKeyInfo.builder().updateAction(ForeignKey.ChangeAction.RESTRICT.name())
                .deleteAction(ForeignKey.ChangeAction.RESTRICT.name())
                .columnName("_id")
                .tableName(foreignKeyTableName);
    }

    public static <T> Set<T> setOf(T... ts) {
        return new HashSet<>(Arrays.asList(ts));
    }

    // Helpers for covenience methods

    private static ColumnInfo.Builder columnFrom(String qualifiedType) {
        return ColumnInfo.builder().columnName(nameFrom(qualifiedType)).qualifiedType(qualifiedType);
    }

    private static String nameFrom(String qualifiedType) {
        if (qualifiedType == null || qualifiedType.isEmpty()) {
            return "";
        }

        final String suffix = "_column";
        switch (qualifiedType) {
            case "java.lang.String":
                return "string" + suffix;
            case "java.math.BigDecimal":
                return "big_decimal" + suffix;
            case "java.util.Date":
                return "date" + suffix;
            case "byte[]":
                return "byte_array" + suffix;
        }

        return qualifiedType + suffix;
    }
}
