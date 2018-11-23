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

import javax.annotation.Nonnull;
import java.util.*;

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
        private final Map<String, Map<String, List<TableIndexInfo>>> tableIndexInfoMap = new HashMap<>();

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

        public Builder replaceForeignKeyInfo(String tableKey, Set<TableForeignKeyInfo> foreignKeys) {
            Map<String, Set<TableForeignKeyInfo.Builder>> forTableMap = tableForeignKeyInfoMap
                    .computeIfAbsent(tableKey, k -> new HashMap<>());
            forTableMap.clear();
            Set<TableForeignKeyInfo.Builder> all = foreignKeys.stream().map(TableForeignKeyInfo::toBuilder).collect(toSet());
            forTableMap.put("", all);
            return this;
        }

        public Builder addForeignKeyInfo(String tableKey, String compositeKey, TableForeignKeyInfo.Builder builder) {
            Map<String, Set<TableForeignKeyInfo.Builder>> forTableMap = tableForeignKeyInfoMap
                    .computeIfAbsent(tableKey, k -> new HashMap<>());
            Set<TableForeignKeyInfo.Builder> tmpSet = forTableMap.computeIfAbsent(compositeKey, k -> new HashSet<>());
            tmpSet.add(builder);
            return this;
        }

        public Builder addTableIndexInfo(String tableKey, String compositeKey, TableIndexInfo tio) {
            Map<String, List<TableIndexInfo>> forTableMap = tableIndexInfoMap
                    .computeIfAbsent(tableKey, k -> new HashMap<>());
            List<TableIndexInfo> tmpList = forTableMap.computeIfAbsent(compositeKey, k -> new ArrayList<>());
            tmpList.add(tio);
            return this;
        }

        /**
         * <p>Clears the current {@link TableInfo#primaryKey()} for the table
         * at the tableKey passed in and overwrites.
         * @param tableKey the tableKey for the table to overwrite the primary
         *                 key
         * @param primaryKey the {@link List}&lt;{@link String}&gt; describing
         *                   the primary key of this table.
         * @return this {@link Builder}
         */
        public Builder setPrimaryKey(String tableKey, Set<String> primaryKey) {
            TableInfo.BuilderCompat builder = tableInfoMap.get(tableKey);
            if (builder == null) {
                throw new IllegalArgumentException("Cannot find table by key: " + tableKey);
            }
            builder.primaryKey(primaryKey);
            return this;
        }

        /**
         * <p>Clears the current {@link TableIndexInfo} list for the table
         * identified by tableKey
         * @param tableKey the key to the table which
         * @param indices the {@link Set}&lt;{@link TableIndexInfo}&gt; to
         *                replace whatever is currently stored for the key
         * @return this {@link Builder}
         */
        public Builder replaceIndexInfo(String tableKey, Set<TableIndexInfo> indices) {
            Map<String, List<TableIndexInfo>> forTableMap = tableIndexInfoMap.computeIfAbsent(tableKey, k -> new HashMap<>());
            forTableMap.clear();
            List<TableIndexInfo> nonCollapseable = forTableMap.computeIfAbsent("", k -> new ArrayList<>());
            nonCollapseable.addAll(indices);
            return this;
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
            return TableContext.fromSchema(schema);
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
            Map<String, List<TableIndexInfo>> forTable = tableIndexInfoMap.get(tableKey);
            if (forTable == null) {
                return Collections.emptySet();
            }

            Set<TableIndexInfo> indices = new HashSet<>(forTable.getOrDefault("", Collections.emptyList()));
            indices.addAll(forTable.keySet().stream()
                    .filter(key -> !key.isEmpty())  // empty key list was added above
                    .map(key -> mergeTableIndexInfo(forTable.get(key)))
                    .collect(toSet()));

            return indices;
        }

        static TableIndexInfo mergeTableIndexInfo(List<TableIndexInfo> toMerge) {
            if (toMerge.size() < 1) {
                throw new IllegalArgumentException("Cannot merge empty TableIndexInfo list");
            }
            if (toMerge.size() < 2) {
                return toMerge.get(0);
            }
            return toMerge.stream().reduce(TableIndexInfo::merge).orElse(null);
        }

    }

    static TableContext empty() {
        return fromSchema(Collections.emptyMap());
    }

    static TableContext fromSchema(@Nonnull final Map<String, TableInfo> schema) {
        return new TableContext() {

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
        };
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
