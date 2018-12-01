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

import com.fsryan.forsuredb.annotationprocessor.util.StreamUtil;
import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.annotationprocessor.util.APLog;
import com.fsryan.forsuredb.api.migration.MigrationRetriever;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class MigrationContext implements TableContext {

    private static final String LOG_TAG = MigrationContext.class.getSimpleName();

    private final MigrationRetriever mr;
    private Map<String, TableInfo> tableMap;

    public MigrationContext(MigrationRetriever mr) {
        this.mr = mr;
    }

    @Nonnull
    public static TableContext createFromMigrationRetriever(@Nullable MigrationRetriever mr) {
        return createfromMigrations(mr == null ? null : mr.getMigrationSets());
    }

    @Nonnull
    public static TableContext createfromMigrations(@Nullable List<MigrationSet> migrationSets) {
        if (migrationSets == null || migrationSets.isEmpty()) {
            return TableContext.empty();
        }

        MigrationSet max = migrationSets.stream()
                .max((ms1, ms2) -> ms2.dbVersion() - ms1.dbVersion())
                .orElseThrow(() -> new IllegalStateException("nonempty migration sets list must have a max db version"));
        return max.setVersion() < 2 ? convert(max.targetSchema()) : TableContext.fromSchema(max.targetSchema());
    }

    private static TableContext convert(Map<String, TableInfo> legacySchema) {
        Map<String, TableInfo> schema = legacySchema.entrySet().stream()
                .map(Map.Entry::getValue)
                .collect(StreamUtil.mapCollector(MigrationContext::accumulateLegacy));
        return TableContext.fromSchema(schema);
    }

    private static void accumulateLegacy(Map<String, TableInfo> acc, TableInfo table) {
        Set<ColumnInfo> columns = new HashSet<>(table.getColumns());
        TableInfo converted = table.toBuilder()
                .clearColumns()
                .addAllColumns(columns)
                .build();
        acc.put(converted.qualifiedClassName(), converted);
    }


    @Override
    public boolean hasTableWithName(String tableName) {
        createTableMapIfNull();
        return tableMap.containsKey(tableName);
    }

    @Override
    public TableInfo getTableByName(String tableName) {
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
            for (Migration m : migrationSet.orderedMigrations()) {
                final TableInfo table = migrationSet.targetSchema().get(m.tableName());
                update(table, m, tableBuilderMap, columnBuilderMap);
            }
        }

        for (Map.Entry<String, Map<String, ColumnInfo>> entry : createTableBuilderKeyToColumnMapMap(columnBuilderMap).entrySet()) {
            TableInfo.Builder tb = tableBuilderMap.get(entry.getKey());
            TableInfo temp = tb.build();
            Map<String, ColumnInfo> previousColumnMap = temp.columnMap();    // <-- cannot overwrite previous
            previousColumnMap.putAll(entry.getValue());
            tb.addAllColumns(previousColumnMap.values());
        }

        Map<String, TableInfo> retMap = new HashMap<>();
        for (Map.Entry<String, TableInfo.Builder> entry : tableBuilderMap.entrySet()) {
            retMap.put(entry.getKey(), entry.getValue().build());
        }

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

    // TODO: drop table handling; consider changing the structure from a big switch
    private void update(TableInfo table,
                        Migration m,
                        Map<String, TableInfo.Builder> tableBuilderMap,
                        Map<String, ColumnInfo.Builder> columnBuilderMap) {
        switch (m.type()) {
            case CREATE_TABLE:
                tableBuilderMap.put(tableKey(m), table.toBuilder());
                break;
            case UPDATE_PRIMARY_KEY:
                handleUpdatePrimaryKey(table, m, tableBuilderMap);
                for (String primaryKeyColumnName : table.getPrimaryKey()) {
                    final String columnKey = columnKey(table.tableName(), primaryKeyColumnName);
                    final ColumnInfo column = table.getColumn(primaryKeyColumnName);
                    columnBuilderMap.put(columnKey, column.toBuilder());
                }
                break;
            case ADD_FOREIGN_KEY_REFERENCE:
                // intentionaly falling through
            case UPDATE_FOREIGN_KEYS:
                handleUpdateForeignKeys(table, m, tableBuilderMap);
                for (TableForeignKeyInfo foreignKey : table.foreignKeys()) {
                    for (String columnName : foreignKey.localToForeignColumnMap().keySet()) {
                        columnBuilderMap.put(columnKey(table.tableName(), columnName), table.getColumn(columnName).toBuilder());
                    }
                }
                break;
            case CHANGE_DEFAULT_VALUE:
                // intentionally falling through
            case ALTER_TABLE_ADD_UNIQUE:
                // intentionally falling through
            case MAKE_COLUMN_UNIQUE:
                // intentionally falling through
            case ALTER_TABLE_ADD_COLUMN:
                columnBuilderMap.put(columnKey(m), table.getColumn(m.columnName()).toBuilder());
                break;
            case ADD_INDEX:
                columnBuilderMap.put(columnKey(m), table.getColumn(m.columnName()).toBuilder().index(true));
                break;
            default:
                APLog.w(LOG_TAG, "Not handling update of type " + m.type() + "; this could cause the migration context to misrepresent the existing schema.");
        }
    }

    private void handleUpdatePrimaryKey(TableInfo table, Migration m, Map<String, TableInfo.Builder> tableBuilderMap) {
        TableInfo.Builder tb = tableBuilderMap.get(tableKey(m));
        if (tb == null) {
            throw new RuntimeException("cannot find table " + m.tableName() + " in prior migration context");
        }
        tb.resetPrimaryKey(table.getPrimaryKey());
    }

    private void handleUpdateForeignKeys(TableInfo table, Migration m, Map<String, TableInfo.Builder> tableBuilderMap) {
        TableInfo.Builder tb = tableBuilderMap.get(tableKey(m));
        if (tb == null) {
            throw new RuntimeException("cannot find table " + m.tableName() + " in prior migration context");
        }
        tb.addAllForeignKeys(table.foreignKeys());
    }

    private String tableKey(Migration m) {
        return m.tableName();
    }

    private String columnKey(Migration m) {
        return columnKey(m.tableName(), m.columnName());
    }

    private String columnKey(String tableName, String columnName) {
        return tableName + "." + columnName;
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
