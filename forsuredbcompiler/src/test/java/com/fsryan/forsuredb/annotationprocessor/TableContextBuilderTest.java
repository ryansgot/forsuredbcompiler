package com.fsryan.forsuredb.annotationprocessor;

import com.fsryan.forsuredb.annotationprocessor.util.Pair;
import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.info.TableInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static com.fsryan.forsuredb.info.ColumnInfoUtil.colNameByType;
import static com.fsryan.forsuredb.info.DBInfoFixtures.*;
import static com.fsryan.forsuredb.info.TableInfoUtil.tableFQClassName;
import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertMapEquals;
import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertSetEquals;
import static com.fsryan.forsuredb.test.tools.CollectionUtil.mapOf;
import static com.fsryan.forsuredb.test.tools.CollectionUtil.setOf;

public abstract class TableContextBuilderTest {

    @RunWith(Parameterized.class)
    public static class NonDocStore extends TableContextBuilderTest {

        // table name -> table builder for that name
        private final Map<String, TableInfo.Builder> startingTables;
        // Pair table name, column name -> column builder for that pair
        private final Map<Pair<String, String>, ColumnInfo.Builder> startingColumns;
        // pair table name, composite name -> set of table foreign key builder for that pair
        private final Map<Pair<String, String>, Set<TableForeignKeyInfo.Builder>> startingFKs;
        private final Map<String, TableInfo> expectedSchema;

        private TableContext.Builder builderUnderTest;
        private TableContext actual;

