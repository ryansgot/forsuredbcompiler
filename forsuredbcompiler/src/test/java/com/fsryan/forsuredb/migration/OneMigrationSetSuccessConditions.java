package com.fsryan.forsuredb.migration;

import com.fsryan.forsuredb.info.TableInfo;
import com.google.common.collect.ImmutableMap;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.fsryan.forsuredb.TestData.*;

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
                        tableMapOf(defaultPkTable("table1").build())
                },
                {   // 01: one table with one extra non-unique column
                        Arrays.asList(
                                createTableMigration("table1"),
                                addColumnMigration("table1").columnName(longCol().build().getColumnName()).build()
                        ),
                        tableMapOf(defaultPkTable("table1")
                                .addColumn(longCol().build())
                                .build())
                },
                {   // 02: one table with one two extra non-unique columns
                        Arrays.asList(
                                createTableMigration("table1"),
                                addColumnMigration("table1").columnName(longCol().build().getColumnName()).build(),
                                addColumnMigration("table1").columnName(stringCol().build().getColumnName()).build()
                        ),
                        tableMapOf(defaultPkTable("table1")
                                .addColumn(longCol().build())
                                .addColumn(stringCol().build())
                                .build())
                },
                {   // 03: two tables--composite primary and foreign keys
                        Arrays.asList(createTableMigration("table1"), createTableMigration("table2")),
                        tableMapOf(defaultPkTable("table1")
                                .addColumn(stringCol().columnName("table2_project").build())
                                .addColumn(longCol().columnName("table2_build").build())
                                .addForeignKey(cascadeTFKI("table2")
                                        .localToForeignColumnMap(ImmutableMap.of(
                                                "table2_project", "project",
                                                "table2_build", "build")
                                        ).build())
                                .build(),
                                table("table2")
                                        .addColumn(stringCol().columnName("project").build())
                                        .addColumn(longCol().columnName("build").build())
                                        .resetPrimaryKey(new HashSet<>(Arrays.asList("project", "build")))
                                        .build()
                        )
                },
                {   // 04: one table with a column that has an index
                        Arrays.asList(
                                createTableMigration("table1"),
                                addColumnMigration("table1").columnName("table_1_index").build(),
                                addIndexMigration("table1").columnName("table_1_index").build()
                        ),
                        tableMapOf(table("table1")
                                .addColumn(longCol().columnName("table_1_index").index(true).build())
                                .build()
                        )
                }
        });
    }
}
