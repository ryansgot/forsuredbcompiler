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
package com.fsryan.forsuredb.annotationprocessor.generator;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.migration.Migration;
import com.fsryan.forsuredb.migration.MigrationSet;
import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.annotationprocessor.util.APLog;
import com.fsryan.forsuredb.info.TableInfo;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import java.util.*;

// TODO: make this TableContext and MigrationContext agnostic.
/**
 * <p>
 *     Analyzes the diff between two {@link TableContext TableContext} objects--the one used to
 *     instantiate the DiffGenerator and the one passed to the
 *     {@link #analyzeDiff(TableContext) analyzeDiff(TableContext targetContext)} method.
 * </p>
 * @author Ryan Scott
 */
public class DiffGenerator {

    private static final String LOG_TAG = DiffGenerator.class.getSimpleName();
    private static final Gson gson = new Gson();
    private static final Map<String, ColumnInfo> TABLE_DEFAULT_COLUMNS = TableInfo.defaultColumns();

    private final TableContext sourceContext;
    private final int sourceDbVersion;

    /**
     * @param sourceContext should be the {@link TableContext} generated from the collection of
     *                 migration files, but there is no restriction on this.
     */
    public DiffGenerator(TableContext sourceContext, int sourceDbVersion) {
        this.sourceContext = sourceContext;
        this.sourceDbVersion = sourceDbVersion;
    }

    /**
     * <p>
     *     Anayzes the diff between this instance's {@link TableContext TableContext} and that of
     *     the argument
     * </p>
     * @param targetContext The {@link TableContext TableContext} that you would like to reach from
     *                      the {@link TableContext TableContext} member of this
     *                      {@link DiffGenerator DiffGenerator}
     * @return a priority queue of {@link Migration} that describes a sequence of migrations to
     * apply to migrate a database from the schema of this instance's {@link TableContext} to that
     * of the {@link TableContext TableContext} argument.
     */
    public MigrationSet analyzeDiff(TableContext targetContext) {
        APLog.i(LOG_TAG, "analyzing diff: targetContext.allTables().size() = " + targetContext.allTables().size());

        PriorityQueue<Migration> migrationQueue = new PriorityQueue<>();
        migrationQueue.addAll(additiveChanges(targetContext));
        migrationQueue.addAll(subtractiveChanges(targetContext));

        return MigrationSet.builder().dbVersion(sourceDbVersion + 1)
                .orderedMigrations(toList(migrationQueue))
                .targetSchema(targetContext.tableMap())
                .build();
    }

    private List<Migration> additiveChanges(TableContext targetContext) {
        List<Migration> retList = new ArrayList<>();
        for (TableInfo targetTable : targetContext.allTables()) {
            if (!sourceContext.hasTableWithName(targetTable.tableName())) {
                retList.add(Migration.builder().type(Migration.Type.CREATE_TABLE)
                        .tableName(targetTable.tableName())
                        .build());
                // if the TABLE_CREATE migration was added, then all non-default, non-unique columns must be added
                for (ColumnInfo targetColumn : nonDefaultColumnsIn(targetTable, true)) {
                    if (targetTable.isForeignKeyColumn(targetColumn.getColumnName())) {
                        continue;
                    }
                    retList.add(addMigrationForNewColumn(targetColumn, targetTable));
                }
                continue;
            }

            TableInfo sourceTable = sourceContext.getTableByName(targetTable.tableName());
            if (sourceTable != null && !sourceTable.getPrimaryKey().equals(targetTable.getPrimaryKey())) {
                retList.add(Migration.builder().type(Migration.Type.UPDATE_PRIMARY_KEY)
                        .extras(ImmutableMap.of("existing_column_names", gson.toJson(columnNamesOf(sourceTable))))
                        .tableName(targetTable.tableName())
                        .build());
            }

            final boolean updatingForeignKeys = addUpdateForeignKeysMigration(retList, sourceTable, targetTable);
            for (ColumnInfo targetColumn : nonDefaultColumnsIn(targetTable, updatingForeignKeys)) {
                if (!sourceTable.hasColumn(targetColumn.getColumnName())) {
                    retList.add(addMigrationForNewColumn(targetColumn, targetTable));
                    continue;
                }

                ColumnInfo sourceColumn = sourceTable.getColumn(targetColumn.getColumnName());
                retList.addAll(getExistingColumnMigration(sourceColumn, targetColumn, targetTable.tableName()));
            }
        }
        return retList;
    }

