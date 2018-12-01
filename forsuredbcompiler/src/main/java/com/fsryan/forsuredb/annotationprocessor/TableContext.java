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
package com.fsryan.forsuredb.annotationprocessor;

import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.info.TableInfo;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fsryan.forsuredb.annotationprocessor.util.StreamUtil.mapCollector;
import static java.util.stream.Collectors.toSet;

/**
 * <p>A snapshot of a schema
 */
public interface TableContext {

    class Builder {

        /**
         * <p>table fq name -> {@link TableInfo}
         */
        private final Map<String, TableInfo.Builder> tableInfoMap = new HashMap<>();
        /**
         * <p>table fq name -> table name
         */
        private final Map<String, String> tableFQNameNameMap = new HashMap<>();
        /**
         * <p>table fq name#methodName -> {@link ColumnInfo}
         */
        private final Map<String, ColumnInfo.Builder> columnInfoMap= new HashMap<>();
        /**
         * <p>table fq name#compositeId -> {@link TableForeignKeyInfo}
         */
        private final Map<String, TableForeignKeyInfo.Builder> compositeForeignKeyInfoMap = new HashMap<>();
        /**
         * <p>table fq name -> {@link TableForeignKeyInfo}
         */
        private final Map<String, Set<TableForeignKeyInfo.Builder>> nonCompositeForeignKeyInfoMap = new HashMap<>();


        public Builder addTable(String tableName, String tableClassName, TableInfo.Builder builder) {
            tableInfoMap.put(tableClassName, builder);
            tableFQNameNameMap.put(tableClassName, tableName);
            return this;
        }

        public Builder addColumn(String tableClassName, String columnMethodName, ColumnInfo.Builder builder) {
            columnInfoMap.put(nestedKey(tableClassName, columnMethodName), builder);
            return this;
        }

        public Builder addForeignKeyInfo(String tableClassName, String compositeId, TableForeignKeyInfo.Builder builder) {
            if (compositeId == null || compositeId.isEmpty()) {
                Set<TableForeignKeyInfo.Builder> set = nonCompositeForeignKeyInfoMap.computeIfAbsent(tableClassName, k -> new HashSet<>());
                set.add(builder);
                return this;
            }

            final String key = nestedKey(tableClassName, compositeId);
            TableForeignKeyInfo.Builder existing = compositeForeignKeyInfoMap.computeIfAbsent(key, k -> builder);
            if (existing == builder) {
                return this;
            }

            existing.addAllLocalToForeignColumns(builder.localToForeignColumnMap());
            return this;
        }

        public TableContext build() {
            final Map<String, TableInfo> schema = tableInfoMap.keySet().stream()
                    .map(this::buildTable)
                    .collect(mapCollector((dest, table) -> dest.put(table.qualifiedClassName(), table)));

            // a TableContext that returns copies of everything
            return fromSchema(schema);
        }

        private TableInfo buildTable(String tableClassName) {
            final TableInfo.Builder builder = tableInfoMap.get(tableClassName);
            if (builder == null) {
                throw new IllegalStateException("Could not find " + TableInfo.Builder.class + " for key '" + tableClassName + "'");
            }

            Set<ColumnInfo> columns = columnInfoMap.keySet()
                    .stream()
                    .filter(k -> k.startsWith(tableClassName + "#"))
                    .map(columnInfoMap::get)
                    .map(ColumnInfo.Builder::build)
                    .collect(Collectors.toSet());

            Stream<TableForeignKeyInfo.Builder> compositeStream = compositeForeignKeyInfoMap.keySet().stream()
                    .filter(k -> k.startsWith(tableClassName + "#"))
                    .map(compositeForeignKeyInfoMap::get);
            Stream<TableForeignKeyInfo.Builder> nonCompositeStream = nonCompositeForeignKeyInfoMap
                    .computeIfAbsent(tableClassName, k -> Collections.emptySet())
                    .stream();
            Set<TableForeignKeyInfo> foreignKeys = Stream.concat(compositeStream, nonCompositeStream)
                    .map(tfkiBuilder -> tfkiBuilder.foreignTableName(findForeignTable(tfkiBuilder.foreignTableApiClassName())))
                    .map(TableForeignKeyInfo.Builder::build)
                    .collect(toSet());

            return builder.addAllColumns(columns)
                    .addAllForeignKeys(foreignKeys)
                    .build();
        }

