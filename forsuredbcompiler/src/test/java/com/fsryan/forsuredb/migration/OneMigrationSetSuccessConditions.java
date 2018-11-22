package com.fsryan.forsuredb.migration;

import com.fsryan.forsuredb.info.TableIndexInfo;
import com.fsryan.forsuredb.info.TableInfo;
import com.google.common.collect.ImmutableMap;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.fsryan.forsuredb.TestData.*;
import static com.fsryan.forsuredb.test.tools.CollectionUtil.mapOf;

@RunWith(Parameterized.class)
public class OneMigrationSetSuccessConditions extends MigrationContextTest.OneMigrationSetTest {

    public OneMigrationSetSuccessConditions(List<Migration> migrations, Map<String, TableInfo> expectedSchema) {
        super(migrations, expectedSchema);
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {   // 00: one table with no extra columns
                        Arrays.asList(createTableMigration("table1")),
                        newTableContext()
                                .addTable(defaultPkTable("table1").build())
                                .build()
                                .tableMap()
                },
                {   // 01: one table with one extra non-unique column
                        Arrays.asList(
                                createTableMigration("table1"),
                                addColumnMigration("table1").columnName(longCol().build().getColumnName()).build()
                        ),
                        newTableContext()
                                .addTable(defaultPkTable("table1")
                                        .addToColumns(longCol().build())
                                        .build())
                                .build()
                                .tableMap()
                },
                {   // 02: one table with one two extra non-unique columns
                        Arrays.asList(
                                createTableMigration("table1"),
                                addColumnMigration("table1").columnName(longCol().build().getColumnName()).build(),
                                addColumnMigration("table1").columnName(stringCol().build().getColumnName()).build()
                        ),
                        newTableContext()
                                .addTable(defaultPkTable("table1")
                                        .addToColumns(longCol().build())
                                        .addToColumns(stringCol().build())
                                        .build())
                                .build()
                                .tableMap()
                },
                {   // 03: two tables--composite primary and foreign keys
                        Arrays.asList(createTableMigration("table1"), createTableMigration("table2")),
                        newTableContext()
                                .addTable(defaultPkTable("table1")
                                        .addToColumns(stringCol().columnName("table2_project").build())
                                        .addToColumns(longCol().columnName("table2_build").build())
                                        .addTableForeignKey(cascadeTFKI("table2")
                                                .localToForeignColumnMap(ImmutableMap.of(
                                                        "table2_project", "project",
                                                        "table2_build", "build")
                                                ).build())
                                        .build())
                                .addTable(table("table2")
                                        .addToColumns(stringCol().columnName("project").build())
                                        .addToColumns(longCol().columnName("build").build())
                                        .addToPrimaryKey("project")
                                        .addToPrimaryKey("build")
                                        .build())
                                .build()
                                .tableMap()
                },
                {   // 04: one table with a column that has an index
                        Arrays.asList(
                                createTableMigration("table1"),
                                addColumnMigration("table1").columnName("table_1_index").build(),
                                addIndexMigration("table1").columnName("table_1_index").build()
                        ),
                        newTableContext()
                                .addTable(table("table1")
                                        .addToColumns(longCol().columnName("table_1_index").index(true).build())
                                        .build())
                                .build()
                                .tableMap()
                },
                {   // 05: one table with a composite index
                        Arrays.asList(
                                createTableMigration("table1"),
                                addColumnMigration("table1").columnName("table_1_index_col1").build(),
                                addColumnMigration("table1").columnName("table_1_index_col2").build(),
                                addIndexMigration("table1")
                                        .extras(mapOf("order", "table_1_index_col1,table_1_index_col2"))
                                        .build()
                        ),
                        newTableContext()
                                .addTable(table("table1")
                                        .addToColumns(longCol().columnName("table_1_index_col1").index(true).build())
                                        .addToColumns(longCol().columnName("table_1_index_col2").index(true).build())
                                        .addIndex(TableIndexInfo.create(false, Arrays.asList("table_1_index_col1", "table_1_index_col2"), Arrays.asList("", "")))
                                        .build())
                                .build()
                                .tableMap()
                }
        });
    }
}