    private List<Migration> getExistingColumnMigration(ColumnInfo sourceColumn, ColumnInfo targetColumn, String tableName) {
        List<Migration> ret = new ArrayList<>();
        if (!sourceColumn.index() && !sourceColumn.unique() && targetColumn.index() && targetColumn.unique()) {
            ret.add(Migration.builder().type(Migration.Type.ADD_UNIQUE_INDEX)
                    .columnName(sourceColumn.getColumnName())
                    .tableName(tableName)
                    .build());
        } else if (!sourceColumn.unique() && targetColumn.unique()) {
            ret.add(Migration.builder().type(Migration.Type.MAKE_COLUMN_UNIQUE)
                    .columnName(sourceColumn.getColumnName())
                    .tableName(tableName)
                    .build());
        } else if (!sourceColumn.index() && targetColumn.index()) {
            ret.add(Migration.builder().type(Migration.Type.ADD_INDEX)
                    .columnName(sourceColumn.getColumnName())
                    .tableName(tableName)
                    .build());
        }

        // return early if neither source nor target has default value
        if (!sourceColumn.hasDefaultValue() && !targetColumn.hasDefaultValue()) {
            return ret;
        }

        if ((sourceColumn.hasDefaultValue() && !sourceColumn.defaultValue().equals(targetColumn.defaultValue()))
                || (targetColumn.hasDefaultValue() && !targetColumn.defaultValue().equals(sourceColumn.defaultValue()))) {
            ret.add(Migration.builder().type(Migration.Type.CHANGE_DEFAULT_VALUE)
                    .columnName(sourceColumn.getColumnName())
                    .tableName(tableName)
                    .build());
        }

        return ret;
    }

    private boolean addUpdateForeignKeysMigration(List<Migration> retList, TableInfo sourceTable, TableInfo targetTable) {
        if (sourceTable == null || sourceTable.foreignKeys().equals(targetTable.foreignKeys())) {
            return false;
        }

        retList.add(Migration.builder().type(Migration.Type.UPDATE_FOREIGN_KEYS)
                .tableName(targetTable.tableName())
                .extras(new ImmutableMap.Builder<String, String>()
                        .put("existing_column_names", gson.toJson(columnNamesOf(sourceTable)))
                        .put("current_foreign_keys", gson.toJson(sourceTable.foreignKeys()))
                        .build())
                .build());
        return true;
    }

    private List<Migration> toList(PriorityQueue<Migration> migrationQueue) {
        List<Migration> retList = new LinkedList<>();
        while (migrationQueue.size() > 0) {
            retList.add(migrationQueue.remove());
        }
        return retList;
    }

    private Migration addMigrationForNewColumn(ColumnInfo targetColumn, TableInfo targetTable) {
        return Migration.builder().columnName(targetColumn.getColumnName())
                .tableName(targetTable.tableName())
                .type(targetColumn.isForeignKey() ? Migration.Type.ADD_FOREIGN_KEY_REFERENCE
                        : targetColumn.unique() ? Migration.Type.ALTER_TABLE_ADD_UNIQUE
                        : Migration.Type.ALTER_TABLE_ADD_COLUMN)
                .build();
    }

    private Set<ColumnInfo> nonDefaultColumnsIn(TableInfo targetTable, boolean filterForeignKeys) {
        Set<ColumnInfo> retSet = new HashSet<>();
        for (ColumnInfo targetColumn : filterForeignKeys ? targetTable.getNonForeignKeyColumns() : targetTable.getColumns()) {
            if (TABLE_DEFAULT_COLUMNS.containsKey(targetColumn.getColumnName())) {
                continue;
            }
            retSet.add(targetColumn);
        }
        return retSet;
    }

    private List<Migration> subtractiveChanges(TableContext targetContext) {
        List<Migration> retList = new ArrayList<>();
        for (TableInfo sourceTable : sourceContext.allTables()) {
            if (!targetContext.hasTableWithName(sourceTable.tableName())) {
                retList.add(Migration.builder().type(Migration.Type.DROP_TABLE)
                        .tableName(sourceTable.tableName())
                        .build());
                continue;
            }

//            TableInfo sourceTable = sourceContext.getTableByName(targetTable.tableName());
//            for (ColumnInfo targetColumn : nonDefaultColumnsIn(targetTable)) {
//                if (tableCreateMigrationCreated || !sourceTable.hasColumn(targetColumn.getColumnName())) {
//                    // if the TABLE_CREATE migration was added, then all non-default columns must be added as migrations
//                    retList.add(addMigrationForNewColumn(targetColumn, targetTable));
//                    continue;
//                }
//
//                ColumnInfo sourceColumn = sourceTable.getColumn(targetColumn.getColumnName());
//                if (!sourceColumn.unique() && targetColumn.unique()) {
//                    retList.add(Migration.builder().type(Migration.Type.ADD_UNIQUE_INDEX)
//                            .columnName(sourceColumn.getColumnName())
//                            .tableName(targetTable.tableName())
//                            .build());
//                }
//            }
        }
        return retList;
    }

    private static Set<String> columnNamesOf(TableInfo table) {
        Set<String> ret = new HashSet<>();
        for (ColumnInfo columnInfo : table.getColumns()) {
            ret.add(columnInfo.getColumnName());
        }
        return ret;
    }
}
