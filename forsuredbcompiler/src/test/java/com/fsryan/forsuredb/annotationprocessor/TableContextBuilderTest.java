package com.fsryan.forsuredb.annotationprocessor;

import com.fsryan.forsuredb.annotationprocessor.util.Pair;
import com.fsryan.forsuredb.info.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertMapEquals;
import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertSetEquals;
import static com.fsryan.forsuredb.test.tools.CollectionUtil.mapOf;
import static com.fsryan.forsuredb.test.tools.CollectionUtil.setOf;
import static org.junit.Assert.assertEquals;

public abstract class TableContextBuilderTest {

    protected final String desc;
    protected final Map<Pair<String, String>, TableInfo.BuilderCompat> startingTables;
    protected final Map<String, Map<String, ColumnInfo.Builder>> startingColumns;
    protected final Map<String, TableInfo> expectedSchema;

    protected TableContext.Builder builderUnderTest;
    protected TableContext actual;

    public TableContextBuilderTest(String desc, Map<Pair<String, String>, TableInfo.BuilderCompat> startingTables, Map<String, Map<String, ColumnInfo.Builder>> startingColumns, Map<String, TableInfo> expectedSchema) {
        this.desc = desc;
        this.startingTables = startingTables;
        this.startingColumns = startingColumns;
        this.expectedSchema = expectedSchema;
    }

    @Before
    public void createBuilderUnderTest() {
        builderUnderTest = new TableContext.Builder();
        startingTables.forEach((tableKeys, tabeBuilder) -> {
            builderUnderTest.addTable(tableKeys.first(), tableKeys.second(), tabeBuilder);
            TableInfo.defaultColumns().forEach((columnName, columnInfo) -> {
                builderUnderTest.addColumn(tableKeys.first(), columnName, columnInfo.toBuilder());
            });
            startingColumns.getOrDefault(tableKeys.first(), Collections.emptyMap()).forEach((columnName, columnBuilder) -> {
                builderUnderTest.addColumn(tableKeys.first(), columnName, columnBuilder);
            });
        });
        updateBuilder(builderUnderTest);
        actual = builderUnderTest.build();
    }

    protected abstract void updateBuilder(TableContext.Builder tableContextBuilder);

    @Test
    public void shouldGenerateCorrectSchema() {
        assertMapEquals(desc, expectedSchema, actual.tableMap());
    }

    @Test
    public void shouldGenerateAllTables() {
        assertEquals(desc, expectedSchema.keySet(), actual.tableMap().keySet());
    }

    @Test
    public void shouldGenerateAllColumns() {
        expectedSchema.forEach((tableKey, expectedTable) -> {
            Map<String, ColumnInfo> expectedColumns = expectedTable.columnMap();
            Map<String, ColumnInfo> actualColumns = actual.tableMap().get(tableKey).columnMap();
            assertMapEquals(desc, expectedColumns, actualColumns);
        });
    }

    @RunWith(Parameterized.class)
    public static class TablesAndColumns extends TableContextBuilderTest {