        private String findForeignTable(String tableClassName) {
            return tableFQNameNameMap.entrySet().stream()
                    .filter(e -> tableClassName.equals(e.getKey()))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Could not find table name for class name: '" + tableClassName + "'"));
        }

        private static String nestedKey(String tableClassName, String nestedLevel) {
            return tableClassName + "#" + nestedLevel;
        }

//        private Map<String, ColumnInfo> buildColumnMap(String tableName) {
//            Map<String, ColumnInfo> tableColumnInfoMap = new HashMap<>();
//            if (!columnInfoMap.containsKey(tableName)) {
//                throw new IllegalStateException("found no columns for table key: " + tableName);
//            }
//
//            columnInfoMap.get(tableName).values().forEach(builder -> {
//                ColumnInfo columnInfo = builder.build();
//                tableColumnInfoMap.put(columnInfo.columnName(), columnInfo);
//            });
//            return tableColumnInfoMap;
//        }
//
//        private Set<TableForeignKeyInfo> collapseForeignKeys(String tableKey) {
//            Set<TableForeignKeyInfo> foreignKeys = new HashSet<>();
//            Map<String, Set<TableForeignKeyInfo.Builder>> forTable = tableForeignKeyInfoMap.get(tableKey);
//            if (forTable == null) {
//                return Collections.emptySet();
//            }
//
//            forTable.forEach((compositeId, builders) -> {
//                if ("".equals(compositeId)) {
//                    foreignKeys.addAll(builders.stream()
//                            .map(TableForeignKeyInfo.Builder::build)
//                            .map(tfki -> tfki.toBuilder()
//                                    .foreignTableName(tableClassNameToNameMap.get(tfki.foreignTableApiClassName()))
//                                    .build())
//                            .collect(toSet()));
//                } else {
//                    TableForeignKeyInfo.Builder accumulator = null;
//                    for (TableForeignKeyInfo.Builder b : builders) {
//                        if (accumulator == null) {
//                            accumulator = b;
//                        } else {
//                            TableForeignKeyInfo accumulated = accumulator.build();
//                            Map<String, String> localToForeignColumnMap = new HashMap<>(accumulated.localToForeignColumnMap());
//                            localToForeignColumnMap.putAll(b.build().localToForeignColumnMap());
//                            accumulator = accumulated.toBuilder()
//                                    .localToForeignColumnMap(localToForeignColumnMap);
//                        }
//                    }
//                    TableForeignKeyInfo tfki = accumulator.build();
//                    foreignKeys.add(tfki.toBuilder()
//                            .foreignTableName(tableClassNameToNameMap.get(tfki.foreignTableApiClassName()))
//                            .build());
//                }
//            });
//
//            return foreignKeys;
//        }
    }

    static TableContext empty() {
        return fromSchema(null);
    }

    static TableContext fromSchema(Map<String, TableInfo> schema) {
        final Map<String, TableInfo> actualSchema = schema == null ? Collections.emptyMap() : new HashMap<>(schema);
        return new TableContext() {
            @Override
            public boolean hasTableWithName(String tableName) {
                if (tableName == null) {
                    return false;
                }
                return actualSchema.values().stream().anyMatch(t -> tableName.equals(t.tableName()));
            }

            @Override
            public TableInfo getTableByName(String tableName) {
                if (tableName == null) {
                    return null;
                }
                return actualSchema.values().stream()
                        .filter(t -> tableName.equals(t.tableName()))
                        .findFirst()
                        .orElse(null);
            }

            @Override
            public Collection<TableInfo> allTables() {
                return actualSchema.values();
            }

            @Override
            public Map<String, TableInfo> tableMap() {
                return actualSchema;
            }
        };
    }

    /**
     * @param tableName the name of the table to check
     * @return true if the table exists within the context
     */
    boolean hasTableWithName(String tableName);

    /**
     * @param tableName the name of the table to get
     * @return a TableInfo object if the context contains the table and null if not
     */
    TableInfo getTableByName(String tableName);

    /**
     * @return all of the tables in the context
     */
    Collection<TableInfo> allTables();

    /**
     * @return a map from table_name -&gt; TableInfo for all tables
     */
    Map<String, TableInfo> tableMap();
}
