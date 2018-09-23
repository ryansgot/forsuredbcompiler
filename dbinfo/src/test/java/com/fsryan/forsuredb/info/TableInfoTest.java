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

import static com.fsryan.forsuredb.info.TestData.*;
import static com.fsryan.forsuredb.test.tools.CollectionUtil.mapOf;
import static com.fsryan.forsuredb.test.tools.CollectionUtil.setOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class TableInfoTest {

    protected TableInfo tableUnderTest;

    protected List<ColumnInfo> nonDefaultColumns;

    public TableInfoTest(ColumnInfo[] nonDefaultColumns) {
        this.nonDefaultColumns = new ArrayList<>(Arrays.asList(nonDefaultColumns));
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
            this.expectedPrimaryKeys.addAll(Arrays.asList(expectedPrimaryKeys));
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

    /**
     * <p>This class sets up the scenario wherein the {@link TableInfo} is
     * built without the notion of {@link TableIndexInfo}. As such, these tests
     * demonstrate the behavior in which the {@link TableInfo} can only get its
     * {@link TableIndexInfo} from pulling the relevant data off of the
     * {@link ColumnInfo} associated with the table.
     *
     * <p>As an aside, this will mean that all
     * {@link TableIndexInfo#columnSortOrderMap()} will be singleton maps and
     * additionally that all values of the map will be the empty string. This
     * fact is a result of pre-0.14.0 versions of forsuredb not supporting
     * composite expectedIndices and not supporting directional expectedIndices (ASC, DESC).
     */
    @RunWith(Parameterized.class)
    public static class IndicesSetByColumns extends TableInfoTest {

        private final String desc;
        private final Set<TableIndexInfo> expectedTableIndices;

        public IndicesSetByColumns(String desc, ColumnInfo[] nonDefaultColumns, Set<TableIndexInfo> expectedTableIndices) {
            super(nonDefaultColumns);
            this.desc = desc;
            this.expectedTableIndices = expectedTableIndices;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {
                            "00: no added columns should result in empty set",
                            new ColumnInfo[0],
                            Collections.<TableIndexInfo>emptySet()
                    },
                    {
                            "01: added columns that are not marked as expectedIndices should result in empty set",
                            new ColumnInfo[] { longCol().build() },
                            Collections.<TableIndexInfo>emptySet()
                    },
                    {
                            "02: a single unique index column should result in a single TableIndexInfo with unique == true",
                            new ColumnInfo[] { longCol().index(true).unique(true).build() },
                            setOf(
                                    TableIndexInfo.create(
                                            mapOf(longCol().build().columnName(), ""),
                                            true
                                    )
                            )
                    },
                    {
                            "03: single non-unique index column should result in a single TableIndexInfo with unique == false",
                            new ColumnInfo[] { longCol().index(true).unique(false).build() },
                            setOf(
                                    TableIndexInfo.create(
                                            mapOf(longCol().build().columnName(), ""),
                                            false
                                    )
                            )
                    },
                    {
                            "04: with a non-unique index column and a unique index column should result in a two-TableIndexInfo set",
                            new ColumnInfo[] {
                                    longCol().index(true).unique(false).build(),
                                    stringCol().index(true).unique(true).build()
                            },
                            setOf(
                                    TableIndexInfo.create(
                                            mapOf(longCol().build().columnName(), ""),
                                            false
                                    ),
                                    TableIndexInfo.create(
                                            mapOf(stringCol().build().columnName(), ""),
                                            true
                                    )
                            )
                    },
                    {   //
                            "05: with a non-index column and a unique index column should result in a singleton TableIndexInfo set",
                            new ColumnInfo[] {
                                    longCol().index(false).unique(false).build(),
                                    stringCol().index(true).unique(true).build()
                            },
                            setOf(
                                    TableIndexInfo.create(
                                            mapOf(stringCol().build().columnName(), ""),
                                            true
                                    )
                            )
                    }
            });
        }

        @Test
        public void shouldCorrectlyInterpretColumnIndicesOnTableInfo() {
            assertEquals(desc, expectedTableIndices, tableUnderTest.indices());
        }
    }

    /**
     * <p>This class is intended to demonstrate that building
     */
    @RunWith(Parameterized.class)
    public static class IndicesSetOnTable extends TableInfoTest {

        private final String desc;
        private final Set<TableIndexInfo> inputIndices;
        private final Set<TableIndexInfo> expectedIndices;

        public IndicesSetOnTable(String desc, ColumnInfo[] nonDefaultColumns, Set<TableIndexInfo> inputIndices, Set<TableIndexInfo> expectedIndices) {
            super(nonDefaultColumns);
            this.desc = desc;
            this.inputIndices = inputIndices;
            this.expectedIndices = expectedIndices;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {
                            "00: empty set of expectedIndices added with no columns should result in empty set of expectedIndices",
                            new ColumnInfo[0],
                            Collections.<TableIndexInfo>emptySet(),
                            Collections.<TableIndexInfo>emptySet()
                    },
                    {
                            "01: empty set of expectedIndices with an index column should NOT ignore the column, setting the expectedIndices from the column",
                            new ColumnInfo[] { longCol().index(true).unique(true).build() },
                            Collections.<TableIndexInfo>emptySet(),
                            setOf(
                                    TableIndexInfo.create(
                                            mapOf(longCol().build().columnName(), ""),
                                            true
                                    )
                            )
                    },
                    {
                            "02: nonempty set of expectedIndices with no index columns should set the expectedIndices from the inputIndices",
                            new ColumnInfo[0],
                            setOf(
                                    TableIndexInfo.create(
                                            mapOf(longCol().build().columnName(), ""),
                                            true
                                    )
                            ),
                            setOf(
                                    TableIndexInfo.create(
                                            mapOf(longCol().build().columnName(), ""),
                                            true
                                    )
                            )
                    },
                    {
                            "03: nonempty set of expectedIndices with index columns should set the expectedIndices from the inputIndices--ignoring the index columns",
                            new ColumnInfo[] {stringCol().index(true).build()},
                            setOf(
                                    TableIndexInfo.create(
                                            mapOf(longCol().build().columnName(), "ASC"),
                                            true
                                    )
                            ),
                            setOf(
                                    TableIndexInfo.create(
                                            mapOf(longCol().build().columnName(), "ASC"),
                                            true
                                    )
                            )
                    },
                    {
                            "04: multiple indices should be possible",
                            new ColumnInfo[] {stringCol().index(true).build()},
                            setOf(
                                    TableIndexInfo.create(
                                            mapOf(
                                                    longCol().build().columnName(), "ASC",
                                                    intCol().build().columnName(), "DESC"
                                            ),
                                            true
                                    ),
                                    TableIndexInfo.create(
                                            mapOf(
                                                    doubleCol().build().columnName(), ""
                                            ),
                                            false
                                    )
                            ),
                            setOf(
                                    TableIndexInfo.create(
                                            mapOf(
                                                    longCol().build().columnName(), "ASC",
                                                    intCol().build().columnName(), "DESC"
                                            ),
                                            true
                                    ),
                                    TableIndexInfo.create(
                                            mapOf(
                                                    doubleCol().build().columnName(), ""
                                            ),
                                            false
                                    )
                            ),
                    },
            });
        }

        @Test
        public void shouldCorrectlyInterpretColumnIndicesOnTableInfo() {
            assertEquals(desc, expectedIndices, tableUnderTest.indices());
        }

        @Override
        protected TableInfo buildTableUnderTest(TableInfo.BuilderCompat builder) {
            return builder.indices(inputIndices).build();
        }
    }
}