        public TablesAndColumns(String desc, Map<Pair<String, String>, TableInfo.BuilderCompat> startingTables, Map<String, Map<String, ColumnInfo.Builder>> startingColumns, Map<String, TableInfo> expectedSchema) {
            super(desc, startingTables, startingColumns, expectedSchema);
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {
                            "00: one tableBuilder with no extra columns tests basic one-tableBuilder behavior",
                            mapOf(table1KeyPair(), table1Builder()),
                            Collections.emptyMap(),
                            mapOf(
                                    "table_1",
                                    table1Builder()
                                            .columnMap(defaultColumnsPlus())
                                            .foreignKeys(Collections.emptySet())
                                            .build()
                            )
                    },
                    {
                            "01: one tableBuilder with an extra column tests that extra column defninitions",
                            mapOf(table1KeyPair(), table1Builder()),
                            mapOf("table_1", mapOf("column_1", table1ColumnBuilder("column_1"))),
                            mapOf(
                                    "table_1",
                                    table1Builder()
                                            .columnMap(defaultColumnsPlus(table1ColumnBuilder("column_1").build()))
                                            .foreignKeys(Collections.emptySet())
                                            .build()
                            )
                    },
                    {
                            "02: two tables tests that multiple tables are supported",
                            mapOf(
                                    table1KeyPair(), table1Builder(),
                                    table2KeyPair(), table2Builder()
                            ),
                            mapOf(
                                    "table_1", mapOf("column_1", table1ColumnBuilder("column_1")),
                                    "table_2", mapOf("table_2_column", table2ColumnBuilder())
                            ),
                            mapOf(
                                    "table_1",
                                    table1Builder()
                                            .columnMap(defaultColumnsPlus(table1ColumnBuilder("column_1").build()))
                                            .foreignKeys(Collections.emptySet())
                                            .build(),
                                    "table_2",
                                    table2Builder()
                                            .columnMap(defaultColumnsPlus(table2ColumnBuilder().build()))
                                            .foreignKeys(Collections.emptySet())
                                            .build()
                            )
                    },
            });
        }

        @Override
        protected void updateBuilder(TableContext.Builder tableContextBuilder) {
            // nothing to do. Parent builds all columns into tables
        }
    }

    @RunWith(Parameterized.class)
    public static class ForeignKeys extends TableContextBuilderTest {

        private final Map<String, Map<String, Set<TableForeignKeyInfo.Builder>>> startingForeignKeys;

        public ForeignKeys(String desc,
                           Map<Pair<String, String>, TableInfo.BuilderCompat> startingTables,
                           Map<String, Map<String, ColumnInfo.Builder>> startingColumns,
                           Map<String, Map<String, Set<TableForeignKeyInfo.Builder>>> startingForeignKeys,
                           Map<String, TableInfo> expectedSchema) {
            super(desc, startingTables, startingColumns, expectedSchema);
            this.startingForeignKeys = startingForeignKeys;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {
                            "00: two tables, one with a non-composite foreign key to the other Tests basic foreign key building",
                            mapOf(
                                    table1KeyPair(), table1Builder(),
                                    table2KeyPair(), table2Builder()
                            ),
                            mapOf(
                                    "table_1", mapOf("column_1", table1ColumnBuilder("column_1")),
                                    "table_2", mapOf("table_2_column", table2ColumnBuilder())
                            ),
                            mapOf(
                                    "table_2",
                                    mapOf("", setOf(foreignKeyToTable1Builder("table_2_column", "_id")))
                            ),
                            mapOf(
                                    "table_1",
                                    table1Builder()
                                            .columnMap(defaultColumnsPlus(table1ColumnBuilder("column_1").build()))
                                            .foreignKeys(Collections.emptySet())
                                            .build(),
                                    "table_2",
                                    table2Builder()
                                            .columnMap(defaultColumnsPlus(table2ColumnBuilder().build()))
                                            .foreignKeys(setOf(
                                                    foreignKeyToTable1Builder("table_2_column", "_id").build()
                                            )).build()
                            )
                    },
                    {
                            "01: two tables, one with a composite foreign key to the other and one with a composite primary key Tests the collapsing of composites into a single foreign key with multiple columns",
                            mapOf(
                                    table1KeyPair(),
                                    table1Builder()
                                            .primaryKey(setOf("_id", "column_1"))
                                            .primaryKeyOnConflict("REPLACE"),
                                    table2KeyPair(),
                                    table2Builder()
                            ),
                            mapOf(
                                    "table_1", mapOf("column_1", table1ColumnBuilder("column_1").unique(true)),
                                    "table_2", mapOf(
                                            "table_2_column", table2ColumnBuilder(),
                                            "table_2_string_column", table2StringColumnBuilder()
                                    )
                            ),
                            mapOf(
                                    "table_2",
                                    mapOf(
                                            UUID.randomUUID().toString(),   // <-- this is the composite id
                                            setOf(
                                                    foreignKeyToTable1Builder("table_2_column", "_id"),
                                                    foreignKeyToTable1Builder("table_2_string_column", "column_1")
                                            )
                                    )
                            ),
                            mapOf(
                                    "table_1",
                                    table1Builder()
                                            .primaryKey(setOf("_id", "column_1"))
                                            .primaryKeyOnConflict("REPLACE")
                                            .columnMap(defaultColumnsPlus(table1ColumnBuilder("column_1").unique(true).build()))
                                            .foreignKeys(Collections.emptySet())
                                            .build(),
                                    "table_2",
                                    table2Builder()
                                            .columnMap(defaultColumnsPlus(
                                                    table2ColumnBuilder().build(),
                                                    table2StringColumnBuilder().build()
                                            ))
                                            .foreignKeys(setOf(
                                                    foreignKeyToTable1Builder("table_2_column", "_id", "table_2_string_column", "column_1")
                                                            .build())
                                            ).build()
                            )
                    },
                    {
                            "02: three tables-one with multiple non-composite foreign keys tests that keys without their composite ID will not collapse into the same foreign key",
                            mapOf(
                                    table1KeyPair(), table1Builder(),
                                    table2KeyPair(), table2Builder(),
                                    table3KeyPair(), table3Builder()
                            ),
                            mapOf(
                                    "table_1", mapOf("column_1", table1ColumnBuilder("column_1")),
                                    "table_2", mapOf(
                                            "table_2_column", table2ColumnBuilder(),
                                            "table_2_string_column", table2StringColumnBuilder(),
                                            "table_2_second_long_column", table2SecondLongColumnBuilder()
                                    )
                            ),
                            mapOf(
                                    "table_2",
                                    mapOf(
                                            "",
                                            setOf(
                                                    foreignKeyToTable1Builder("table_2_column", "_id"),
                                                    foreignKeyToTable3Builder("table_2_second_long_column", "_id")
                                            )
                                    )
                            ),
                            mapOf(
                                    "table_1",
                                    table1Builder()
                                            .columnMap(defaultColumnsPlus(table1ColumnBuilder("column_1").build()))
                                            .foreignKeys(Collections.emptySet())
                                            .build(),
                                    "table_2",
                                    table2Builder()
                                            .columnMap(defaultColumnsPlus(
                                                    table2ColumnBuilder().build(),
                                                    table2StringColumnBuilder().build(),
                                                    table2SecondLongColumnBuilder().build()
                                            )).foreignKeys(setOf(
                                                    foreignKeyToTable1Builder("table_2_column", "_id").build(),
                                                    foreignKeyToTable3Builder("table_2_second_long_column", "_id").build()
                                            )).build(),
                                    "table_3",
                                    table3Builder()
                                            .columnMap(defaultColumnsPlus())
                                            .foreignKeys(Collections.emptySet())
                                            .build()
                            )
                    }
            });
        }

        @Override
        protected void updateBuilder(TableContext.Builder tableContextBuilder) {
            startingTables.forEach((tableKeys, tableBuilder) -> {
                startingForeignKeys.getOrDefault(tableKeys.first(), Collections.emptyMap())
                        .forEach((compositeId, foreignKeyBuilders) -> {
                            foreignKeyBuilders.forEach(builder -> tableContextBuilder.addForeignKeyInfo(tableKeys.first(), compositeId, builder));
                        });
            });
        }

        @Test
        public void shouldGenerateExpectedForeignKeys() {
            expectedSchema.forEach((tableKey, expectedTable) -> {
                Set<TableForeignKeyInfo> expectedForeignKeys = expectedTable.foreignKeys();
                Set<TableForeignKeyInfo> actualForeignKeys = actual.tableMap().get(tableKey).foreignKeys();
                assertSetEquals(desc, expectedForeignKeys, actualForeignKeys);
            });
        }
    }

    @RunWith(Parameterized.class)
    public static class Indices extends TableContextBuilderTest {

        private final Map<String, Map<String, List<TableIndexInfo>>> startingTableIndices;

        public Indices(String desc, Map<Pair<String, String>,
                       TableInfo.BuilderCompat> startingTables,
                       Map<String, Map<String, ColumnInfo.Builder>> startingColumns,
                       Map<String, Map<String, List<TableIndexInfo>>> startingTableIndices,
                       Map<String, TableInfo> expectedSchema) {
            super(desc, startingTables, startingColumns, expectedSchema);
            this.startingTableIndices = startingTableIndices;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {
                            "00: a single, non-composite, non-unique tableBuilder index with default sort order",
                            mapOf(table1KeyPair(), table1Builder()),
                            mapOf("table_1", mapOf("column_1", table1ColumnBuilder("column_1"))),
                            mapOf(
                                    "table_1",
                                    mapOf(
                                            "",
                                            Collections.singletonList(TableIndexInfoUtil.nonUniqueDefaultSorts("column_1"))
                                    )
                            ),
                            mapOf(
                                    "table_1",
                                    table1Builder()
                                            .columnMap(defaultColumnsPlus(table1ColumnBuilder("column_1").build()))
                                            .foreignKeys(Collections.emptySet())
                                            .indices(setOf(TableIndexInfoUtil.nonUniqueDefaultSorts("column_1")))
                                            .build()
                            )
                    },
                    {
                            "01: a single, non-composite, unique tableBuilder index with default sort order",
                            mapOf(table1KeyPair(), table1Builder()),
                            mapOf("table_1", mapOf("column_1", table1ColumnBuilder("column_1"))),
                            mapOf(
                                    "table_1",
                                    mapOf(
                                            "",
                                            Collections.singletonList(TableIndexInfoUtil.uniqueDefaultSorts("column_1"))
                                    )
                            ),
                            mapOf(
                                    "table_1",
                                    table1Builder()
                                            .columnMap(defaultColumnsPlus(table1ColumnBuilder("column_1").build()))
                                            .foreignKeys(Collections.emptySet())
                                            .indices(setOf(TableIndexInfoUtil.uniqueDefaultSorts("column_1")))
                                            .build()
                            )
                    },
                    {
                            "02: a single, non-composite, non-unique tableBuilder index with ASC sort order",
                            mapOf(table1KeyPair(), table1Builder()),
                            mapOf("table_1", mapOf("column_1", table1ColumnBuilder("column_1"))),
                            mapOf(
                                    "table_1",
                                    mapOf(
                                            "",
                                            Collections.singletonList(TableIndexInfoUtil.nonUniqueIndex("column_1", "ASC"))
                                    )
                            ),
                            mapOf(
                                    "table_1",
                                    table1Builder()
                                            .columnMap(defaultColumnsPlus(table1ColumnBuilder("column_1").build()))
                                            .foreignKeys(Collections.emptySet())
                                            .indices(setOf(TableIndexInfoUtil.nonUniqueIndex("column_1", "ASC")))
                                            .build()
                            )
                    },
                    {
                            "03: a single, non-composite, unique tableBuilder index with DESC sort order",
                            mapOf(table1KeyPair(), table1Builder()),
                            mapOf("table_1", mapOf("column_1", table1ColumnBuilder("column_1"))),
                            mapOf(
                                    "table_1",
                                    mapOf(
                                            "",
                                            Collections.singletonList(TableIndexInfoUtil.uniqueIndex("column_1", "DESC"))
                                    )
                            ),
                            mapOf(
                                    "table_1",
                                    table1Builder()
                                            .columnMap(defaultColumnsPlus(table1ColumnBuilder("column_1").build()))
                                            .foreignKeys(Collections.emptySet())
                                            .indices(setOf(TableIndexInfoUtil.uniqueIndex("column_1", "DESC")))
                                            .build()
                            )
                    },
                    {
                            "04: multiple, non-composite, tableBuilder indices with ASC and DESC sort order respectively--ensures non-composited indices show up as separate indices",
                            mapOf(table1KeyPair(), table1Builder()),
                            mapOf("table_1", mapOf(
                                    "column_1", table1ColumnBuilder("column_1"),
                                    "column_2", table1ColumnBuilder("column_2"))
                            ),
                            mapOf(
                                    "table_1",
                                    mapOf(
                                            "",
                                            Arrays.asList(
                                                    TableIndexInfoUtil.uniqueIndex("column_1", "ASC"),
                                                    TableIndexInfoUtil.nonUniqueIndex("column_2", "DESC")
                                            )
                                    )
                            ),
                            mapOf(
                                    "table_1",
                                    table1Builder()
                                            .columnMap(defaultColumnsPlus(
                                                    table1ColumnBuilder("column_1").build(),
                                                    table1ColumnBuilder("column_2").build()
                                            )).foreignKeys(Collections.emptySet())
                                            .indices(setOf(
                                                    TableIndexInfoUtil.uniqueIndex("column_1", "ASC"),
                                                    TableIndexInfoUtil.nonUniqueIndex("column_2", "DESC")
                                            )).build()
                            )
                    },
                    {
                            "05: composite indices (three in the composite), get correctly composited",
                            mapOf(table1KeyPair(), table1Builder()),
                            mapOf("table_1", mapOf(
                                    "column_1", table1ColumnBuilder("column_1"),
                                    "column_2", table1ColumnBuilder("column_2"),
                                    "column_3", table1ColumnBuilder("column_3"))
                            ),
                            mapOf(
                                    "table_1",
                                    mapOf(
                                            "my_composite_index",
                                            Arrays.asList(
                                                    TableIndexInfoUtil.uniqueIndex("column_1", "ASC"),
                                                    TableIndexInfoUtil.uniqueIndex("column_2", "DESC"),
                                                    TableIndexInfoUtil.uniqueIndex("column_3", "")
                                            )
                                    )
                            ),
                            mapOf(
                                    "table_1",
                                    table1Builder()
                                            .columnMap(defaultColumnsPlus(
                                                    table1ColumnBuilder("column_1").build(),
                                                    table1ColumnBuilder("column_2").build(),
                                                    table1ColumnBuilder("column_3").build()
                                            )).foreignKeys(Collections.emptySet())
                                            .indices(setOf(
                                                    TableIndexInfoUtil.uniqueIndex(
                                                            "column_1", "ASC",
                                                            "column_2", "DESC",
                                                            "column_3", "")
                                            )).build()
                            )
                    },
                    {
                            "06: composite indices get correctly composited when non-composite indices also exist",
                            mapOf(table1KeyPair(), table1Builder()),
                            mapOf("table_1", mapOf(
                                    "column_1", table1ColumnBuilder("column_1"),
                                    "column_2", table1ColumnBuilder("column_2"),
                                    "column_3", table1ColumnBuilder("column_3"))
                            ),
                            mapOf(
                                    "table_1",
                                    mapOf(
                                            "",
                                            Collections.singletonList(TableIndexInfoUtil.uniqueIndex("column_3", "")),
                                            "my_composite_index",
                                            Arrays.asList(
                                                    TableIndexInfoUtil.nonUniqueIndex("column_1", "ASC"),
                                                    TableIndexInfoUtil.nonUniqueIndex("column_2", "DESC")
                                            )
                                    )
                            ),
                            mapOf(
                                    "table_1",
                                    table1Builder()
                                            .columnMap(defaultColumnsPlus(
                                                    table1ColumnBuilder("column_1").build(),
                                                    table1ColumnBuilder("column_2").build(),
                                                    table1ColumnBuilder("column_3").build()
                                            )).foreignKeys(Collections.emptySet())
                                            .indices(setOf(
                                                    TableIndexInfoUtil.uniqueDefaultSorts("column_3"),
                                                    TableIndexInfoUtil.nonUniqueIndex("column_1", "ASC", "column_2", "DESC")
                                            )).build()
                            )
                    },
                    {
                            "07: composite index with just one entry should work just fine",
                            mapOf(table1KeyPair(), table1Builder()),
                            mapOf("table_1", mapOf("column_1", table1ColumnBuilder("column_1"))),
                            mapOf(
                                    "table_1",
                                    mapOf(
                                            "my_composite_index",
                                            Arrays.asList(
                                                    TableIndexInfoUtil.uniqueDefaultSorts("column_1")
                                            )
                                    )
                            ),
                            mapOf(
                                    "table_1",
                                    table1Builder()
                                            .columnMap(defaultColumnsPlus(table1ColumnBuilder("column_1").build()))
                                            .foreignKeys(Collections.emptySet())
                                            .indices(setOf(TableIndexInfoUtil.uniqueDefaultSorts("column_1")))
                                            .build()
                            )
                    }
            });
        }

        @Test
        public void shouldGenerateExpectedTableIndices() {
            expectedSchema.forEach((tableKey, expectedTable) -> {
                Set<TableIndexInfo> expectedIndices = expectedTable.indices();
                Set<TableIndexInfo> actualIndices = actual.tableMap().get(tableKey).indices();
                assertSetEquals(desc, expectedIndices, actualIndices);
            });
        }

        @Override
        protected void updateBuilder(TableContext.Builder tableContextBuilder) {
            startingTables.forEach((tableKeys, tableBuilder) -> {
                startingTableIndices.getOrDefault(tableKeys.first(), Collections.emptyMap())
                        .forEach((compositeId, tableIndexInfoList) -> {
                            tableIndexInfoList.forEach(tio -> tableContextBuilder.addTableIndexInfo(tableKeys.first(), compositeId, tio));
                        });
            });
        }
    }

    private static Pair<String, String> table1KeyPair() {
        return Pair.of("table_1", TableContextBuilderTest.class.getName());
    }

    private static TableInfo.BuilderCompat table1Builder() {
        return TableInfo.builder()
                .tableName("table_1")
                .primaryKey(setOf(TableInfo.DEFAULT_PRIMARY_KEY_COLUMN))
                .qualifiedClassName(TableContextBuilderTest.class.getName())
                .primaryKeyOnConflict("")
                .staticDataAsset("")
                .staticDataRecordName("");
    }

    private static ColumnInfo.Builder table1ColumnBuilder(String columnName) {
        return ColumnInfo.builder()
                .columnName(columnName)
                .methodName(columnName + "Method")
                .qualifiedType(String.class.getName());
    }

    private static TableForeignKeyInfo.Builder foreignKeyToTable1Builder(String... localToForeignColumns) {
        return TableForeignKeyInfoUtil.baseBuilder("table_1", localToForeignColumns)
                .foreignTableApiClassName(TableContextBuilderTest.class.getName());
    }

    private static Pair<String, String> table2KeyPair() {
        return Pair.of("table_2", ForeignKeys.class.getName());
    }

    private static TableInfo.BuilderCompat table2Builder() {
        return TableInfo.builder()
                .tableName("table_2")
                .primaryKey(setOf(TableInfo.DEFAULT_PRIMARY_KEY_COLUMN))
                .qualifiedClassName(ForeignKeys.class.getName())
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
        return Pair.of("table_3", FSAnnotationProcessor.class.getName());
    }

    private static TableInfo.BuilderCompat table3Builder() {
        return TableInfo.builder()
                .tableName("table_3")
                .primaryKey(setOf(TableInfo.DEFAULT_PRIMARY_KEY_COLUMN))
                .qualifiedClassName(FSAnnotationProcessor.class.getName())
                .primaryKeyOnConflict("")
                .staticDataAsset("")
                .staticDataRecordName("");
    }

    private static TableForeignKeyInfo.Builder foreignKeyToTable3Builder(String... localToForeignColumns) {
        return TableForeignKeyInfoUtil.baseBuilder("table_3", localToForeignColumns)
                .foreignTableApiClassName(FSAnnotationProcessor.class.getName());
    }

    private static Map<String, ColumnInfo> defaultColumnsPlus(ColumnInfo... columns) {
        Map<String, ColumnInfo> ret = TableInfo.defaultColumns();
        Arrays.stream(columns).forEach(column -> ret.put(column.columnName(), column));
        return ret;
    }
}
