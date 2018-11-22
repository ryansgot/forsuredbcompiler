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

import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.annotationprocessor.util.APLog;
import com.fsryan.forsuredb.api.migration.MigrationRetriever;

import java.util.*;
import java.util.stream.Collectors;

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
        TableContext.Builder builder = new TableContext.Builder();
        for (MigrationSet set : mr.getMigrationSets()) {
            for (Migration m : set.orderedMigrations()) {
                update(builder, m, set.targetSchema());
            }
        }

        return builder.build().tableMap();
    }

    private void update(Builder builder, Migration m, Map<String, TableInfo> currentSchema) {
        TableInfo table = currentSchema.get(m.tableName());
        switch (m.type()) {
            case CREATE_TABLE:
                builder.addTable(table.tableName(), table.qualifiedClassName(), table.toBuilderCompat());
                table.getColumns().stream()
                        .filter(c -> !TableInfo.defaultColumns().containsKey(c.columnName()))
                        .forEach(c -> builder.addColumn(table.tableName(), c.columnName(), c.toBuilder()));
                builder.replaceForeignKeyInfo(table.tableName(), table.foreignKeys());
                break;
            case UPDATE_PRIMARY_KEY:
                builder.setPrimaryKey(table.tableName(), table.primaryKey());
                table.getPrimaryKey().stream()
                        .map(table::getColumn)
                        .forEach(c -> builder.addColumn(table.tableName(), c.columnName(), c.toBuilder()));
                break;
            case ADD_FOREIGN_KEY_REFERENCE:
                // intentionaly falling through
            case UPDATE_FOREIGN_KEYS:
                table.getForeignKeyColumns()
                        .forEach(fkColumn -> builder.addColumn(table.tableName(), fkColumn.columnName(), fkColumn.toBuilder()));
                builder.replaceForeignKeyInfo(table.tableName(), table.foreignKeys());
                break;
            case CHANGE_DEFAULT_VALUE:
                // intentionally falling through
            case ALTER_TABLE_ADD_UNIQUE:
                // intentionally falling through
            case MAKE_COLUMN_UNIQUE:
                // intentionally falling through
            case ALTER_TABLE_ADD_COLUMN: {
                ColumnInfo column = table.getColumn(m.columnName());
                builder.addColumn(table.tableName(), m.columnName(), column.toBuilder());
                break;
            }
            case ADD_INDEX: {
                builder.replaceIndexInfo(table.tableName(), table.indices());
                ColumnInfo column = table.getColumn(m.columnName());
                // Legacy versions used to set a column name to be the index.
                // This happened because indices used to only be supported on a
                // single column. This functionality has been replaced by use
                // of extras to determine the column order of the index.
                if (column != null) {
                    builder.addColumn(table.tableName(), m.columnName(), column.toBuilder());
                }
                if (m.hasExtras()) {
                    String order = m.extras().get("order");
                    if (order != null) {
                        Arrays.stream(order.split(","))
                                .map(table::getColumn)
                                .forEach(c -> builder.addColumn(table.tableName(), c.columnName(), c.toBuilder().index(true)));

                    }
                }
                break;
            }
            default:
                APLog.w(LOG_TAG, "Not handling update of type " + m.type() + "; this could cause the migration context to misrepresent the existing schema.");
        }
    }

    private void createTableMapIfNull() {
        if (tableMap == null) {
            tableMap = createTables();
        }
    }
}
