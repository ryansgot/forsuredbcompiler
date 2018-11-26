package com.fsryan.forsuredb.annotationprocessor;

import com.fsryan.forsuredb.annotationprocessor.util.Pair;
import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.info.TableInfo;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static com.fsryan.forsuredb.TestData.*;
import static org.junit.Assert.assertEquals;

public abstract class TableContextBuilderTest {

    @RunWith(Parameterized.class)
    public static class NonDocStore extends TableContextBuilderTest {

        private final Map<Pair<String, String>, TableInfo.Builder> startingTables;
        private final Map<String, Map<String, ColumnInfo.Builder>> startingColumns;
        private final Map<String, Map<String, Set<TableForeignKeyInfo.Builder>>> startingForeignKeys;
        private final Map<String, TableInfo> expectedSchema;

        private TableContext.Builder builderUnderTest;
        private TableContext actual;

        public NonDocStore(Map<Pair<String, String>, TableInfo.Builder> startingTables,
                           Map<String, Map<String, ColumnInfo.Builder>> startingColumns,
                           Map<String, Map<String, Set<TableForeignKeyInfo.Builder>>> startingForeignKeys,
                           Map<String, TableInfo> expectedSchema) {
            this.startingTables = startingTables;
            this.startingColumns = startingColumns;
            this.startingForeignKeys = startingForeignKeys;
            this.expectedSchema = expectedSchema;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {   // 00: one table with no extra columns
                        // tests basic one-table behavior
                            ImmutableMap.of(table1KeyPair(), table1Builder()),
                            Collections.emptyMap(),
                            Collections.emptyMap(),
                            ImmutableMap.of("table_1", table1Builder().build())
                    },
                    {   // 01: one table with an extra column
                        // tests that extra column definitions
                            ImmutableMap.of(table1KeyPair(), table1Builder()),
                            ImmutableMap.of("table_1", ImmutableMap.of("column_1", table1Column1Builder())),
                            Collections.emptyMap(),
                            ImmutableMap.of(
                                    "table_1",
                                    table1Builder()
                                            .addColumn(table1Column1Builder().build())
                                            .build()
                            )
                    },
                    {   // 02: two tables
                        // Tests that multiple tables are supported
                            ImmutableMap.of(
                                    table1KeyPair(), table1Builder(),
                                    table2KeyPair(), table2Builder()
                            ),
                            ImmutableMap.of(
                                    "table_1", ImmutableMap.of("column_1", table1Column1Builder()),
                                    "table_2", ImmutableMap.of("table_2_column", table2ColumnBuilder())
                            ),
                            Collections.emptyMap(),
                            ImmutableMap.of(
                                    "table_1",
                                    table1Builder()
                                            .addColumn(table1Column1Builder().build())
                                            .build(),
                                    "table_2",
                                    table2Builder()
                                            .addColumn(table2ColumnBuilder().build())
                                            .build()
                            )
                    },
                    {   // 03: two tables, one with a non-composite foreign key to the other
                        // Tests basic foreign key building
                            ImmutableMap.of(
                                    table1KeyPair(), table1Builder(),
                                    table2KeyPair(), table2Builder()
                            ),
                            ImmutableMap.of(
                                    "table_1", ImmutableMap.of("column_1", table1Column1Builder()),
                                    "table_2", ImmutableMap.of("table_2_column", table2ColumnBuilder())
                            ),
                            ImmutableMap.of(
                                    "table_2",
                                    ImmutableMap.of(
                                            "",
                                            Sets.newHashSet(
                                                    TableForeignKeyInfo.builder()
                                                            .localToForeignColumnMap(ImmutableMap.of("table_2_column", "_id"))
                                                            .deleteChangeAction("CASCADE")
                                                            .updateChangeAction("CASCADE")
                                                            .foreignTableApiClassName(TableContextBuilderTest.class.getName())
                                            )
                                    )
                            ),
                            ImmutableMap.of(
                                    "table_1",
                                    table1Builder()
                                            .addColumn(table1Column1Builder().build())
                                            .build(),
                                    "table_2",
                                    table2Builder()
                                            .addColumn(table2ColumnBuilder().build())
                                            .addForeignKey(TableForeignKeyInfo.builder()
                                                    .localToForeignColumnMap(ImmutableMap.of("table_2_column", "_id"))
                                                    .deleteChangeAction("CASCADE")
                                                    .updateChangeAction("CASCADE")
                                                    .foreignTableApiClassName(TableContextBuilderTest.class.getName())
                                                    .foreignTableName("table_1")
                                                    .build())
                                            .build()
                            )
                    },
                    {   // 04: two tables, one with a composite foreign key to the other and one with a composite primary key
                            // Tests the collapsing of composites into a single foreign key with multiple columns
                            ImmutableMap.of(
                                    table1KeyPair(),
                                    table1Builder()
                                            .resetPrimaryKey(Sets.newHashSet("_id", "column_1"))
                                            .primaryKeyOnConflict("REPLACE"),
                                    table2KeyPair(),
                                    table2Builder()
                            ),
                            ImmutableMap.of(
                                    "table_1",
                                    ImmutableMap.of("column_1", table1Column1Builder().unique(true)),
                                    "table_2",
                                    ImmutableMap.of(
                                            "table_2_column", table2ColumnBuilder(),
                                            "table_2_string_column", table2StringColumnBuilder()
                                    )
                            ),
                            ImmutableMap.of(
                                    "table_2",
                                    ImmutableMap.of(
                                            UUID.randomUUID().toString(),   // <-- this is the composite id
                                            Sets.newHashSet(
                                                    TableForeignKeyInfo.builder()
                                                            .localToForeignColumnMap(ImmutableMap.of("table_2_column", "_id"))
                                                            .deleteChangeAction("CASCADE")
                                                            .updateChangeAction("CASCADE")
                                                            .foreignTableApiClassName(TableContextBuilderTest.class.getName()),
                                                    TableForeignKeyInfo.builder()
                                                            .localToForeignColumnMap(ImmutableMap.of("table_2_string_column", "column_1"))
                                                            .deleteChangeAction("CASCADE")
                                                            .updateChangeAction("CASCADE")
                                                            .foreignTableApiClassName(TableContextBuilderTest.class.getName())
                                            )
                                    )
                            ),
                            ImmutableMap.of(
                                    "table_1",
                                    table1Builder()
                                            .resetPrimaryKey(Sets.newHashSet("_id", "column_1"))
                                            .primaryKeyOnConflict("REPLACE")
                                            .addColumn(table1Column1Builder().unique(true).build())
                                            .build(),
                                    "table_2",
                                    table2Builder()
                                            .addColumn(table2ColumnBuilder().build())
                                            .addColumn(table2StringColumnBuilder().build())
                                            .addForeignKey(TableForeignKeyInfo.builder()
                                                    .localToForeignColumnMap(ImmutableMap.of(
                                                            "table_2_column", "_id",
                                                            "table_2_string_column", "column_1")
                                                    ).deleteChangeAction("CASCADE")
                                                    .updateChangeAction("CASCADE")
                                                    .foreignTableApiClassName(TableContextBuilderTest.class.getName())
                                                    .foreignTableName("table_1")
                                                    .build())
                                            .build()
                            )
                    },
                    {   // 05: three tables-one with multiple non-composite foreign keys
                        // Tests the collapsing of composites into a single foreign key with multiple columns
                            ImmutableMap.of(
                                    table1KeyPair(), table1Builder(),
                                    table2KeyPair(), table2Builder(),
                                    table3KeyPair(), table3Builder()
                            ),
                            ImmutableMap.of(
                                    "table_1", ImmutableMap.of("column_1", table1Column1Builder()),
                                    "table_2", ImmutableMap.of(
                                            "table_2_column", table2ColumnBuilder(),
                                            "table_2_string_column", table2StringColumnBuilder(),
                                            "table_2_second_long_column", table2SecondLongColumnBuilder()
                                    )
                            ),
                            ImmutableMap.of(
                                    "table_2",
                                    ImmutableMap.of(
                                            "",
                                            Sets.newHashSet(
                                                    TableForeignKeyInfo.builder()
                                                            .localToForeignColumnMap(ImmutableMap.of("table_2_column", "_id"))
                                                            .deleteChangeAction("CASCADE")
                                                            .updateChangeAction("CASCADE")
                                                            .foreignTableApiClassName(TableContextBuilderTest.class.getName()),
                                                    TableForeignKeyInfo.builder()
                                                            .localToForeignColumnMap(ImmutableMap.of("table_2_second_long_column", "_id"))
                                                            .deleteChangeAction("CASCADE")
                                                            .updateChangeAction("CASCADE")
                                                            .foreignTableApiClassName(FSAnnotationProcessor.class.getName())
                                            )
                                    )
                            ),
                            ImmutableMap.of(
                                    "table_1",
                                    table1Builder()
                                            .addColumn(table1Column1Builder().build())
                                            .build(),
                                    "table_2",
                                    table2Builder()
                                            .addColumn(table2ColumnBuilder().build())
                                            .addColumn(table2StringColumnBuilder().build())
                                            .addColumn(table2SecondLongColumnBuilder().build())
                                            .addForeignKey(TableForeignKeyInfo.builder()
                                                    .localToForeignColumnMap(ImmutableMap.of("table_2_column", "_id"))
                                                    .deleteChangeAction("CASCADE")
                                                    .updateChangeAction("CASCADE")
                                                    .foreignTableApiClassName(TableContextBuilderTest.class.getName())
                                                    .foreignTableName("table_1")
                                                    .build())
                                            .addForeignKey(TableForeignKeyInfo.builder()
                                                    .localToForeignColumnMap(ImmutableMap.of("table_2_second_long_column", "_id"))
                                                    .deleteChangeAction("CASCADE")
                                                    .updateChangeAction("CASCADE")
                                                    .foreignTableApiClassName(FSAnnotationProcessor.class.getName())
                                                    .foreignTableName("table_3")
                                                    .build())
                                            .build(),
                                    "table_3",
                                    table3Builder().build()
                            )
                    }
            });
        }

        @Before
        public void createBuilderUnderTest() {
            builderUnderTest = new TableContext.Builder();
            startingTables.forEach((tableKeys, tableBuilder) -> {
                builderUnderTest.addTable(tableKeys.first, tableKeys.second, tableBuilder);
                startingColumns.getOrDefault(tableKeys.first, Collections.emptyMap()).forEach((columnName, columnBuilder) -> {
                    builderUnderTest.addColumn(tableKeys.first, columnName, columnBuilder);
                });
                startingForeignKeys.getOrDefault(tableKeys.first, Collections.emptyMap()).forEach((compositeId, foreignKeyBuilders) -> {
                    foreignKeyBuilders.forEach(builder -> builderUnderTest.addForeignKeyInfo(tableKeys.first, compositeId, builder));
                });
            });
            actual = builderUnderTest.build();
        }

        @Test
        public void shouldGenerateCorrectSchema() {
            assertEquals(expectedSchema, actual.tableMap());
        }

        @Test
        public void shouldGenerateAllTables() {
            assertEquals(expectedSchema.keySet(), actual.tableMap().keySet());
        }

        @Test
        public void shouldGenerateAllColumns() {
            expectedSchema.forEach((tableKey, expectedTable) -> {
                assertEquals(expectedTable.columnMap(), actual.tableMap().get(tableKey).columnMap());
            });
        }

        @Test
        public void shouldGenerateExpectedForeignKeys() {
            expectedSchema.forEach((tableKey, expectedTable) -> {
                assertEquals(expectedTable.foreignKeys(), actual.tableMap().get(tableKey).foreignKeys());
            });
        }
    }

    private static Pair<String, String> table1KeyPair() {
        return new Pair<>("table_1", TableContextBuilderTest.class.getName());
    }

    private static TableInfo.Builder table1Builder() {
        return tableBuilderWithDefaultColumns()
                .tableName("table_1")
                .qualifiedClassName(TableContextBuilderTest.class.getName())
                .resetPrimaryKey(Sets.newHashSet(TableInfo.DEFAULT_PRIMARY_KEY_COLUMN))
                .primaryKeyOnConflict("")
                .staticDataAsset("")
                .staticDataRecordName("");
    }

    private static ColumnInfo.Builder table1Column1Builder() {
        return ColumnInfo.builder()
                .columnName("column_1")
                .methodName("column1Method")
                .qualifiedType(String.class.getName());
    }

    private static Pair<String, String> table2KeyPair() {
        return new Pair<>("table_2", TableContextBuilderTest.NonDocStore.class.getName());
    }

    private static TableInfo.Builder table2Builder() {
        return tableBuilderWithDefaultColumns()
                .tableName("table_2")
                .qualifiedClassName(TableContextBuilderTest.NonDocStore.class.getName())
                .resetPrimaryKey(Sets.newHashSet(TableInfo.DEFAULT_PRIMARY_KEY_COLUMN))
                .primaryKeyOnConflict("")
                .staticDataAsset("")
                .staticDataRecordName("");
    }

    private static ColumnInfo.Builder table2ColumnBuilder() {
        return ColumnInfo.builder()
                .columnName("table_2_column")
                .methodName("table2ColumnMethod")
                .qualifiedType(long.class.getName());
    }

    private static ColumnInfo.Builder table2StringColumnBuilder() {
        return ColumnInfo.builder()
                .columnName("table_2_string_column")
                .methodName("table2StringColumnMethod")
                .qualifiedType(String.class.getName());
    }

    private static ColumnInfo.Builder table2SecondLongColumnBuilder() {
        return ColumnInfo.builder()
                .columnName("table_2_second_long_column")
                .methodName("table2SecondLongColumnMethod")
                .qualifiedType(long.class.getName());
    }

    private static Pair<String, String> table3KeyPair() {
        return new Pair<>("table_3", FSAnnotationProcessor.class.getName());
    }

    private static TableInfo.Builder table3Builder() {
        return tableBuilderWithDefaultColumns()
                .tableName("table_3")
                .qualifiedClassName(FSAnnotationProcessor.class.getName())
                .resetPrimaryKey(Sets.newHashSet(TableInfo.DEFAULT_PRIMARY_KEY_COLUMN))
                .primaryKeyOnConflict("")
                .staticDataAsset("")
                .staticDataRecordName("");
    }

    private static TableInfo.Builder tableBuilderWithDefaultColumns() {
        return TableInfo.builder()
                .addColumn(createdCol())
                .addColumn(deletedCol())
                .addColumn(modifiedCol())
                .addColumn(idCol());
    }
}
