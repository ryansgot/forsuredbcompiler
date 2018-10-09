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
import com.fsryan.forsuredb.info.TableIndexInfo;
import com.fsryan.forsuredb.info.TableInfo;

import java.util.*;
import java.util.function.BinaryOperator;

import static java.util.stream.Collectors.toSet;

/**
 * <p>
 *     Describes a collection of tables.
 * </p>
 */
public interface TableContext {

    class Builder {

        // tableKey -> TableInfo.BuilderCompat
        private final Map<String, TableInfo.BuilderCompat> tableInfoMap = new HashMap<>();
        // tableClassName -> tableName
        private final Map<String, String> tableClassNameToNameMap = new HashMap<>();
        // tablekey -> columnKey -> ColumnInfo.Builder
        private final Map<String, Map<String, ColumnInfo.Builder>> columnInfoMap = new HashMap<>();
        // tablekey -> Set of ForeignKeyInfo.Builder
        private final Map<String, Map<String, Set<TableForeignKeyInfo.Builder>>> tableForeignKeyInfoMap = new HashMap<>();
        // tablekey -> Set of TableIndexInfo
        private final Map<String, Map<String, Set<TableIndexInfo>>> tableIndexInfoMap = new HashMap<>();

        public Builder addTable(String tableName, String tableClassName, TableInfo.BuilderCompat builder) {
            tableClassNameToNameMap.put(tableClassName, tableName);
            tableInfoMap.put(tableName, builder);
            TableInfo.defaultColumns().values().forEach(c -> addColumn(tableName, c.columnName(), c.toBuilder()));
            return this;
        }

        public Builder addColumn(String tableKey, String columnKey, ColumnInfo.Builder builder) {
            Map<String, ColumnInfo.Builder> tableColumnInfoMap = columnInfoMap
                    .computeIfAbsent(tableKey, k -> new HashMap<>());
            tableColumnInfoMap.put(columnKey, builder);
            return this;
        }

        public Builder addForeignKeyInfo(String tableKey, String compositeKey, TableForeignKeyInfo.Builder builder) {
            Map<String, Set<TableForeignKeyInfo.Builder>> forTableMap = tableForeignKeyInfoMap
                    .computeIfAbsent(tableKey, k -> new HashMap<>());
            Set<TableForeignKeyInfo.Builder> tmpSet = forTableMap.computeIfAbsent(compositeKey, k -> new HashSet<>());
            tmpSet.add(builder);
            return this;
        }

        public void addTableIndexInfo(String tableKey, String compositeKey, TableIndexInfo tio) {
            Map<String, Set<TableIndexInfo>> forTableMap = tableIndexInfoMap
                    .computeIfAbsent(tableKey, k -> new HashMap<>());
            Set<TableIndexInfo> tmpSet = forTableMap.computeIfAbsent(compositeKey, k -> new HashSet<>());
            tmpSet.add(tio);
        }

        public TableContext build() {
            final Map<String, TableInfo> schema = new HashMap<>();
            tableInfoMap.keySet().forEach(tableKey -> {
                TableInfo t = tableInfoMap.get(tableKey)
                        .columnMap(buildColumnMap(tableKey))
                        .foreignKeys(collapseForeignKeys(tableKey))
                        .indices(collapseIndices(tableKey))
                        .build();
                schema.put(t.tableName(), t);
            });

            // a TableContext that returns copies of everything
            return new BasicTableContext(schema);
        }

        private Map<String, ColumnInfo> buildColumnMap(String tableName) {
            Map<String, ColumnInfo> tableColumnInfoMap = new HashMap<>();
            if (!columnInfoMap.containsKey(tableName)) {
                throw new IllegalStateException("found no columns for table key: " + tableName);
            }

            columnInfoMap.get(tableName).values().forEach(builder -> {
                ColumnInfo columnInfo = builder.build();
                tableColumnInfoMap.put(columnInfo.columnName(), columnInfo);
            });
            return tableColumnInfoMap;
        }

