/*
   forsuredb, an object relational mapping tool

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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.PriorityQueue;

import static com.forsuredb.TestData.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class DiffGeneratorTest {

    private PriorityQueue<Migration> actualMigrationQueue;

    private TableContext migrationContext;
    private TableContext processingContext;
    private Migration[] orderedMigrationsExpected;

    public DiffGeneratorTest(TableContext migrationContext, TableContext processingContext, Migration[] orderedMigrationsExpected) {
        this.migrationContext = migrationContext;
        this.processingContext = processingContext;
        this.orderedMigrationsExpected = orderedMigrationsExpected;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // The processing context has a table that the migration context does not have--table has no extra columns
                {
                        newTableContext().build(),
                        newTableContext()
                                .addTable(table().build())
                                .build(),
                        new Migration[] {
                                Migration.builder().dbVersion(1)
                                        .type(Migration.Type.CREATE_TABLE)
                                        .tableInfo(table().build())
                                        .build()
                        }
                },
                // The processing context has a table that the migration context does not have--table has extra columns
                {
                        newTableContext().build(),
                        newTableContext().addTable(table().columnMap(columnMapOf(intCol().build()))
                                        .build())
                                .build(),
                        new Migration[] {
                                Migration.builder().dbVersion(1)
                                        .type(Migration.Type.CREATE_TABLE)
                                        .tableInfo(table().columnMap(columnMapOf(intCol().build()))
                                                .build())
                                        .build(),
                                Migration.builder().dbVersion(1)
                                        .type(Migration.Type.ALTER_TABLE_ADD_COLUMN)
                                        .tableInfo(table().columnMap(columnMapOf(intCol().build()))
                                                .build())
                                        .columnName(intCol().build().getColumnName())
                                        .build()
                        }
                },
                // The processing context has a non-unique, non foreign-key column that the migration context does not have
                {
                        newTableContext().addTable(table().build())
                                .build(),
                        newTableContext()
                                .addTable(table()
                                        .columnMap(columnMapOf(bigDecimalCol().build()))
                                        .build())
                                .build(),
                        new Migration[] {
                                Migration.builder().dbVersion(1)
                                        .type(Migration.Type.ALTER_TABLE_ADD_COLUMN)
                                        .tableInfo(table().columnMap(columnMapOf(bigDecimalCol().build()))
                                                .build())
                                        .columnName(bigDecimalCol().build().getColumnName())
                                        .build()
                        }
                },
                // The processing context has a foreign key the migration context does not know about (default delete and update actions)
                {
                        newTableContext().addTable(table().build()).build(),
                        newTableContext()
                                .addTable(table().columnMap(columnMapOf(longCol().foreignKeyInfo(cascadeFKI("user")
                                                        .build())
                                                .build()))
                                        .build())
                                .build(),
                        new Migration[] {
                                Migration.builder().dbVersion(1)
                                        .type(Migration.Type.ADD_FOREIGN_KEY_REFERENCE)
                                        .tableInfo(table().columnMap(columnMapOf(longCol().foreignKeyInfo(cascadeFKI("user")
                                                                .build())
                                                        .build()))
                                                .build())
                                        .columnName(longCol().build().getColumnName())
                                        .build()
                        }
                },
                // The processing context has a unique index column the migration context doesn't know about
                {
                        newTableContext().addTable(table().build())
                                .build(),
                        newTableContext()
                                .addTable(table().columnMap(columnMapOf(stringCol().unique(true)
                                                .build()))
                                        .build())
                                .build(),
                        new Migration[] {
                                Migration.builder().dbVersion(1)
                                        .type(Migration.Type.ALTER_TABLE_ADD_UNIQUE)
                                        .tableInfo(table().columnMap(columnMapOf(stringCol().unique(true)
                                                        .build()))
                                                .build())
                                        .columnName(stringCol().build().getColumnName())
                                        .build()
                        }
                },
                // The processing context has a unique index on a column the migration context knows about, but doesn't know is unique
                {
                        newTableContext().addTable(table().columnMap(columnMapOf(stringCol().build()))
                                        .build())
                                .build(),
                        newTableContext()
                                .addTable(table()
                                        .columnMap(columnMapOf(stringCol().unique(true)
                                                .build()))
                                        .build())
                                .build(),
                        new Migration[] {
                                Migration.builder().dbVersion(1)
                                        .type(Migration.Type.ADD_UNIQUE_INDEX)
                                        .tableInfo(table().columnMap(columnMapOf(stringCol().unique(true)
                                                        .build()))
                                                .build())
                                        .columnName(stringCol().build().getColumnName())
                                        .build()
                        }
                }
        });
    }

    @Before
    public void setUp() {
        actualMigrationQueue = new DiffGenerator(migrationContext, 0).analyzeDiff(processingContext);
    }

    @Test
    public void shouldMatchQueriesInOrderAndContent() {
        int i = 0;
        while (actualMigrationQueue.size() > 0) {
            assertEquals("migration index " + i, orderedMigrationsExpected[i], actualMigrationQueue.remove());
            i++;
        }
    }

}
