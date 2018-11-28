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

import static com.fsryan.forsuredb.info.ColumnInfoUtil.colNameByType;
import static com.fsryan.forsuredb.info.DBInfoFixtures.*;
import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertListEquals;
import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertSetEquals;
import static org.junit.Assert.assertTrue;

public abstract class TableInfoTest {

    protected TableInfo tableUnderTest;

    protected List<ColumnInfo> nonDefaultColumns;

    public TableInfoTest(ColumnInfo[] nonDefaultColumns) {
        this.nonDefaultColumns = Arrays.asList(nonDefaultColumns);
    }

    @Before
    public void setUp() {
        TableInfo.Builder builder = TableInfo.builder()
                .tableName("test_table")
                .qualifiedClassName(TableInfoUtil.tableFQClassName("test_table"))
                .resetPrimaryKey(Collections.<String>emptySet())
                .addAllColumns(nonDefaultColumns);
        tableUnderTest = buildTableUnderTest(builder);
    }

    protected TableInfo buildTableUnderTest(TableInfo.Builder builder) {
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
            for (ColumnInfo column : TableInfo.defaultColumns().values()) {
                assertTrue("Default column: " + column.getColumnName() + " was not in table", tableUnderTest.hasColumn(column.getColumnName()));
            }
        }

        @Test
        public void shouldReturnNonForeignKeyColumnsInCorrectSort() {
            List<ColumnInfo> sortedNonForeignKeyColumns = new LinkedList<>();
            sortedNonForeignKeyColumns.addAll(tableUnderTest.getNonForeignKeyColumns());
            Collections.sort(sortedNonForeignKeyColumns);

            List<ColumnInfo> unsortedColumns = tableUnderTest.getNonForeignKeyColumns();
            assertListEquals("Incorrect column sort", sortedNonForeignKeyColumns, unsortedColumns);
        }
    }

    @RunWith(Parameterized.class)
    public static class PrimaryKeySetByColumns extends TableInfoTest {

        private final Set<String> expectedPrimaryKeys = new HashSet<>();

        public PrimaryKeySetByColumns(ColumnInfo[] nonDefaultColumns, String[] expectedPrimaryKeys) {
            super(nonDefaultColumns);
            this.expectedPrimaryKeys.addAll(Arrays.asList(expectedPrimaryKeys));
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
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
                            new String[] {colNameByType(long.class)}
                    },
                    {
                            new ColumnInfo[] {longCol().primaryKey(true).build(), stringCol().primaryKey(true).build()},
                            new String[] {colNameByType(long.class), colNameByType(String.class)}
                    },
            });
        }

        @Test
        public void shouldHaveExpectedPrimaryKey() {
            assertSetEquals(expectedPrimaryKeys, tableUnderTest.getPrimaryKey());
        }
    }

    @RunWith(Parameterized.class)
    public static class PrimaryKeySetByTable extends TableInfoTest {

        private final Set<String> expectedPrimaryKeys = new HashSet<>();

        public PrimaryKeySetByTable(ColumnInfo[] nonDefaultColumns, String[] primaryKey) {
            super(nonDefaultColumns);
            this.expectedPrimaryKeys.addAll(Arrays.asList(primaryKey));
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
                            new String[] {colNameByType(String.class)}
                    },
            });
        }

        @Test
        public void shouldHaveExpectedPrimaryKey() {
            assertSetEquals(expectedPrimaryKeys, tableUnderTest.getPrimaryKey());
        }

        @Override
        protected TableInfo buildTableUnderTest(TableInfo.Builder builder) {
            return builder.resetPrimaryKey(expectedPrimaryKeys).build();
        }
    }
}
