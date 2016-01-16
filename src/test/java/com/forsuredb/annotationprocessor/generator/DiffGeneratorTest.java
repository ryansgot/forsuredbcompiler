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
import com.forsuredb.migration.Migration;

import com.forsuredb.migration.MigrationSet;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.forsuredb.TestData.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class DiffGeneratorTest {

    private MigrationSet actualMigrationSet;

    private final int sourceDbVersion;
    private final TableContext migrationContext;
    private final TableContext processingContext;
    private final MigrationSet expectedMigrationSet;

    public DiffGeneratorTest(int sourceDbVersion,
                             TableContext migrationContext,
                             TableContext processingContext,
                             MigrationSet expectedMigrationSet) {
        this.sourceDbVersion = sourceDbVersion;
        this.migrationContext = migrationContext;
        this.processingContext = processingContext;
        this.expectedMigrationSet = expectedMigrationSet;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                // No diff (identical contexts)
                {
                        4,
                        newTableContext().addTable(table().columnMap(columnMapOf(intCol().build(), stringCol().build()))
                                        .build())
                                .build(),
                        newTableContext().addTable(table().columnMap(columnMapOf(intCol().build(), stringCol().build()))
                                        .build())
                                .build(),
                        MigrationSet.builder().dbVersion(5)
                                .orderedMigrations(new ArrayList<Migration>())
                                .build()
                },
                // The processing context has a table that the migration context does not have--table has no extra columns
                {
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
                // The processing context has a table that the migration context does not have--table has extra columns
                {
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
                                                .columnName(stringCol().build().getColumnName())
                                                .build(),
                                        Migration.builder().type(Migration.Type.ALTER_TABLE_ADD_COLUMN)
                                                .tableName(TABLE_NAME)
                                                .columnName(intCol().build().getColumnName())
                                                .build()))
                                .targetSchema(tableMapOf(table().columnMap(columnMapOf(intCol().build(), stringCol().build()))
                                .build()))
                                .build()
                },
                // The processing context has a non-unique, non foreign-key column that the migration context does not have
                {
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
                // The processing context has a foreign key the migration context does not know about (default delete and update actions)
                {
                        2,
                        newTableContext().addTable(table().build()).build(),
                        newTableContext()
                                .addTable(table().columnMap(columnMapOf(longCol().foreignKeyInfo(cascadeFKI("user")
                                                        .build())
                                                .build()))
                                        .build())
                                .build(),
                        MigrationSet.builder().dbVersion(3)
                                .orderedMigrations(Lists.newArrayList(Migration.builder().type(Migration.Type.ADD_FOREIGN_KEY_REFERENCE)
                                        .tableName(TABLE_NAME)
                                        .columnName(longCol().build().getColumnName())
                                        .build()))
                                .targetSchema(tableMapOf(table().columnMap(columnMapOf(longCol().foreignKeyInfo(cascadeFKI("user")
                                                        .build())
                                                .build()))
                                        .build()))
                                .build()
                },
                // The processing context has a unique index column the migration context doesn't know about
                {
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
                // The processing context has a unique index on a column the migration context knows about, but doesn't know is unique
                {
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
                                .orderedMigrations(Lists.newArrayList(Migration.builder().type(Migration.Type.ADD_UNIQUE_INDEX)
                                        .tableName(TABLE_NAME)
                                        .columnName(stringCol().build().getColumnName())
                                        .build()))
                                .targetSchema(tableMapOf(table().columnMap(columnMapOf(stringCol().unique(true)
                                                .build()))
                                        .build()))
                                .build()
                },
                // The processing does not have a table the migration context knows about (a table deletion)
                {
                        47,
                        newTableContext()
                                .addTable(table()
                                        .tableName("table_1")
                                        .columnMap(columnMapOf(
                                                idCol(),
                                                modifiedCol(),
                                                createdCol(),
                                                deletedCol(),
                                                stringCol().columnName("table_1_string").build()))
                                        .build())
                                .addTable(table()
                                        .tableName("table_2")
                                        .columnMap(columnMapOf(
                                                idCol(),
                                                modifiedCol(),
                                                createdCol(),
                                                deletedCol(),
                                                stringCol().columnName("table_2_string").build()))
                                        .build())
                                .build(),
                        newTableContext()
                                .addTable(table()
                                        .tableName("table_2")
                                        .columnMap(columnMapOf(
                                                idCol(),
                                                modifiedCol(),
                                                createdCol(),
                                                deletedCol(),
                                                stringCol().columnName("table_2_string").build()))
                                        .build())
                                .build(),
                        MigrationSet.builder().dbVersion(48)
                                .orderedMigrations(Lists.newArrayList(Migration.builder()
                                        .type(Migration.Type.DROP_TABLE)
                                        .tableName("table_1")
                                        .build()))
                                .targetSchema(tableMapOf(table()
                                        .tableName("table_2")
                                        .columnMap(columnMapOf(
                                                idCol(),
                                                modifiedCol(),
                                                createdCol(),
                                                deletedCol(),
                                                stringCol().columnName("table_2_string").build()))
                                        .build()))
                                .build()
                }
        });
    }

    @Before
    public void setUp() {
        actualMigrationSet = new DiffGenerator(migrationContext, sourceDbVersion).analyzeDiff(processingContext);
    }

    @Test
    public void shouldHaveCorrectTargetDbVersion() {
        assertEquals(expectedMigrationSet.getDbVersion(), actualMigrationSet.getDbVersion());
    }

    @Test
    public void shouldHaveCorrectNumberOfMigrations() {
        assertEquals(expectedMigrationSet.getOrderedMigrations().size(), actualMigrationSet.getOrderedMigrations().size());
    }

    @Test
    public void shouldMatchMigrationsInOrderAndContent() {
        final List<Migration> expected = expectedMigrationSet.getOrderedMigrations();
        final List<Migration> actual = actualMigrationSet.getOrderedMigrations();
        for (int i = 0; i < expected.size(); i++) {
            assertEquals("migration index " + i, expected.get(i), actual.get(i));
        }
    }

    @Test
    public void shouldContainTargetContext() {
        assertEquals(processingContext.tableMap(), actualMigrationSet.getTargetSchema());
    }
}
