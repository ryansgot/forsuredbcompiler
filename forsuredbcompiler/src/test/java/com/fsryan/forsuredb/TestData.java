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
import com.fsryan.forsuredb.api.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.api.info.TableInfo;
import com.fsryan.forsuredb.api.migration.Migration;
import com.google.common.collect.ImmutableMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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
    public static ProgressiveTableInfoBuilder table() {
        return table(TABLE_NAME);
    }

    public static ProgressiveTableInfoBuilder table(String tableName, ColumnInfo... columns) {
        ProgressiveTableInfoBuilder ret = new ProgressiveTableInfoBuilder()
                .tableName(tableName)
                .qualifiedClassName(TABLE_CLASS_NAME);
        Arrays.stream(DEFAULT_COLUMNS).forEach(ret::addToColumns);
        if (columns != null && columns.length > 0) {
            Arrays.stream(columns).forEach(ret::addToColumns);
        }
        return ret;
    }

    public static ProgressiveTableInfoBuilder defaultPkTable(String tableName, ColumnInfo... columns) {
        return table(tableName, columns).addToPrimaryKey(TableInfo.DEFAULT_PRIMARY_KEY_COLUMN);
    }

    public static ImmutableMap.Builder<String, ColumnInfo> baseColumnMapBuilder() {
        return new ImmutableMap.Builder<String, ColumnInfo>()
                .put(idCol().getColumnName(), idCol())
                .put(createdCol().getColumnName(), createdCol())
                .put(modifiedCol().getColumnName(), modifiedCol())
                .put(deletedCol().getColumnName(), deletedCol());
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

    public static ProgressiveMigrationBuilder migration(Migration.Type type) {
        return new ProgressiveMigrationBuilder().type(type);
    }

    public static Migration createTableMigration(String tableName) {
        return migration(Migration.Type.CREATE_TABLE).tableName(tableName).build();
    }

    public static ProgressiveMigrationBuilder addColumnMigration(String tableName) {
        return migration(Migration.Type.ALTER_TABLE_ADD_COLUMN).tableName(tableName);
    }

    public static ProgressiveMigrationBuilder addForeignKeyReferenceMigration(String tableName) {
        return migration(Migration.Type.ADD_FOREIGN_KEY_REFERENCE).tableName(tableName);
    }

    public static ProgressiveMigrationBuilder updateForeignKeysMigration(String tableName) {
        return migration(Migration.Type.UPDATE_FOREIGN_KEYS).tableName(tableName);
    }

    public static ProgressiveMigrationBuilder updatePrimaryKeyMigration(String tableName) {
        return migration(Migration.Type.UPDATE_PRIMARY_KEY).tableName(tableName);
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
        return columnFrom("java.lang.String").methodName("stringColumn");
    }

    public static ColumnInfo.Builder intCol() {
        return columnFrom("int").methodName("intColumn");
    }

    public static ColumnInfo.Builder longCol() {
        return columnFrom("long").methodName("longColumn");
    }

    public static ColumnInfo.Builder doubleCol() {
        return columnFrom("double").methodName("doubleColumn");
    }

    public static ColumnInfo.Builder booleanCol() {
        return columnFrom("boolean").methodName("booleanColumn");
    }

    public static ColumnInfo.Builder bigDecimalCol() {
        return columnFrom("java.math.BigDecimal").methodName("bigDecimalColumn");
    }

    public static ColumnInfo.Builder dateCol() {
        return columnFrom("java.util.Date").methodName("dateColumn");
    }

    public static TableForeignKeyInfo.Builder dbmsDefaultTFKI(String foreignTableName) {
        return new TableForeignKeyInfo.Builder()
                .foreignTableName(foreignTableName)
                .updateChangeAction("")
                .deleteChangeAction("");
    }

    public static ForeignKeyInfo.Builder cascadeFKI(String foreignKeyTableName) {
        return ForeignKeyInfo.builder().updateAction(ForeignKey.ChangeAction.CASCADE)
                .deleteAction(ForeignKey.ChangeAction.CASCADE)
                .columnName("_id")
                .tableName(foreignKeyTableName);
    }

    public static TableForeignKeyInfo.Builder cascadeTFKI(String foreignTableName) {
        return dbmsDefaultTFKI(foreignTableName).updateChangeAction("CASCADE").deleteChangeAction("CASCADE");
    }

    public static ForeignKeyInfo.Builder noActionFKI(String foreignKeyTableName) {
        return ForeignKeyInfo.builder().updateAction(ForeignKey.ChangeAction.NO_ACTION)
                .deleteAction(ForeignKey.ChangeAction.NO_ACTION)
                .columnName("_id")
                .tableName(foreignKeyTableName);
    }

    public static TableForeignKeyInfo.Builder noActionTFKI(String foreignTableName) {
        return dbmsDefaultTFKI(foreignTableName).updateChangeAction("NO ACTION").deleteChangeAction("NO ACTION");
    }

    public static ForeignKeyInfo.Builder setNullFKI(String foreignKeyTableName) {
        return ForeignKeyInfo.builder().updateAction(ForeignKey.ChangeAction.SET_NULL)
                .deleteAction(ForeignKey.ChangeAction.SET_NULL)
                .columnName("_id")
                .tableName(foreignKeyTableName);
    }

    public static TableForeignKeyInfo.Builder setNullTFKI(String foreignTableName) {
        return dbmsDefaultTFKI(foreignTableName).updateChangeAction("SET NULL").deleteChangeAction("SET NULL");
    }

    public static ForeignKeyInfo.Builder setDefaultFKI(String foreignKeyTableName) {
        return ForeignKeyInfo.builder().updateAction(ForeignKey.ChangeAction.SET_DEFAULT)
                .deleteAction(ForeignKey.ChangeAction.SET_DEFAULT)
                .columnName("_id")
                .tableName(foreignKeyTableName);
    }

    public static TableForeignKeyInfo.Builder setDefaultTFKI(String foreignTableName) {
        return dbmsDefaultTFKI(foreignTableName).updateChangeAction("SET DEFAULT").deleteChangeAction("SET DEFAULT");
    }

    public static ForeignKeyInfo.Builder restrictFKI(String foreignKeyTableName) {
        return ForeignKeyInfo.builder().updateAction(ForeignKey.ChangeAction.RESTRICT)
                .deleteAction(ForeignKey.ChangeAction.RESTRICT)
                .columnName("_id")
                .tableName(foreignKeyTableName);
    }

    public static TableForeignKeyInfo.Builder restrictTFKI(String foreignTableName) {
        return dbmsDefaultTFKI(foreignTableName).updateChangeAction("RESTRICT").deleteChangeAction("RESTRICT");
    }

    public static TableContextBuilder newTableContext(TableInfo... tables) {
        TableContextBuilder ret = new TableContextBuilder();
        if (tables != null && tables.length > 0) {
            Arrays.stream(tables).forEach(t -> ret.addTable(t));
        }
        return ret;
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
        return newTableContext().addTable(
                        table("test_table")
                                .qualifiedClassName("com.fsryan.annotationprocessor.generator.code.TestTable")
                                .staticDataAsset("test_table_data.xml")
                                .staticDataRecordName("test_table_data")
                                .addToColumns(longCol().columnName("test_table_2_id")
                                        .methodName("testTable2Id")
                                        .foreignKeyInfo(cascadeFKI("test_table_2")
                                                .columnName("_id")
                                                .apiClassName("com.fsryan.annotationprocessor.generator.code.TestTable2")
                                                .build())
                                        .build())
                                .build()
                )
                .addTable(
                        table("test_table_2")
                                .qualifiedClassName("com.fsryan.annotationprocessor.generator.code.TestTable2")
                                .addToColumns(longCol().columnName("test_table_3_id")
                                        .methodName("testTable3Id")
                                        .foreignKeyInfo(cascadeFKI("test_table_3")
                                                .columnName("_id")
                                                .apiClassName("com.fsryan.annotationprocessor.generator.code.TestTable3")
                                                .build())
                                        .build())
                                .build()
                )
                .addTable(table("test_table_3")
                        .qualifiedClassName("com.fsryan.annotationprocessor.generator.code.TestTable3")
                        .addToColumns(doubleCol().columnName("app_rating").methodName("appRating").build())
                        .addToColumns(bigDecimalCol().columnName("competitor_app_rating").methodName("competitorAppRating").searchable(false).build())
                        .addToColumns(longCol().columnName("global_id").methodName("globalId").orderable(false).build())
                        .addToColumns(intCol().columnName("login_count").methodName("loginCount").build())
                        .build()
                )
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

    public static class ProgressiveTableInfoBuilder {

        private Set<TableForeignKeyInfo> foreignKeys = new HashSet<>();
        private Set<String> primaryKey = new HashSet<>();
        private Map<String, ColumnInfo> columnMap = new HashMap<>();
        private final TableInfo.Builder realBuilder = TableInfo.builder();

        public ProgressiveTableInfoBuilder tableName(String tableName) {
            realBuilder.tableName(tableName);
            return this;
        }

        public ProgressiveTableInfoBuilder addTableForeignKey(TableForeignKeyInfo... foreignKeys) {
            if (foreignKeys != null && foreignKeys.length > 0) {
                Arrays.stream(foreignKeys).forEach(fk -> this.foreignKeys.add(fk));
            }
            return this;
        }

        public ProgressiveTableInfoBuilder foreignKeys(Set<TableForeignKeyInfo> foreignKeys) {
            this.foreignKeys = foreignKeys;
            return this;
        }

        public ProgressiveTableInfoBuilder addToPrimaryKey(String... primaryKeyColumns) {
            if (primaryKeyColumns != null && primaryKeyColumns.length > 0) {
                Arrays.stream(primaryKeyColumns).forEach(pkc -> this.primaryKey.add(pkc));
            }
            return this;
        }

        public ProgressiveTableInfoBuilder primaryKey(Set<String> primaryKey) {
            this.primaryKey = primaryKey;
            return this;
        }

        public ProgressiveTableInfoBuilder primaryKeyOnConflict(String primaryKeyOnConflict) {
            realBuilder.primaryKeyOnConflict(primaryKeyOnConflict);
            return this;
        }

        public ProgressiveTableInfoBuilder qualifiedClassName(String tableName) {
            realBuilder.qualifiedClassName(tableName);
            return this;
        }

        public ProgressiveTableInfoBuilder addToColumns(ColumnInfo... columns) {
            if (columns != null && columns.length > 0) {
                Arrays.stream(columns).forEach(c -> columnMap.put(c.getColumnName(), c));
            }
            return this;
        }

        public ProgressiveTableInfoBuilder columnMap(Map<String, ColumnInfo> columnMap) {
            this.columnMap = columnMap;
            return this;
        }

        public ProgressiveTableInfoBuilder docStoreParameterization(String docStoreParameterization) {
            realBuilder.docStoreParameterization(docStoreParameterization);
            return this;
        }

        public ProgressiveTableInfoBuilder staticDataAsset(String staticDataAsset) {
            realBuilder.staticDataAsset(staticDataAsset);
            return this;
        }

        public ProgressiveTableInfoBuilder staticDataRecordName(String staticDataRecordName) {
            realBuilder.staticDataRecordName(staticDataRecordName);
            return this;
        }

        public TableInfo build() {
            return realBuilder.foreignKeys(foreignKeys).columnMap(columnMap).primaryKey(primaryKey).build();
        }
    }

    public static class ProgressiveMigrationBuilder {

        private Migration.Builder realBuilder = Migration.builder();
        private Map<String, String> extras;

        public ProgressiveMigrationBuilder type(Migration.Type type) {
            realBuilder.type(type);
            return this;
        }

        public ProgressiveMigrationBuilder tableName(String tableName) {
            realBuilder.tableName(tableName);
            return this;
        }

        public ProgressiveMigrationBuilder columnName(String columnName) {
            realBuilder.columnName(columnName);
            return this;
        }

        public ProgressiveMigrationBuilder extras(Map<String, String> extras) {
            this.extras = extras;
            return this;
        }

        public ProgressiveMigrationBuilder addExtra(String key, String value) {
            if (extras == null) {
                extras = new HashMap<>();
            }
            extras.put(key, value);
            return this;
        }

        public Migration build() {
            return realBuilder.extras(extras).build();
        }
    }
}
