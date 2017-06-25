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
import com.fsryan.forsuredb.api.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.api.info.TableInfo;
import com.fsryan.forsuredb.annotationprocessor.util.APLog;
import com.fsryan.forsuredb.api.migration.Migration;
import com.fsryan.forsuredb.api.migration.MigrationRetriever;
import com.fsryan.forsuredb.api.migration.MigrationSet;

import java.util.*;

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
            Set<String> recreatedTables = new HashSet<>();  // <-- need a new set for each migration set because the end state is different for each
            for (Migration m : migrationSet.getOrderedMigrations()) {
                final TableInfo table = migrationSet.getTargetSchema().get(m.getTableName());
                update(table, m, tableBuilderMap, columnBuilderMap, recreatedTables);
            }
        }

        for (Map.Entry<String, Map<String, ColumnInfo>> entry : createTableBuilderKeyToColumnMapMap(columnBuilderMap).entrySet()) {
            TableInfo.Builder tb = tableBuilderMap.get(entry.getKey());
            TableInfo temp = tb.build();
            Map<String, ColumnInfo> previousColumnMap = temp.getColumnMap();    // <-- cannot overwrite previous
            previousColumnMap.putAll(entry.getValue());
            tb.columnMap(previousColumnMap);
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

    // TODO: drop table handling; consider changing the structure from a big switch
    private void update(TableInfo table,
                        Migration m,
                        Map<String, TableInfo.Builder> tableBuilderMap,
                        Map<String, ColumnInfo.Builder> columnBuilderMap,
                        Set<String> recreatedTables) {
        switch (m.getType()) {
            case CREATE_TABLE:
                tableBuilderMap.put(tableKey(m), table.newBuilder());
                recreatedTables.add(table.getTableName());
                break;
            case UPDATE_PRIMARY_KEY:
                if (!recreatedTables.contains(table.getTableName())) {
                    handleUpdatePrimaryKey(table, m, tableBuilderMap);
                    recreatedTables.add(table.getTableName());
                }
                for (String primaryKeyColumnName : table.getPrimaryKey()) {
                    final String columnKey = columnKey(table.getTableName(), primaryKeyColumnName);
                    final ColumnInfo column = table.getColumn(primaryKeyColumnName);
                    columnBuilderMap.put(columnKey, column.newBuilder());
                }
                break;
            case ADD_FOREIGN_KEY_REFERENCE:
                // intentionaly falling through
            case UPDATE_FOREIGN_KEYS:
                if (!recreatedTables.contains(table.getTableName())) {
                    handleUpdateForeignKeys(table, m, tableBuilderMap);
                    recreatedTables.add(table.getTableName());
                }
                for (TableForeignKeyInfo foreignKey : table.getForeignKeys()) {
                    for (String columnName : foreignKey.getLocalToForeignColumnMap().keySet()) {
                        columnBuilderMap.put(columnKey(table.getTableName(), columnName), table.getColumn(columnName).newBuilder());
                    }
                }
                break;
            case ALTER_TABLE_ADD_UNIQUE:
                // intentionally falling through
            case MAKE_COLUMN_UNIQUE:
                // intentionally falling through
            case ALTER_TABLE_ADD_COLUMN:
                columnBuilderMap.put(columnKey(m), table.getColumn(m.getColumnName()).newBuilder());
        }
    }

    private void handleUpdatePrimaryKey(TableInfo table, Migration m, Map<String, TableInfo.Builder> tableBuilderMap) {
        TableInfo.Builder tb = tableBuilderMap.get(tableKey(m));
        if (tb == null) {
            throw new RuntimeException("cannot find table " + m.getTableName() + " in prior migration context");
        }
        tb.primaryKey(table.getPrimaryKey());
    }

    private void handleUpdateForeignKeys(TableInfo table, Migration m, Map<String, TableInfo.Builder> tableBuilderMap) {
        TableInfo.Builder tb = tableBuilderMap.get(tableKey(m));
        if (tb == null) {
            throw new RuntimeException("cannot find table " + m.getTableName() + " in prior migration context");
        }
        tb.foreignKeys(table.getForeignKeys());
    }

    private String tableKey(Migration m) {
        return m.getTableName();
    }

    private String columnKey(Migration m) {
        return columnKey(m.getTableName(), m.getColumnName());
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