        public NonDocStore(Map<String, TableInfo.Builder> startingTables,
                           Map<Pair<String, String>, ColumnInfo.Builder> startingColumns,
                           Map<Pair<String, String>, Set<TableForeignKeyInfo.Builder>> startingFKs,
                           Map<String, TableInfo> expectedSchema) {
            this.startingTables = startingTables;
            this.startingColumns = startingColumns;
            this.startingFKs = startingFKs;
            this.expectedSchema = expectedSchema;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {   // 00: one table with no extra columns
                        // tests basic one-table behavior
                            mapOf("t1", tableBuilder("t1")),
                            Collections.emptyMap(),
                            Collections.emptyMap(),
                            mapOf(tableFQClassName("t1"), tableBuilder("t1").build())
                    },
                    {   // 01: one table with an extra column
                        // tests that extra column definitions
                            mapOf("t1", tableBuilder("t1")),
                            mapOf(t1ColPair(colNameByType(String.class)), stringCol()),
                            Collections.emptyMap(),
                            mapOf(
                                    tableFQClassName("t1"),
                                    tableBuilder("t1")
                                            .addColumn(stringCol().build())
                                            .build()
                            )
                    },
                    {   // 02: two tables
                        // Tests that multiple tables are supported
                            mapOf(
                                    "t1", tableBuilder("t1"),
                                    "t2", tableBuilder("t2")
                            ),
                            mapOf(
                                    t1ColPair("t1c1"), t1c1(),
                                    t2ColPair("t2c1"), t2c1()
                            ),
                            Collections.emptyMap(),
                            mapOf(
                                    tableFQClassName("t1"),
                                    tableBuilder("t1")
                                            .addColumn(t1c1().build())
                                            .build(),
                                    tableFQClassName("t2"),
                                    tableBuilder("t2")
                                            .addColumn(t2c1().build())
                                            .build()
                            )
                    },
                    {   // 03: two tables, one with a non-composite foreign key to the other
                        // Tests basic foreign key building
                            mapOf(
                                    "t1", tableBuilder("t1"),
                                    "t2", tableBuilder("t2")
                            ),
                            mapOf(
                                    t1ColPair("t1c1"), t1c1(),
                                    t2ColPair("t2c1"), t2c1()
                            ),
                            mapOf(
                                    t2FKPair(""),
                                    setOf(
                                            cascadeForeignKeyTo("t1")
                                                    .mapLocalToForeignColumn("t2c1", "t1c1")
                                    )
                            ),
                            mapOf(
                                    tableFQClassName("t1"),
                                    tableBuilder("t1")
                                            .addColumn(t1c1().build())
                                            .build(),
                                    tableFQClassName("t2"),
                                    tableBuilder("t2")
                                            .addColumn(t2c1().build())
                                            .addForeignKey(
                                                    cascadeForeignKeyTo("t1")
                                                            .mapLocalToForeignColumn("t2c1", "t1c1")
                                                            .build()
                                            ).build()
                            )
                    },
                    {   // 04: two tables, one with a composite foreign key to the other and one with a composite primary key
                            // Tests the collapsing of composites into a single foreign key with multiple columns
                            mapOf(
                                    "t1",
                                    tableBuilder("t1")
                                            .resetPrimaryKey(setOf("t1c1", "t1c2"))
                                            .primaryKeyOnConflict("REPLACE"),
                                    "t2",
                                    tableBuilder("t2")
                            ),
                            mapOf(
                                    t1ColPair("t1c1"), t1c1(),
                                    t1ColPair("t1c2"), t1c2(),
                                    t2ColPair("t2c1"), t2c1(),
                                    t2ColPair("t2c2"), t2c2()
                            ),
                            mapOf(
                                    t2FKPair(UUID.randomUUID().toString()),
                                    setOf(
                                            cascadeForeignKeyTo("t1")
                                                    .mapLocalToForeignColumn("t2c1", "t1c1"),
                                            cascadeForeignKeyTo("t1")
                                                    .mapLocalToForeignColumn("t2c2", "t1c2")
                                    )
                            ),
                            mapOf(
                                    tableFQClassName("t1"),
                                    tableBuilder("t1")
                                            .resetPrimaryKey(setOf("t1c1", "t1c2"))
                                            .primaryKeyOnConflict("REPLACE")
                                            .addColumn(t1c1().build())
                                            .addColumn(t1c2().build())
                                            .build(),
                                    tableFQClassName("t2"),
                                    tableBuilder("t2")
                                            .addColumn(t2c1().build())
                                            .addColumn(t2c2().build())
                                            .addForeignKey(cascadeForeignKeyTo("t1")
                                                    .mapLocalToForeignColumn("t2c1", "t1c1")
                                                    .mapLocalToForeignColumn("t2c2", "t1c2")
                                                    .build())
                                            .build()
                            )
                    },
                    {   // 05: three tables-one with multiple non-composite foreign keys
                        // Tests the collapsing of composites into a single foreign key with multiple columns
                            mapOf(
                                    "t1", tableBuilder("t1")
                                            .resetPrimaryKey(setOf("t1c1", "t1c2")),
                                    "t2", tableBuilder("t2"),
                                    "t3", tableBuilder("t3")
                            ),
                            mapOf(
                                    t1ColPair("t1c1"), t1c1(),
                                    t1ColPair("t1c2"), t1c2(),
                                    t2ColPair("t2c1"), t2c1(),
                                    t2ColPair("t2c2"), t2c2(),
                                    t2ColPair("t2c3"), t2c3()
                            ),
                            mapOf(
                                    t2FKPair(""),
                                    setOf(
                                            cascadeForeignKeyTo("t1")
                                                    .mapLocalToForeignColumn("t2c1", "t1c1"),
                                            cascadeForeignKeyTo("t1")
                                                    .mapLocalToForeignColumn("t2c2", "t1c2")
                                    )
                            ),
                            mapOf(
                                    tableFQClassName("t1"),
                                    tableBuilder("t1")
                                            .resetPrimaryKey(setOf("t1c1", "t1c2"))
                                            .addColumn(t1c1().build())
                                            .addColumn(t1c2().build())
                                            .build(),
                                    tableFQClassName("t2"),
                                    tableBuilder("t2")
                                            .addColumn(t2c1().build())
                                            .addColumn(t2c2().build())
                                            .addColumn(t2c3().build())
                                            .addForeignKey(
                                                    cascadeForeignKeyTo("t1")
                                                            .mapLocalToForeignColumn("t2c1", "t1c1")
                                                            .build()
                                            ).addForeignKey(
                                                    cascadeForeignKeyTo("t1")
                                                            .mapLocalToForeignColumn("t2c2", "t1c2")
                                                            .build()
                                            ).build(),
                                    tableFQClassName("t3"),
                                    tableBuilder("t3").build()
                            )
                    }
            });
        }

        @Before
        public void createBuilderUnderTest() {
            builderUnderTest = new TableContext.Builder();
            startingTables
                    .forEach((name, builder) -> builderUnderTest.addTable(name, tableFQClassName(name), builder));
            startingColumns
                    .forEach((t2cMethod, builder) -> builderUnderTest.addColumn(tableFQClassName(t2cMethod.first()), t2cMethod.second(), builder));
            startingFKs
                    .forEach((t2Composite, builderSet) -> {
                        final String tableClassName = tableFQClassName(t2Composite.first());
                        final String compositeId = t2Composite.second();
                        builderSet.forEach(builder -> builderUnderTest.addForeignKeyInfo(tableClassName, compositeId, builder));
                    });
            actual = builderUnderTest.build();
        }

        @Test
        public void shouldGenerateCorrectSchema() {
            assertMapEquals(expectedSchema, actual.tableMap());
        }

        @Test
        public void shouldGenerateAllTables() {
            assertSetEquals(expectedSchema.keySet(), actual.tableMap().keySet());
        }

        @Test
        public void shouldGenerateAllColumns() {
            expectedSchema.forEach((tableKey, expectedTable) -> {
                assertMapEquals(expectedTable.columnMap(), actual.tableMap().get(tableKey).columnMap());
            });
        }

        @Test
        public void shouldGenerateExpectedForeignKeys() {
            expectedSchema.forEach((tableKey, expectedTable) -> {
                assertSetEquals(expectedTable.foreignKeys(), actual.tableMap().get(tableKey).foreignKeys());
            });
        }
    }

    static Pair<String, String> t1ColPair(String colName) {
        return Pair.create("t1", colName);
    }

    static ColumnInfo.Builder t1c1() {
        return stringCol().methodName("t1c1").columnName("t1c1");
    }

    static ColumnInfo.Builder t1c2() {
        return intCol().methodName("t1c2").columnName("t1c2");
    }

    static Pair<String, String> t2ColPair(String colName) {
        return Pair.create("t2", colName);
    }

    static Pair<String, String> t2FKPair(String compositeId) {
        return Pair.create("t2", compositeId);
    }

    static ColumnInfo.Builder t2c1() {
        return stringCol().methodName("t2c1").columnName("t2c1");
    }

    static ColumnInfo.Builder t2c2() {
        return intCol().methodName("t2c2").columnName("t2c2");
    }

    static ColumnInfo.Builder t2c3() {
        return longCol().methodName("t2c3").columnName("t2c3");
    }
}
