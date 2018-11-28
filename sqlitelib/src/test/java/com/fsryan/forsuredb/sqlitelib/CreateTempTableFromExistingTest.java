/*
   forsuredbsqlitelib, sqlite library for the forsuredb project

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
package com.fsryan.forsuredb.sqlitelib;

import com.fsryan.forsuredb.api.migration.QueryGenerator;
import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableInfo;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static com.fsryan.forsuredb.info.DBInfoFixtures.*;
import static com.fsryan.forsuredb.sqlitelib.TestData.TABLE_NAME;

@RunWith(Parameterized.class)
public class CreateTempTableFromExistingTest extends BaseSQLiteGeneratorTest {

    private CreateTempTableFromExisting generatorUnderTest;

    private TableInfo table;
    private ColumnInfo[] excludedColumns;

    public CreateTempTableFromExistingTest(TableInfo table, ColumnInfo[] excludedColumns, String[] expectedSql) {
        super(expectedSql);
        this.table = table;
        this.excludedColumns = excludedColumns;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // Copy a table with a non-default column
                {
                        tableBuilder(TABLE_NAME)
                                .addColumn(stringCol().build())
                                .build(),
                        new ColumnInfo[] {},
                        new String[] {
                                "DROP TABLE IF EXISTS temp_" + TABLE_NAME + ";",
                                "CREATE TEMP TABLE temp_" + TABLE_NAME + " AS SELECT _id, created, deleted, modified, string_col FROM " + TABLE_NAME + ";"
                        }
                },
                // Copy a table with a foreign key column
                {
                        tableBuilder(TABLE_NAME)
                                .addColumn(longCol().foreignKeyInfo(idCascadeFKI("user")).build())
                                .build(),
                        new ColumnInfo[] {},
                        new String[] {
                                "DROP TABLE IF EXISTS temp_" + TABLE_NAME + ";",
                                "CREATE TEMP TABLE temp_" + TABLE_NAME + " AS SELECT _id, created, deleted, modified, long_col FROM " + TABLE_NAME + ";"
                        }
                },
                // Copy a table with an excluded column
                {
                        tableBuilder(TABLE_NAME)
                                .addColumn(longCol().foreignKeyInfo(idCascadeFKI("user")).build())
                                .build(),
                        new ColumnInfo[] {
                                longCol().foreignKeyInfo(idCascadeFKI("user")).build()
                        },
                        new String[] {
                                "DROP TABLE IF EXISTS temp_" + TABLE_NAME + ";",
                                "CREATE TEMP TABLE temp_" + TABLE_NAME + " AS SELECT _id, created, deleted, modified FROM " + TABLE_NAME + ";"
                        }
                },
        });
    }

    @Before
    public void setUp() {
        generatorUnderTest = new CreateTempTableFromExisting(table, excludedColumns);
    }

    @Override
    protected QueryGenerator getGenerator() {
        return generatorUnderTest;
    }
}
