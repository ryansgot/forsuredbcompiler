package com.fsryan.forsuredb.annotationprocessor.generator;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.migration.Migration;
import com.fsryan.forsuredb.migration.MigrationSet;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static com.fsryan.forsuredb.info.DBInfoFixtures.cascadeFKI;
import static com.fsryan.forsuredb.info.DBInfoFixtures.stringCol;
import static com.fsryan.forsuredb.info.DBInfoFixtures.tableBuilder;
import static com.fsryan.forsuredb.info.TableInfoUtil.tableMapOf;
import static com.fsryan.forsuredb.migration.MigrationFixtures.migration;
import static com.fsryan.forsuredb.migration.MigrationSetFixtures.migrationSet;


/**
 * <p>
 *     Rather larger scale tests that are designed to mimic what a person will do with forsuredb
 *     initially. Most of the time, the first time the dbmigrate task is run, it will contain a
 *     lot of migrations
 * </p>
 */
@RunWith(Parameterized.class)
public class ZeroToOneDiffGeneratorTest extends BaseDiffGeneratorTest {

    private static final String TABLE_NAME = "test_table";

    public ZeroToOneDiffGeneratorTest(TableContext targetContext, MigrationSet expectedMigrationSet) {
        super(0, TableContext.empty(), targetContext, expectedMigrationSet);
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {   // 00: test_table and test_table2 where test_table2 has foreign key to test_table
                        TableContext.fromSchema(tableMapOf(
                                tableBuilder(TABLE_NAME)
                                        .addColumn(stringCol().columnName("non_unique_index_column").index(true).build())
                                        .addColumn(stringCol().columnName("unique_index_column").unique(true).index(true).build())
                                        .build(),
                                tableBuilder("test_table2")
                                        .addColumn(stringCol()
                                                .columnName("test_table_unique_index_column")
                                                .foreignKeyInfo(cascadeFKI(TABLE_NAME, "unique_index_column")
                                                        .apiClassName("com.fsryan.forsuredb.annotationprocessor.generator.TestTable")
                                                        .build())
                                                .build())
                                        .build())
                        ),
                        migrationSet(1)
                                .orderedMigrations(Arrays.asList(
                                        migration(Migration.Type.CREATE_TABLE)
                                                .tableName(TABLE_NAME)
                                                .build(),
                                        migration(Migration.Type.CREATE_TABLE)
                                                .tableName("test_table2")
                                                .build(),
                                        migration(Migration.Type.ALTER_TABLE_ADD_COLUMN)
                                                .tableName(TABLE_NAME)
                                                .columnName("non_unique_index_column")
                                                .build(),
                                        migration(Migration.Type.ALTER_TABLE_ADD_UNIQUE)
                                                .tableName(TABLE_NAME)
                                                .columnName("unique_index_column")
                                                .build())
                                ).targetSchema(tableMapOf(tableBuilder(TABLE_NAME)
                                        .tableName(TABLE_NAME)
                                        .addColumn(stringCol()
                                                .columnName("non_unique_index_column")
                                                .index(true)
                                                .build())
                                        .addColumn(stringCol()
                                                .columnName("unique_index_column")
                                                .unique(true)
                                                .index(true)
                                                .build())
                                        .build(),
                                        tableBuilder("test_table2")
                                                .addColumn(stringCol()
                                                        .columnName("test_table_unique_index_column")
                                                        .foreignKeyInfo(cascadeFKI(TABLE_NAME, "unique_index_column")
                                                                .apiClassName("com.fsryan.forsuredb.annotationprocessor.generator.TestTable")
                                                                .build())
                                                        .build()
                                                ).build())
                                ).build()
                }
        });
    }
}
