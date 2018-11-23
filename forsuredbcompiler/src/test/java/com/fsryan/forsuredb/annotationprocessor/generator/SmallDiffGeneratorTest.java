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

import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.info.TableIndexInfo;
import com.fsryan.forsuredb.migration.Migration;
import com.fsryan.forsuredb.migration.MigrationSet;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;

import static com.fsryan.forsuredb.TestData.*;
import static com.fsryan.forsuredb.test.tools.CollectionUtil.mapOf;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class SmallDiffGeneratorTest extends BaseDiffGeneratorTest {

    public SmallDiffGeneratorTest(int sourceDbVersion, TableContext migrationContext, TableContext processingContext, MigrationSet expectedMigrationSet) {
        super(sourceDbVersion, migrationContext, processingContext, expectedMigrationSet);
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {   // 00 No diff (identical contexts)
                        4,
                        newTableContext().addTable(table().columnMap(columnMapOf(intCol().build(), stringCol().build()))
                                        .build())
                                .build(),
                        newTableContext().addTable(table().columnMap(columnMapOf(intCol().build(), stringCol().build()))
                                        .build())
                                .build(),
                        MigrationSet.builder().dbVersion(5)
                                .targetSchema(tableMapOf(table().columnMap(columnMapOf(intCol().build(), stringCol().build()))
                                        .build()))
                                .orderedMigrations(new ArrayList<>())
                                .build()
                },
                {   // 01 The processing context has a table that the migration context does not have--table has no extra columns
                        1,
                        newTableContext().build(),
                        newTableContext().addTable(table().build())
                                .build(),
                        MigrationSet.builder().dbVersion(2)
                                .orderedMigrations(Lists.newArrayList(Migration.builder().type(Migration.Type.CREATE_TABLE)
                                        .tableName(TABLE_NAME)
                                        .build()))
                                .targetSchema(tableMapOf(table().build()))
                                .build()
                },
                {   // 02 The processing context has a table that the migration context does not have--table has extra columns
                        10,
                        newTableContext().build(),
                        newTableContext().addTable(table().columnMap(columnMapOf(intCol().build(), stringCol().build()))
                                        .build())
                                .build(),
                        MigrationSet.builder().dbVersion(11)
                                .orderedMigrations(Lists.newArrayList(Migration.builder().type(Migration.Type.CREATE_TABLE)
                                                .tableName(TABLE_NAME)
                                                .build(),
                                        Migration.builder().type(Migration.Type.ALTER_TABLE_ADD_COLUMN)
                                                .tableName(TABLE_NAME)
                                                .columnName(intCol().build().getColumnName())
                                                .build(),
                                        Migration.builder().type(Migration.Type.ALTER_TABLE_ADD_COLUMN)
                                                .tableName(TABLE_NAME)
                                                .columnName(stringCol().build().getColumnName())
                                                .build()))
                                .targetSchema(tableMapOf(table().columnMap(columnMapOf(intCol().build(), stringCol().build()))
                                .build()))
                                .build()
                },
                {   // 03 The processing context has a non-unique, non foreign-key column that the migration context does not have
                        3,
                        newTableContext().addTable(table().build())
                                .build(),
                        newTableContext().addTable(table()
                                        .columnMap(columnMapOf(bigDecimalCol().build()))
                                        .build())
                                .build(),
                        MigrationSet.builder().dbVersion(4)
                                .orderedMigrations(Lists.newArrayList(Migration.builder().type(Migration.Type.ALTER_TABLE_ADD_COLUMN)
                                        .tableName(TABLE_NAME)
                                        .columnName(bigDecimalCol().build().getColumnName())
                                        .build()))
                                .targetSchema(tableMapOf(table()
                                        .columnMap(columnMapOf(bigDecimalCol().build()))
                                        .build()))
                                .build()
                },
                {   // 04 The processing context has a foreign key the migration context does not know about (default delete and update actions)
                        2,
                        newTableContext().addTable(table().build()).build(),
                        newTableContext()
                                .addTable(table().columnMap(columnMapOf(longCol().foreignKeyInfo(cascadeFKI("user").apiClassName("")
                                                        .build())
                                                .build()))
                                        .build())
                                .build(),
                        MigrationSet.builder().dbVersion(3)
                                .orderedMigrations(Lists.newArrayList(Migration.builder().type(Migration.Type.UPDATE_FOREIGN_KEYS)
                                        .tableName(TABLE_NAME)
                                        .extras(new ImmutableMap.Builder<String, String>()
                                                .put("existing_column_names", "[\"deleted\",\"created\",\"modified\",\"_id\"]")
                                                .put("current_foreign_keys", "[]")
                                                .build())
                                        .build()))
                                .targetSchema(tableMapOf(table().columnMap(columnMapOf(longCol().foreignKeyInfo(cascadeFKI("user")
                                                        .build())
                                                .build()))
                                        .build()))
                                .build()
                },
                {   // 05 The processing context has a unique index column the migration context doesn't know about
                        4364,
                        newTableContext().addTable(table().build())
                                .build(),
                        newTableContext()
                                .addTable(table().columnMap(columnMapOf(stringCol().unique(true)
                                                .build()))
                                        .build())
                                .build(),
                        MigrationSet.builder().dbVersion(4365)
                                .orderedMigrations(Lists.newArrayList(Migration.builder().type(Migration.Type.ALTER_TABLE_ADD_UNIQUE)
                                        .tableName(TABLE_NAME)
                                        .columnName(stringCol().build().getColumnName())
                                        .build()))
                                .targetSchema(tableMapOf(table().columnMap(columnMapOf(stringCol().unique(true)
                                                .build()))
                                        .build()))
                                .build()
                },
                {   // 06 The processing context has a unique index on a column the migration context knows about, but doesn't know is unique
                        8,
                        newTableContext().addTable(table().columnMap(columnMapOf(stringCol().build()))
                                        .build())
                                .build(),
                        newTableContext()
                                .addTable(table().columnMap(columnMapOf(stringCol().unique(true)
                                                .build()))
                                        .build())
                                .build(),
                        MigrationSet.builder().dbVersion(9)
                                .orderedMigrations(Lists.newArrayList(Migration.builder().type(Migration.Type.MAKE_COLUMN_UNIQUE)
                                        .tableName(TABLE_NAME)
                                        .columnName(stringCol().build().getColumnName())
                                        .build()))
                                .targetSchema(tableMapOf(table().columnMap(columnMapOf(stringCol().unique(true)
                                                .build()))
                                        .build()))
                                .build()
                },
                {   // 07 The processing does not have a table the migration context knows about (a table deletion)
                        47,
                        newTableContext()
                                .addTable(table()
                                        .tableName("table_1")
                                        .columnMap(baseColumnMapBuilder()
                                                .put("table_1_string", stringCol().columnName("table_1_string").build())
                                                .build())
                                        .build())
                                .addTable(table()
                                        .tableName("table_2")
                                        .columnMap(baseColumnMapBuilder()
                                                .put("table_2_string", stringCol().columnName("table_2_string").build())
                                                .build())
                                        .build())
                                .build(),
                        newTableContext()
                                .addTable(table()
                                        .tableName("table_2")
                                        .columnMap(baseColumnMapBuilder()
                                                .put("table_2_string", stringCol().columnName("table_2_string").build())
                                                .build())
                                        .build())
                                .build(),
                        MigrationSet.builder().dbVersion(48)
                                .orderedMigrations(Lists.newArrayList(Migration.builder()
                                        .type(Migration.Type.DROP_TABLE)
                                        .tableName("table_1")
                                        .build()))
                                .targetSchema(tableMapOf(table()
                                        .tableName("table_2")
                                        .columnMap(baseColumnMapBuilder()
                                                .put("table_2_string", stringCol().columnName("table_2_string").build())
                                                .build())
                                        .build()))
                                .build()
                },
                {   // 08: add non-unique index to existing column
                        1,
                        newTableContext()
                                .addTable(table()
                                        .columnMap(baseColumnMapBuilder()
                                                .put(longCol().build().getColumnName(), longCol().build())
                                                .build())
                                        .build())
                                .build(),
                        newTableContext()
                                .addTable(table()
                                        .columnMap(baseColumnMapBuilder()
                                                .put(longCol().build().getColumnName(), longCol().index(true).build())
                                                .build())
                                        .build())
                                .build(),
                        MigrationSet.builder()
                                .dbVersion(2)
                                .orderedMigrations(Lists.newArrayList(
                                        Migration.builder()
                                                .type(Migration.Type.ADD_INDEX)
                                                .tableName(TABLE_NAME)
                                                .columnName(longCol().build().getColumnName())
                                                .build()))
                                .targetSchema(tableMapOf(table()
                                        .columnMap(baseColumnMapBuilder()
                                                .put(longCol().build().getColumnName(), longCol().index(true).build())
                                                .build())
                                        .build()))
                                .build()
                },
                {   // 09: add new column that is a non-unique index
                        1,
                        newTableContext()
                                .addTable(table()
                                        .columnMap(baseColumnMapBuilder().build())
                                        .build())
                                .build(),
                        newTableContext()
                                .addTable(table()
                                        .columnMap(baseColumnMapBuilder()
                                                .put(longCol().build().getColumnName(), longCol().index(true).build())
                                                .build())
                                        .build())
                                .build(),
                        MigrationSet.builder()
                                .dbVersion(2)
                                .orderedMigrations(Lists.newArrayList(
                                        Migration.builder()
                                                .type(Migration.Type.ALTER_TABLE_ADD_COLUMN)
                                                .tableName(TABLE_NAME)
                                                .columnName(longCol().build().getColumnName())
                                                .build()))
                                .targetSchema(tableMapOf(table()
                                        .columnMap(baseColumnMapBuilder()
                                                .put(longCol().build().getColumnName(), longCol().index(true).build())
                                                .build())
                                        .build()))
                                .build()
                },
                {   // 10: make an existing column a unique index
                        1,
                        newTableContext()
                                .addTable(table()
                                        .columnMap(baseColumnMapBuilder()
                                                .put(longCol().build().getColumnName(), longCol().build())
                                                .build())
                                        .build())
                                .build(),
                        newTableContext()
                                .addTable(table()
                                        .columnMap(baseColumnMapBuilder()
                                                .put(longCol().build().getColumnName(), longCol().unique(true).index(true).build())
                                                .build())
                                        .build())
                                .build(),
                        MigrationSet.builder()
                                .dbVersion(2)
                                .orderedMigrations(Lists.newArrayList(
                                        Migration.builder()
                                                .type(Migration.Type.ADD_UNIQUE_INDEX)
                                                .tableName(TABLE_NAME)
                                                .columnName(longCol().build().getColumnName())
                                                .build()))
                                .targetSchema(tableMapOf(table()
                                        .columnMap(baseColumnMapBuilder()
                                                .put(longCol().build().getColumnName(), longCol().index(true).build())
                                                .build())
                                        .build()))
                                .build()
                },
                {   // 11: add a new column that is a unique index
                        1,
                        newTableContext()
                                .addTable(table()
                                        .columnMap(baseColumnMapBuilder().build())
                                        .build())
                                .build(),
                        newTableContext()
                                .addTable(table()
                                        .columnMap(baseColumnMapBuilder()
                                                .put(longCol().build().getColumnName(), longCol().unique(true).index(true).build())
                                                .build())
                                        .build())
                                .build(),
                        MigrationSet.builder()
                                .dbVersion(2)
                                .orderedMigrations(Lists.newArrayList(
                                        Migration.builder()
                                                .type(Migration.Type.ALTER_TABLE_ADD_UNIQUE)
                                                .tableName(TABLE_NAME)
                                                .columnName(longCol().build().getColumnName())
                                                .build()))
                                .targetSchema(tableMapOf(table()
                                        .columnMap(baseColumnMapBuilder()
                                                .put(longCol().build().getColumnName(), longCol().index(true).build())
                                                .build())
                                        .build()))
                                .build()
                },
                {   // 12: change the primary key of a table
                        1,
                        newTableContext()
                                .addTable(table()
                                        .columnMap(baseColumnMapBuilder()
                                                .put(longCol().build().getColumnName(), longCol().primaryKey(false).build())
                                                .build())
                                        .build())
                                .build(),
                        newTableContext()
                                .addTable(table()
                                        .columnMap(baseColumnMapBuilder()
                                                .put(longCol().build().getColumnName(), longCol().primaryKey(true).build())
                                                .build())
                                        .build())
                                .build(),
                        MigrationSet.builder()
                                .dbVersion(2)
                                .orderedMigrations(Lists.newArrayList(
                                        Migration.builder()
                                                .type(Migration.Type.UPDATE_PRIMARY_KEY)
                                                .extras(ImmutableMap.of("existing_column_names", "[\"deleted\",\"created\",\"modified\",\"long_column\",\"_id\"]"))
                                                .tableName(TABLE_NAME)
                                                .build()))
                                .targetSchema(tableMapOf(table()
                                        .columnMap(baseColumnMapBuilder()
                                                .put(longCol().build().getColumnName(), longCol().primaryKey(true).build())
                                                .build())
                                        .build()))
                                .build()
                },
                {   // 13: same as 04, but done via TableForeignKeyInfo instead of foreign key info on columns
                        2,
                        newTableContext().addTable(table().build()).build(),
                        newTableContext()
                                .addTable(table().columnMap(columnMapOf(longCol().build()))
                                        .foreignKeys(Sets.newHashSet(
                                                tableForeignKeyInfoBuilder()
                                                        .foreignTableName("user")
                                                        .updateChangeAction("CASCADE")
                                                        .deleteChangeAction("CASCADE")
                                                        .localToForeignColumnMap(ImmutableMap.of("long_column", "_id"))
                                                        .build()
                                        ))
                                        .build())
                                .build(),
                        MigrationSet.builder().dbVersion(3)
                                .orderedMigrations(Lists.newArrayList(Migration.builder().type(Migration.Type.UPDATE_FOREIGN_KEYS)
                                        .tableName(TABLE_NAME)
                                        .extras(new ImmutableMap.Builder<String, String>()
                                                .put("existing_column_names", "[\"deleted\",\"created\",\"modified\",\"_id\"]")
                                                .put("current_foreign_keys", "[]")
                                                .build())
                                        .build()))
                                .targetSchema(tableMapOf(table().columnMap(columnMapOf(longCol().foreignKeyInfo(cascadeFKI("user")
                                        .build())
                                        .build()))
                                        .build()))
                                .build()
                },
                {   // 14: same as 13, but transitions an existing column to being a foreign key
                        2,
                        newTableContext().addTable(table().columnMap(columnMapOf(longCol().build())).build()).build(),
                        newTableContext()
                                .addTable(table().columnMap(columnMapOf(longCol().build()))
                                        .foreignKeys(Sets.newHashSet(
                                                tableForeignKeyInfoBuilder()
                                                        .foreignTableName("user")
                                                        .updateChangeAction("CASCADE")
                                                        .deleteChangeAction("CASCADE")
                                                        .localToForeignColumnMap(ImmutableMap.of("long_column", "_id"))
                                                        .build()
                                        ))
                                        .build())
                                .build(),
                        MigrationSet.builder().dbVersion(3)
                                .orderedMigrations(Lists.newArrayList(Migration.builder().type(Migration.Type.UPDATE_FOREIGN_KEYS)
                                        .tableName(TABLE_NAME)
                                        .extras(new ImmutableMap.Builder<String, String>()
                                                .put("existing_column_names", "[\"deleted\",\"created\",\"modified\",\"long_column\",\"_id\"]")
                                                .put("current_foreign_keys", "[]")
                                                .build())
                                        .build()))
                                .targetSchema(tableMapOf(table().columnMap(columnMapOf(longCol().foreignKeyInfo(cascadeFKI("user")
                                        .build())
                                        .build()))
                                        .build()))
                                .build()
                },
                {   // 15: change the default value of an existing column
                        2,
                        newTableContext()
                                .addTable(defaultPkTable("test1", longCol().build()).build())
                                .build(),
                        newTableContext()
                                .addTable(defaultPkTable("test1", longCol().defaultValue("12").build()).build())
                                .build(),
                        MigrationSet.builder()
                                .dbVersion(3)
                                .orderedMigrations(Arrays.asList(
                                        changeDefaultValueMigration("test1")
                                                .columnName(longCol().build().getColumnName())
                                                .build()
                                ))
                                .targetSchema(tableMapOf(
                                        defaultPkTable("test1")
                                                .addToColumns(longCol().defaultValue("12").build())
                                                .build()
                                        )
                                )
                                .build()
                },
                {   // 16: change the default value of an existing column and make it a unique index
                        2,
                        newTableContext()
                                .addTable(defaultPkTable("test1", longCol().build()).build())
                                .build(),
                        newTableContext()
                                .addTable(defaultPkTable("test1", longCol().unique(true).index(true).defaultValue("12").build()).build())
                                .build(),
                        MigrationSet.builder()
                                .dbVersion(3)
                                .orderedMigrations(Arrays.asList(
                                        changeDefaultValueMigration("test1")
                                                .columnName(longCol().build().getColumnName())
                                                .build(),
                                        addUniqueIndexMigration("test1")
                                                .columnName(longCol().build().getColumnName())
                                                .build()
                                ))
                                .targetSchema(tableMapOf(
                                        defaultPkTable("test1")
                                                .addToColumns(longCol().defaultValue("12").build())
                                                .build()
                                        )
                                )
                                .build()
                },
//                {   // 17: add a composite index with two new columns
//                        2,
//                        newTableContext()
//                                .addTable(defaultPkTable("test1").build())
//                                .build(),
//                        newTableContext()
//                                .addTable(defaultPkTable("test1")
//                                        .addToColumns(longCol().columnName("index_col_1").index(true).build())
//                                        .addToColumns(longCol().columnName("index_col_2").index(true).build())
//                                        .addIndex(TableIndexInfo.create(false, Arrays.asList("index_col_1", "index_col_2"), Arrays.asList("", "")))
//                                        .build()
//                                ).build(),
//                        MigrationSet.builder()
//                                .dbVersion(3)
//                                .orderedMigrations(Arrays.asList(
//                                        addIndexMigration("test1")
//                                                .extras(mapOf("order", "index_col_1,index_col_2"))
//                                                .build()
//                                )).targetSchema(tableMapOf(
//                                        defaultPkTable("test1")
//                                                .addToColumns(longCol().columnName("index_col_1").index(true).build())
//                                                .addToColumns(longCol().columnName("index_col_2").index(true).build())
//                                                .addIndex(TableIndexInfo.create(false, Arrays.asList("index_col_1", "index_col_2"), Arrays.asList("", "")))
//                                                .build()))
//                                .build()
//                }
        });
    }

    private static TableForeignKeyInfo.Builder tableForeignKeyInfoBuilder() {
        return TableForeignKeyInfo.builder()
                .foreignTableApiClassName(SmallDiffGeneratorTest.class.getSimpleName());
    }

    @Test
    public void shouldHaveCorrectDbVersion() {
        assertEquals(expectedMigrationSet.dbVersion(), actualMigrationSet.dbVersion());
    }
}
