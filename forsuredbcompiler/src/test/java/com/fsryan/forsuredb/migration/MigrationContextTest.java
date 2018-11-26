package com.fsryan.forsuredb.migration;

import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.api.migration.MigrationRetriever;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class MigrationContextTest {

    private final List<MigrationSet> input;
    private final int latestDbVersion;
    protected final Map<String, TableInfo> expectedSchema;

    private MigrationContext migrationContext;

    public MigrationContextTest(List<MigrationSet> input, int latestDbVersion, Map<String, TableInfo> expectedSchema) {
        this.input = input;
        this.latestDbVersion = latestDbVersion;
        this.expectedSchema = expectedSchema;
    }

    @Before
    public void generateMigrationContext() {
        MigrationRetriever mockMigrationRetriever = mock(MigrationRetriever.class);
        when(mockMigrationRetriever.getMigrationSets()).thenReturn(input);
        when(mockMigrationRetriever.latestDbVersion()).thenReturn(latestDbVersion);
        migrationContext = new MigrationContext(mockMigrationRetriever);
    }

    @Test
    public void shouldHaveAllExpectedTables() {
        // All expected tables exist
        expectedSchema.keySet()
                .forEach(expectedTableName -> assertTrue("missing table: " + expectedTableName, migrationContext.hasTableWithName(expectedTableName)));
        // no unexpected tables exist
        migrationContext.allTables()
                .forEach(actualTable -> assertTrue("unexpected table: " + actualTable, expectedSchema.containsKey(actualTable.tableName())));
    }

    @Test
    public void allTablesShouldHaveCorrectPrimaryKey() {
        expectedSchema.values().forEach(expectedTable -> {
            TableInfo actualTable = migrationContext.getTableByName(expectedTable.tableName());

            assertNotNull(actualTable);
            assertEquals(expectedTable.getPrimaryKey(), actualTable.getPrimaryKey());
            assertEquals(expectedTable.primaryKeyOnConflict(), actualTable.primaryKeyOnConflict());
        });
    }

    @Test
    public void allTablesShouldHaveCorrectForeignKeys() {
        expectedSchema.values().forEach(expectedTable -> {
            TableInfo actualTable = migrationContext.getTableByName(expectedTable.tableName());
            assertNotNull("Did not find table: " + expectedTable.tableName(), actualTable);
            assertEquals("table: " + expectedTable.tableName(), expectedTable.foreignKeys(), actualTable.foreignKeys());
        });
    }

    @Test
    public void allTablesShouldHaveCorrectColumns() {
        expectedSchema.values().forEach(expectedTable -> expectedTable.getColumns().forEach(c -> {
                final TableInfo actualTable = migrationContext.getTableByName(expectedTable.tableName());
                assertNotNull(actualTable);

                final Map<String, ColumnInfo> expectedColumns = expectedTable.getColumns().stream().collect(toMap(ColumnInfo::getColumnName, identity()));
                final Map<String, ColumnInfo> actualColumns = actualTable.getColumns().stream().collect(toMap(ColumnInfo::getColumnName, identity()));

                expectedColumns.forEach((expectedName, expectedColumn) -> assertEquals(expectedColumn, actualColumns.get(expectedName)));
                actualColumns.forEach((actualName, actualColumn) -> assertTrue("extra column: " + actualColumn, expectedColumns.containsKey(actualName)));
            }));
    }

    public static abstract class OneMigrationSetTest extends MigrationContextTest {
        public OneMigrationSetTest(List<Migration> migrations, Map<String, TableInfo> expectedSchema) {
            super(Arrays.asList(MigrationSet.builder()
                    .orderedMigrations(migrations)
                    .dbVersion(1)
                    .targetSchema(expectedSchema)
                    .build()),1, expectedSchema);
        }
    }

    public static abstract class TwoMigrationSetTest extends MigrationContextTest {
        public TwoMigrationSetTest(List<Migration> firstMigrations,
                                   Map<String, TableInfo> firstExpectedSchema,
                                   List<Migration> secondMigrations,
                                   Map<String, TableInfo> secondExpectedSchema) {
            super(Arrays.asList(MigrationSet.builder()
                            .orderedMigrations(firstMigrations)
                            .dbVersion(1)
                            .targetSchema(firstExpectedSchema)
                            .build(),
                    MigrationSet.builder()
                            .orderedMigrations(secondMigrations)
                            .dbVersion(2)
                            .targetSchema(secondExpectedSchema)
                            .build()),
                    2,
                    secondExpectedSchema);
        }
    }
}
