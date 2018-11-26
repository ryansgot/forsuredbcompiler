package com.fsryan.forsuredb.annotationprocessor.generator;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.migration.Migration;
import com.fsryan.forsuredb.migration.MigrationSet;
import com.google.common.collect.Lists;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static com.fsryan.forsuredb.TestData.*;

/**
 * <p>
 *     Rather larger scale tests that are designed to mimic what a person will do with forsuredb
 *     initially. Most of the time, the first time the dbmigrate task is run, it will contain a
 *     lot of migrations
 * </p>
 */
@RunWith(Parameterized.class)
public class ZeroToOneDiffGeneratorTest extends BaseDiffGeneratorTest {

    public ZeroToOneDiffGeneratorTest(TableContext targetContext, MigrationSet expectedMigrationSet) {
        super(0, newTableContext().build(), targetContext, expectedMigrationSet);
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {   // 00: test_table and test_table2 where test_table2 has foreign key to test_table
                        TableContext.fromSchema(tableMapOf(table()
                                        .tableName("test_table")
                                        .addColumn(stringCol().columnName("non_unique_index_column").index(true).build())
                                        .addColumn(stringCol().columnName("unique_index_column").unique(true).index(true).build())
                                        .build(),
                                table()
                                        .tableName("test_table2")
                                        .addColumn(stringCol()
                                                .columnName("test_table_unique_index_column")
                                                .foreignKeyInfo(cascadeFKI("test_table")
                                                        .apiClassName("com.fsryan.forsuredb.annotationprocessor.generator.TestTable")
                                                        .columnName("unique_index_column")
                                                        .build())
                                                .build())
                                        .build()
                                )),
                        MigrationSet.builder()
                                .dbVersion(1)
                                .orderedMigrations(Lists.newArrayList(
                                        Migration.builder()
                                                .tableName("test_table")
                                                .type(Migration.Type.CREATE_TABLE)
                                                .build(),
                                        Migration.builder()
                                                .tableName("test_table2")
                                                .type(Migration.Type.CREATE_TABLE)
                                                .build(),
                                        Migration.builder()
                                                .tableName("test_table")
                                                .columnName("non_unique_index_column")
                                                .type(Migration.Type.ALTER_TABLE_ADD_COLUMN)
                                                .build(),
                                        Migration.builder()
                                                .tableName("test_table")
                                                .columnName("unique_index_column")
                                                .type(Migration.Type.ALTER_TABLE_ADD_UNIQUE)
                                                .build()
                                ))
                                .targetSchema(tableMapOf(table()
                                        .tableName("test_table")
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
                                        table()
                                                .tableName("test_table2")
                                                .addColumn(stringCol()
                                                        .columnName("test_table_unique_index_column")
                                                        .foreignKeyInfo(cascadeFKI("test_table")
                                                                .apiClassName("com.fsryan.forsuredb.annotationprocessor.generator.TestTable")
                                                                .columnName("unique_index_column")
                                                                .build())
                                                        .build())
                                                .build()))
                                .build()
                }
        });
    }
}
