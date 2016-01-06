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
package com.forsuredb.annotationprocessor.generator;

import com.forsuredb.annotationprocessor.TableContext;
import com.forsuredb.annotationprocessor.info.ColumnInfo;
import com.forsuredb.annotationprocessor.info.TableInfo;
import com.forsuredb.annotationprocessor.util.APLog;
import com.forsuredb.migration.Migration;
import com.forsuredb.migration.MigrationSet;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

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
        for (TableInfo targetTable : targetContext.allTables()) {
            boolean tableCreateMigrationCreated = false;
            if (!sourceContext.hasTable(targetTable.getTableName())) {
                migrationQueue.add(Migration.builder().type(Migration.Type.CREATE_TABLE)
                        .tableName(targetTable.getTableName())
                        .build());
                tableCreateMigrationCreated = true;
            }

            TableInfo sourceTable = sourceContext.getTable(targetTable.getTableName());
            for (ColumnInfo targetColumn : nonDefaultColumnsIn(targetTable)) {
                if (tableCreateMigrationCreated || !sourceTable.hasColumn(targetColumn.getColumnName())) {
                    // if the TABLE_CREATE migration was added, then all non-default columns must be added as migrations
                    migrationQueue.add(addColumnMigrationFor(targetColumn, targetTable));
                    continue;
                }

                ColumnInfo sourceColumn = sourceTable.getColumn(targetColumn.getColumnName());
                if (!sourceColumn.isUnique() && targetColumn.isUnique()) {
                    migrationQueue.add(Migration.builder().type(Migration.Type.ADD_UNIQUE_INDEX)
                            .columnName(sourceColumn.getColumnName())
                            .tableName(targetTable.getTableName())
                            .build());
                }
            }
        }

        return MigrationSet.builder().dbVersion(sourceDbVersion + 1)
                .orderedMigrations(toList(migrationQueue))
                .targetSchema(targetContext.tableMap())
                .build();
    }

    private List<Migration> toList(PriorityQueue<Migration> migrationQueue) {
        List<Migration> retList = new LinkedList<>();
        while (migrationQueue.size() > 0) {
            retList.add(migrationQueue.remove());
        }
        return retList;
    }

    private Migration addColumnMigrationFor(ColumnInfo targetColumn, TableInfo targetTable) {
        return Migration.builder().columnName(targetColumn.getColumnName())
                .tableName(targetTable.getTableName())
                .type(targetColumn.isForeignKey() ? Migration.Type.ADD_FOREIGN_KEY_REFERENCE
                        : targetColumn.isUnique() ? Migration.Type.ALTER_TABLE_ADD_UNIQUE
                        : Migration.Type.ALTER_TABLE_ADD_COLUMN)
                .build();
    }

    private Set<ColumnInfo> nonDefaultColumnsIn(TableInfo targetTable) {
        Set<ColumnInfo> retSet = new HashSet<>();
        for (ColumnInfo targetColumn : targetTable.getColumns()) {
            if (TableInfo.DEFAULT_COLUMNS.containsKey(targetColumn.getColumnName())) {
                continue;
            }
            retSet.add(targetColumn);
        }
        return retSet;
    }
}
