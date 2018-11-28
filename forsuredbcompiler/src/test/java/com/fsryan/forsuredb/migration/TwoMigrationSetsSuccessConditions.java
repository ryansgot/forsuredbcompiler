package com.fsryan.forsuredb.migration;

import com.fsryan.forsuredb.info.TableInfo;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static com.fsryan.forsuredb.info.ColumnInfoUtil.colNameByType;
import static com.fsryan.forsuredb.info.DBInfoFixtures.*;
import static com.fsryan.forsuredb.info.TableInfoUtil.tableMapOf;
import static com.fsryan.forsuredb.migration.MigrationFixtures.*;
import static com.fsryan.forsuredb.test.tools.CollectionUtil.mapOf;
import static com.fsryan.forsuredb.test.tools.CollectionUtil.setOf;

@RunWith(Parameterized.class)
public class TwoMigrationSetsSuccessConditions extends MigrationContextTest.TwoMigrationSetTest {

    public TwoMigrationSetsSuccessConditions(List<Migration> firstMigrations,
                                             Map<String, TableInfo> firstExpectedSchema,
                                             List<Migration> secondMigrations,
                                             Map<String, TableInfo> secondExpectedSchema) {
        super(firstMigrations, firstExpectedSchema, secondMigrations, secondExpectedSchema);
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {   // 00: one table with no extra columns, add a column
                        Collections.singletonList(createTableMigration("table1")),
                        tableMapOf(tableBuilder("table1").build()),
                        Collections.singletonList(addColumnMigration("table1")
                                .columnName(colNameByType(long.class))
                                .build()),
                        tableMapOf(tableBuilder("table1", longCol().build()).build())
                },
                {   // 01: one table with no extra columns, add two columns
                        Collections.singletonList(createTableMigration("table1")),
                        tableMapOf(tableBuilder("table1").build()),
                        Arrays.asList(
                                addColumnMigration("table1").columnName(colNameByType(long.class)).build(),
                                addColumnMigration("table1").columnName(colNameByType(String.class)).build()
                        ),
                        tableMapOf(
                                tableBuilder("table1")
                                        .addColumn(longCol().build())
                                        .addColumn(stringCol().build())
                                        .build()
                        )
                },
                {   // 02: add new foreign key with the legacy ADD_FOREIGN_KEY_REFERENCE migration
                        Arrays.asList(
                                createTableMigration("table1"),
                                createTableMigration("table2")
                        ),
                        tableMapOf(
                                tableBuilder("table1").build(),
                                tableBuilder("table2").build()
                        ),
                        Collections.singletonList(
                                addForeignKeyReferenceMigration("table1")
                                        .columnName(longCol().build().getColumnName())
                                        .build()
                        ),
                        tableMapOf(
                                tableBuilder("table1")
                                        .addColumn(longCol()
                                                .foreignKeyInfo(idCascadeFKI("table2"))
                                                .build()
                                        ).addForeignKey(foreignKeyTo("table2")
                                                .localToForeignColumnMap(mapOf(colNameByType(long.class), "_id"))
                                                .build()
                                        ).build(),
                                tableBuilder("table2").build()
                        )
                },
                {   // 03: add new foreign key with the legacy UPDATE_FOREIGN_KEYS migration
                        Arrays.asList(
                                createTableMigration("table1"),
                                createTableMigration("table2")
                        ),
                        tableMapOf(
                                tableBuilder("table1").build(),
                                tableBuilder("table2").build()
                        ),
                        Collections.singletonList(
                                updateForeignKeysMigration("table1")
                                        .putExtra("existing_column_names", "[\"_id\",\"created\",\"deleted\",\"modified\"]")
                                        .putExtra("current_foreign_keys", "[]")
                                        .build()
                        ),
                        tableMapOf(
                                tableBuilder("table1")
                                        .addColumn(longCol()
                                                .foreignKeyInfo(idCascadeFKI("table2"))
                                                .build()
                                        ).addForeignKey(cascadeForeignKeyTo("table2")
                                                .localToForeignColumnMap(mapOf(colNameByType(long.class), "_id"))
                                                .build()
                                        ).build(),
                                tableBuilder("table2").build()
                        )
                },
                {   // 04: update the primary key with a new column
                        Collections.singletonList(createTableMigration("table1")),
                        tableMapOf(tableBuilder("table1").build()),
                        Collections.singletonList(updatePrimaryKeyMigration("table1").build()),
                        tableMapOf(
                                tableBuilder("table1")
                                        .resetPrimaryKey(Collections.singleton(colNameByType(long.class)))
                                        .addColumn(longCol().primaryKey(true).build())
                                        .build()
                        )
                },
                {   // 05: two tables--composite primary and foreign keys in first migration--add one column in next
                        Arrays.asList(
                                createTableMigration("table1"),
                                createTableMigration("table2")
                        ),
                        tableMapOf(
                                tableBuilder("table1")
                                        .addColumn(stringCol().columnName("table2_project").build())
                                        .addColumn(longCol().columnName("table2_build").build())
                                        .addForeignKey(cascadeForeignKeyTo("table2")
                                                .localToForeignColumnMap(mapOf(
                                                        "table2_project", "project",
                                                        "table2_build", "build")
                                                ).build()
                                        ).build(),
                                tableBuilder("table2")
                                        .addColumn(stringCol().columnName("project").build())
                                        .addColumn(longCol().columnName("build").build())
                                        .resetPrimaryKey(setOf("project", "build"))
                                        .build()),
                        Collections.singletonList(
                                addColumnMigration("table1")
                                        .columnName(colNameByType(long.class))
                                        .build()
                        ),
                        tableMapOf(
                                tableBuilder("table1")
                                        .addColumn(stringCol().columnName("table2_project").build())
                                        .addColumn(longCol().columnName("table2_build").build())
                                        .addColumn(longCol().build())
                                        .addForeignKey(cascadeForeignKeyTo("table2")
                                                .localToForeignColumnMap(mapOf(
                                                        "table2_project", "project",
                                                        "table2_build", "build")
                                                ).build()
                                        ).build(),
                                tableBuilder("table2")
                                        .addColumn(stringCol().columnName("project").build())
                                        .addColumn(longCol().columnName("build").build())
                                        .resetPrimaryKey(setOf("project", "build"))
                                        .build()
                        )
                },
                {   // 06: update an existing column's default value when it did not have one previously
                        Arrays.asList(
                                createTableMigration("table1"),
                                addColumnMigration("table1")
                                        .columnName(colNameByType(String.class))
                                        .build()
                        ),
                        tableMapOf(
                                tableBuilder("table1")
                                        .addColumn(stringCol().build())
                                        .build()
                        ),
                        Collections.singletonList(
                                changeDefaultValueMigration("table1")
                                        .columnName(stringCol().build().getColumnName())
                                        .build()
                        ),
                        tableMapOf(
                                tableBuilder("table1")
                                        .addColumn(stringCol().defaultValue("some default").build())
                                        .build()
                        )
                },
                {   // 07: one table with a column that has an index added after creation
                        Arrays.asList(
                                createTableMigration("table1"),
                                addColumnMigration("table1").columnName("table_1_index").build()
                        ),
                        tableMapOf(
                                tableBuilder("table1")
                                        .addColumn(longCol().columnName("table_1_index").build())
                                        .build()
                        ),
                        Collections.singletonList(
                                addIndexMigration("table1")
                                        .columnName("table_1_index")
                                        .build()
                        ),
                        tableMapOf(
                                tableBuilder("table1")
                                        .addColumn(longCol().columnName("table_1_index").index(true).build())
                                        .build()
                        )

                }
        });
    }
}
