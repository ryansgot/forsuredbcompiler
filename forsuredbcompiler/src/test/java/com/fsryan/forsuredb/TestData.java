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
package com.fsryan.forsuredb;

import com.fsryan.forsuredb.annotations.ForeignKey;
import com.fsryan.forsuredb.api.info.ColumnInfo;
import com.fsryan.forsuredb.api.info.ForeignKeyInfo;
import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.api.info.TableInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TestData {

    public static final String TEST_RES = "src/test/resources";
    public static final ColumnInfo[] DEFAULT_COLUMNS = new ColumnInfo[] {
            TestData.idCol(),
            TestData.createdCol(),
            TestData.deletedCol(),
            TestData.modifiedCol()
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

    // Convenience methods for making data to go into the tests
    public static TableInfo.Builder table() {
        return TableInfo.builder().tableName(TABLE_NAME)
                .qualifiedClassName(TABLE_CLASS_NAME);
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
            retMap.put(table.getTableName(), table);
        }
        return retMap;
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
        return ForeignKeyInfo.builder().updateAction(ForeignKey.ChangeAction.CASCADE)
                .deleteAction(ForeignKey.ChangeAction.CASCADE)
                .columnName("_id")
                .tableName(foreignKeyTableName);
    }

    public static ForeignKeyInfo.Builder noActionFKI(String foreignKeyTableName) {
        return ForeignKeyInfo.builder().updateAction(ForeignKey.ChangeAction.NO_ACTION)
                .deleteAction(ForeignKey.ChangeAction.NO_ACTION)
                .columnName("_id")
                .tableName(foreignKeyTableName);
    }

    public static ForeignKeyInfo.Builder setNullFKI(String foreignKeyTableName) {
        return ForeignKeyInfo.builder().updateAction(ForeignKey.ChangeAction.SET_NULL)
                .deleteAction(ForeignKey.ChangeAction.SET_NULL)
                .columnName("_id")
                .tableName(foreignKeyTableName);
    }

    public static ForeignKeyInfo.Builder setDefaultFKI(String foreignKeyTableName) {
        return ForeignKeyInfo.builder().updateAction(ForeignKey.ChangeAction.SET_DEFAULT)
                .deleteAction(ForeignKey.ChangeAction.SET_DEFAULT)
                .columnName("_id")
                .tableName(foreignKeyTableName);
    }

    public static ForeignKeyInfo.Builder restrictFKI(String foreignKeyTableName) {
        return ForeignKeyInfo.builder().updateAction(ForeignKey.ChangeAction.RESTRICT)
                .deleteAction(ForeignKey.ChangeAction.RESTRICT)
                .columnName("_id")
                .tableName(foreignKeyTableName);
    }

    public static TableContextBuilder newTableContext() {
        return new TableContextBuilder();
    }

    // Helpers for covenience methods

    private static ColumnInfo.Builder columnFrom(String qualifiedType) {
        return ColumnInfo.builder()
                .columnName(nameFrom(qualifiedType))
                .qualifiedType(qualifiedType)
                .orderable(true)
                .searchable(true);
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

    public static TableInfo targetTableWithChildForeignKey() {
        return testTargetContext().getTable("test_table_3");
    }

    public static TableInfo targetTableWithParentAndChildForeignKey() {
        return testTargetContext().getTable("test_table_2");
    }

    public static TableContext testTargetContext() {
        return newTableContext().addTable(table().qualifiedClassName("com.fsryan.annotationprocessor.generator.code.TestTable")
                        .columnMap(columnMapOf(idCol(),
                                modifiedCol(),
                                createdCol(),
                                deletedCol(),
                                longCol().columnName("test_table_2_id")
                                        .methodName("testTable2Id")
                                        .foreignKeyInfo(cascadeFKI("test_table_2")
                                                .columnName("_id")
                                                .apiClassName("com.fsryan.annotationprocessor.generator.code.TestTable2")
                                                .build())
                                        .build()))
                        .tableName("test_table")
                        .staticDataAsset("test_table_data.xml")
                        .staticDataRecordName("test_table_data")
                        .build())
                .addTable(table().qualifiedClassName("com.fsryan.annotationprocessor.generator.code.TestTable2")
                                .columnMap(columnMapOf(idCol(),
                                        modifiedCol(),
                                        createdCol(),
                                        deletedCol(),
                                        longCol().columnName("test_table_3_id")
                                                .methodName("testTable3Id")
                                                .foreignKeyInfo(cascadeFKI("test_table_3")
                                                        .columnName("_id")
                                                        .apiClassName("com.fsryan.annotationprocessor.generator.code.TestTable3")
                                                        .build())
                                                .build()))
                                .tableName("test_table_2")
                                .build())
                .addTable(table().qualifiedClassName("com.fsryan.annotationprocessor.generator.code.TestTable3")
                                .columnMap(columnMapOf(idCol(),
                                        modifiedCol(),
                                        createdCol(),
                                        deletedCol(),
                                        doubleCol().columnName("app_rating")
                                                .methodName("appRating")
                                                .build(),
                                        bigDecimalCol().columnName("competitor_app_rating")
                                                .methodName("competitorAppRating")
                                                .searchable(false)
                                                .build(),
                                        longCol().columnName("global_id")
                                                .methodName("globalId")
                                                .orderable(false)
                                                .build(),
                                        intCol().columnName("login_count")
                                                .methodName("loginCount")
                                                .build()))
                                .tableName("test_table_3")
                                .build())
                .build();
    }

    public static class TableContextBuilder {

        private final Map<String, TableInfo> tableMap = new HashMap<>();

        public TableContextBuilder addTable(TableInfo table) {
            tableMap.put(table.getTableName(), table);
            return this;
        }

        public TableContext build() {
            return new TableContext() {
                @Override
                public boolean hasTable(String tableName) {
                    return tableMap.containsKey(tableName);
                }

                @Override
                public TableInfo getTable(String tableName) {
                    return tableMap.get(tableName);
                }

                @Override
                public Collection<TableInfo> allTables() {
                    return tableMap.values();
                }

                @Override
                public Map<String, TableInfo> tableMap() {
                    return tableMap;
                }
            };
        }
    }
}
