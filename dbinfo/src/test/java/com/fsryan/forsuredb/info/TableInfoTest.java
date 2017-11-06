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
package com.fsryan.forsuredb.info;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static com.fsryan.forsuredb.info.TestData.longCol;
import static com.fsryan.forsuredb.info.TestData.stringCol;
import static com.fsryan.forsuredb.info.TestData.table;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class TableInfoTest {

    protected TableInfo tableUnderTest;

    protected List<ColumnInfo> nonDefaultColumns;

    public TableInfoTest(ColumnInfo[] nonDefaultColumns) {
        this.nonDefaultColumns = createColumns(nonDefaultColumns);
    }

    @Before
    public void setUp() {
        TableInfo.BuilderCompat builder = table();
        Map<String, ColumnInfo> columnMap = new HashMap<>();
        for (ColumnInfo column : nonDefaultColumns) {
            columnMap.put(column.getColumnName(), column);
        }
        builder.columnMap(columnMap);
        tableUnderTest = buildTableUnderTest(builder);
    }

    protected TableInfo buildTableUnderTest(TableInfo.BuilderCompat builder) {
        return builder.build();
    }

    @RunWith(Parameterized.class)
    public static class Basic extends TableInfoTest {

        public Basic(ColumnInfo[] nonDefaultColumns) {
            super(nonDefaultColumns);
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    // Table with only default columns
                    {
                            new ColumnInfo[] {}
                    },
                    {
                            new ColumnInfo[] {longCol().build()}
                    },
            });
        }

        @Test
        public void shouldHaveDefaultColumns() {
            for (ColumnInfo column : TestData.DEFAULT_COLUMNS) {
                assertTrue("Default column: " + column.getColumnName() + " was not in table", tableUnderTest.hasColumn(column.getColumnName()));
            }
        }

        @Test
        public void shouldReturnNonForeignKeyColumnsInCorrectSort() {
            List<ColumnInfo> sortedNonForeignKeyColumns = new LinkedList<>();
            for (ColumnInfo column : tableUnderTest.getNonForeignKeyColumns()) {
                sortedNonForeignKeyColumns.add(column);
            }
            Collections.sort(sortedNonForeignKeyColumns);

            List<ColumnInfo> unsortedColumns = tableUnderTest.getNonForeignKeyColumns();
            for (int i = 0; i < sortedNonForeignKeyColumns.size(); i++) {
                assertEquals("Incorrect column sort", sortedNonForeignKeyColumns.get(i).getColumnName(), unsortedColumns.get(i).getColumnName());
            }
        }
    }

    @RunWith(Parameterized.class)
    public static class PrimaryKeySetByColumns extends TableInfoTest {

        private final Set<String> expectedPrimaryKeys = new HashSet<>();

        public PrimaryKeySetByColumns(ColumnInfo[] nonDefaultColumns, String[] expectedPrimaryKeys) {
            super(nonDefaultColumns);
            for (String expectedPrimaryKeyColumnName : expectedPrimaryKeys) {
                this.expectedPrimaryKeys.add(expectedPrimaryKeyColumnName);
            }
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    // Table with only default columns
                    {
                            new ColumnInfo[] {},
                            new String[] {"_id"}
                    },
                    {
                            new ColumnInfo[] {longCol().build()},
                            new String[] {"_id"}
                    },
                    {
                            new ColumnInfo[] {longCol().primaryKey(true).build()},
                            new String[] {longCol().build().getColumnName()}
                    },
                    {
                            new ColumnInfo[] {longCol().primaryKey(true).build(), stringCol().primaryKey(true).build()},
                            new String[] {longCol().build().getColumnName(), stringCol().build().getColumnName()}
                    },
            });
        }

        @Test
        public void shouldHaveExpectedPrimaryKey() {
            assertEquals(expectedPrimaryKeys, tableUnderTest.getPrimaryKey());
        }
    }

    @RunWith(Parameterized.class)
    public static class PrimaryKeySetByTable extends TableInfoTest {

        private final Set<String> expectedPrimaryKeys = new HashSet<>();

        public PrimaryKeySetByTable(ColumnInfo[] nonDefaultColumns, String[] primaryKey) {
            super(nonDefaultColumns);
            for (String expectedPrimaryKeyColumnName : primaryKey) {
                this.expectedPrimaryKeys.add(expectedPrimaryKeyColumnName);
            }
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    // Table with only default columns
                    {
                            new ColumnInfo[] {},
                            new String[] {"_id"}
                    },
                    {
                            new ColumnInfo[] {longCol().build()},
                            new String[] {"_id"}
                    },
                    {   // 02: even if columns have primary key listed, it should be ignored if primary key set on table
                            new ColumnInfo[] {longCol().primaryKey(true).build()},
                            new String[] {"_id"}
                    },
                    {
                            new ColumnInfo[] {longCol().primaryKey(true).build(), stringCol().primaryKey(true).build()},
                            new String[] {stringCol().build().getColumnName()}
                    },
            });
        }

        @Test
        public void shouldHaveExpectedPrimaryKey() {
            assertEquals(expectedPrimaryKeys, tableUnderTest.getPrimaryKey());
        }

        @Override
        protected TableInfo buildTableUnderTest(TableInfo.BuilderCompat builder) {
            return builder.primaryKey(expectedPrimaryKeys).build();
        }
    }

    /*package*/ static List<ColumnInfo> createColumns(ColumnInfo[] nonDefaultColumns) {
        List<ColumnInfo> retList = new LinkedList<ColumnInfo>();
        for (ColumnInfo column : nonDefaultColumns) {
            retList.add(column);
        }
        return retList;
    }
}
