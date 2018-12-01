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

import com.fsryan.forsuredb.api.migration.MigrationRetriever;
import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.MigrationSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    }

    @Nonnull
    static TableContext empty() {
        return fromSchema(null);
    }

    /**
     * <p>Use this method to build the migration context. The table schema with
     * the highest revision number will be selected to represent the migration
     * context. In otherwords, the current schema prior to applying any changes
     * caused by a difference between all previous changes and the current
     * state of the code.
     * <p>Note that this method updates the {@link MigrationSet#targetSchema()}
     * to be of the format that the rest of the system (at compile and runtime)
     * expects
     * @param mr the {@link MigrationRetriever} that retrieves all migrations
     * @return the {@link TableContext} associated with the migration context
     * or {@link TableContext#empty()} if the input is null or no
     * {@link MigrationSet}s could be found by the {@link MigrationRetriever}
     */
    @Nonnull
    static TableContext fromMigrationRetrieverCompat(@Nullable MigrationRetriever mr) {
        return mr == null ? empty() : fromMigrationsCompat(mr.getMigrationSets());
    }

    /**
     * @param migrationSets the list of all {@link MigrationSet} that have
     *                      historically been a part of the project
     * @return the {@link TableContext} associated with the highest
     * {@link MigrationSet#dbVersion()} in the input list or {@link #empty()}
     * if the input is null or empty.
     * @see #fromMigrationRetrieverCompat(MigrationRetriever)
     */
    @Nonnull
    static TableContext fromMigrationsCompat(@Nullable List<MigrationSet> migrationSets) {
        if (migrationSets == null || migrationSets.isEmpty()) {
            return empty();
        }

        // finds the migration set with the max db version
        // converts to the latest schema
        Map<String, TableInfo> schema = migrationSets.stream()
                .max((ms1, ms2) -> ms2.dbVersion() - ms1.dbVersion())
                .map(ms -> ms.setVersion() == 1 ? convertToMigrationSetV2(ms) : ms)
                .map(MigrationSet::targetSchema)
                .orElseThrow(() -> new IllegalStateException("nonempty migration sets list must have a max db version"));
        return fromSchema(schema);
    }

    @Nonnull
    static TableContext fromSchema(@Nullable Map<String, TableInfo> schema) {
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

    static MigrationSet convertToMigrationSetV2(MigrationSet ms) {
        Map<String, TableInfo> schema = ms.targetSchema().values().stream()
                .map(table -> table.toBuilder()
                        .clearColumns()
                        .addAllColumns(table.getColumns())
                ).map(TableInfo.Builder::build)
                .collect(mapCollector((acc, table) -> acc.put(table.qualifiedClassName(), table)));
        return ms.toBuilder()
                .setVersion(2)
                .targetSchema(schema)
                .build();
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
