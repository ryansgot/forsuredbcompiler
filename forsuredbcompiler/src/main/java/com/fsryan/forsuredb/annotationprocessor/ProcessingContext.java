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
import com.fsryan.forsuredb.info.JoinInfo;
import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.annotationprocessor.util.APLog;
import com.fsryan.forsuredb.api.FSGetApi;

import java.util.*;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.Modifier;

/**
 * <p>
 *     This is the TableContext that corresponds to the currently defined extensions of the
 *     {@link FSGetApi FSGetApi} interface annotated with the {@link com.fsryan.forsuredb.annotations.FSTable FSTable} annotation.
 * </p>
 * @author Ryan Scott
 */
public class ProcessingContext implements TableContext {

    private static final String LOG_TAG = ProcessingContext.class.getSimpleName();

    private final Set<TypeElement> tableTypes = new HashSet<>();
    private Map<String, TableInfo> tableMap;
    private List<JoinInfo> joins;

    public ProcessingContext(Set<TypeElement> tableTypes) {
        this.tableTypes.addAll(tableTypes);
    }

    @Override
    public Collection<TableInfo> allTables() {
        createTableMapIfNecessary();
        return tableMap.values();
    }

    @Override
    public Map<String, TableInfo> tableMap() {
        createTableMapIfNecessary();
        return tableMap;
    }

    @Override
    public boolean hasTable(String tableName) {
        createTableMapIfNecessary();
        return tableName != null && tableMap.containsKey(tableName);
    }

    @Override
    public TableInfo getTable(String tableName) {
        createTableMapIfNecessary();
        return tableName == null ? null : tableMap.get(tableName);
    }

    //TODO: @Override
    public List<JoinInfo> allJoins() {
        createTableMapIfNecessary();
        return joins;
    }

    private static TableInfo matchingTable(String qualifiedClassName, List<TableInfo> allTables) {
        for (TableInfo table : allTables) {
            if (!qualifiedClassName.equals(table.qualifiedClassName())) {
                continue;
            }
            return table;
        }
        throw new IllegalStateException("could not find table for class name: " + qualifiedClassName);
    }

    private void createTableMapIfNecessary() {
        if (tableMap != null) {
            return;
        }
        joins = new LinkedList<>();

        tableMap = new HashMap<>();
        List<TableInfo> allTables = gatherInitialInfo();
        for (TableInfo table : allTables) {
            for (TableForeignKeyInfo foreignKey : table.foreignKeys()) {
                final TableInfo foreignTable = matchingTable(foreignKey.getForeignTableApiClassName(), allTables);
                foreignKey.setForeignTableApiClassName(foreignTable.tableName());
            }

            // TODO: remove this loop when foreign key information no longer stored on columns
            for (ColumnInfo column : table.getColumns()) {
                column.enrichWithForeignTableInfoFrom(allTables);
            }
            tableMap.put(table.tableName(), table);
            APLog.i(LOG_TAG, "created table: " + table.toString());
        }

        for (TableInfo table : tableMap.values()) {
            for (TableForeignKeyInfo foreignKey : table.foreignKeys()) {
                TableInfo parent = tableMap.get(foreignKey.foreignTableName());
                List<ColumnInfo> childColumns = new ArrayList<>();
                List<ColumnInfo> parentColumns = new ArrayList<>();
                for (Map.Entry<String, String> entry : foreignKey.localToForeignColumnMap().entrySet()) {
                    childColumns.add(table.getColumn(entry.getKey()));
                    parentColumns.add(parent.getColumn(entry.getValue()));
                }
                JoinInfo join = JoinInfo.builder().childTable(table)
                        .childColumns(childColumns)
                        .parentTable(parent)
                        .parentColumns(parentColumns)
                        .build();
                joins.add(join);
                APLog.i(LOG_TAG, "found join: " + join.toString());
            }
        }
    }

    private List<TableInfo> gatherInitialInfo() {
        List<TableInfo> ret = new ArrayList<>();
        for (TypeElement te : tableTypes) {
            if (!isNonPrivateInterface(te)) {
                continue;   // <-- only process interfaces that are non-private
            }

            ret.add(TableInfoFactory.create(te));
        }

        return ret;
    }

    private boolean isNonPrivateInterface(TypeElement te) {
        return te.getKind() == ElementKind.INTERFACE && !te.getModifiers().contains(Modifier.PRIVATE);
    }
}