        private Set<TableForeignKeyInfo> collapseForeignKeys(String tableKey) {
            Set<TableForeignKeyInfo> foreignKeys = new HashSet<>();
            Map<String, Set<TableForeignKeyInfo.Builder>> forTable = tableForeignKeyInfoMap.get(tableKey);
            if (forTable == null) {
                return Collections.emptySet();
            }

            forTable.forEach((compositeId, builders) -> {
                if ("".equals(compositeId)) {
                    foreignKeys.addAll(builders.stream()
                            .map(TableForeignKeyInfo.Builder::build)
                            .map(tfki -> tfki.toBuilder()
                                    .foreignTableName(tableClassNameToNameMap.get(tfki.foreignTableApiClassName()))
                                    .build())
                            .collect(toSet()));
                } else {
                    TableForeignKeyInfo.Builder accumulator = null;
                    for (TableForeignKeyInfo.Builder b : builders) {
                        if (accumulator == null) {
                            accumulator = b;
                        } else {
                            TableForeignKeyInfo accumulated = accumulator.build();
                            Map<String, String> localToForeignColumnMap = new HashMap<>(accumulated.localToForeignColumnMap());
                            localToForeignColumnMap.putAll(b.build().localToForeignColumnMap());
                            accumulator = accumulated.toBuilder()
                                    .localToForeignColumnMap(localToForeignColumnMap);
                        }
                    }
                    TableForeignKeyInfo tfki = accumulator.build();
                    foreignKeys.add(tfki.toBuilder()
                            .foreignTableName(tableClassNameToNameMap.get(tfki.foreignTableApiClassName()))
                            .build());
                }
            });

            return foreignKeys;
        }

        private Set<TableIndexInfo> collapseIndices(String tableKey) {
            Set<TableIndexInfo> indices = new HashSet<>();
            Map<String, Set<TableIndexInfo>> forTable = tableIndexInfoMap.get(tableKey);
            if (forTable == null) {
                return Collections.emptySet();
            }

            forTable.forEach((compositeId, tableIndexInfos) -> {
                if ("".equals(compositeId)) {
                    TableIndexInfo info = tableIndexInfos.stream()
                            .reduce((current, next) -> {
                                Map<String, String> sortOrderMap = new HashMap<>(current.columnSortOrderMap());
                                sortOrderMap.putAll(next.columnSortOrderMap());
                                return TableIndexInfo.create(sortOrderMap, current.unique());
                            }).get();
                    indices.add(info);
                } else {
                    TableIndexInfo accumulator = null;
                    for (TableIndexInfo tio : tableIndexInfos) {
                        if (accumulator == null) {
                            accumulator = tio;
                        } else {
                            Map<String, String> newSortOrderMap = tio.columnSortOrderMap();
                            newSortOrderMap.putAll(accumulator.columnSortOrderMap());
                            if (accumulator.unique() != tio.unique()) {
                                throw new IllegalStateException("Composite indices cannot mix unique and non-unique values: " + newSortOrderMap);
                            }
                            accumulator = TableIndexInfo.create(newSortOrderMap, accumulator.unique());
                        }
                    }
                    indices.add(accumulator);
                }
            });

            return indices;
        }

        static class BasicTableContext implements TableContext {

            private final Map<String, TableInfo> schema;

            BasicTableContext(Map<String, TableInfo> schema) {
                this.schema = schema;
            }

            @Override
            public boolean hasTable(String tableName) {
                return schema.containsKey(tableName);
            }

            @Override
            public TableInfo getTable(String tableName) {
                return schema.get(tableName).toBuilder().build();
            }

            @Override
            public Collection<TableInfo> allTables() {
                return new ArrayList<>(schema.values());
            }

            @Override
            public Map<String, TableInfo> tableMap() {
                return new HashMap<>(schema);
            }
        }
    }

    /**
     * @param tableName the name of the table to check
     * @return true if the table exists within the context
     */
    boolean hasTable(String tableName);

    /**
     * @param tableName the name of the table to get
     * @return a TableInfo object if the context contains the table and null if not
     */
    TableInfo getTable(String tableName);

    /**
     * @return all of the tables in the context
     */
    Collection<TableInfo> allTables();

    /**
     * @return a map from table_name -&gt; TableInfo for all tables
     */
    Map<String, TableInfo> tableMap();
}
