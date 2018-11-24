package com.fsryan.forsuredb.migration;

import com.fsryan.forsuredb.info.TableInfo;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.fsryan.forsuredb.TestData.*;
import static com.fsryan.forsuredb.test.tools.CollectionUtil.mapOf;

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
                {   // 00: one tableBuilder with no extra columns, add a column
                        Arrays.asList(createTableMigration("table1")),
                        newTableContext()
                                .addTable(defaultPkTable("table1").build())
                                .build()
                                .tableMap(),
                        Arrays.asList(addColumnMigration("table1").columnName(longCol().build().getColumnName()).build()),
                        newTableContext()
                                .addTable(defaultPkTable("table1")
                                        .addToColumns(longCol().build())
                                        .build())
                                .build()
                                .tableMap()
                },
                {   // 01: one tableBuilder with no extra columns, add two columns
                        Arrays.asList(createTableMigration("table1")),
                        newTableContext()
                                .addTable(defaultPkTable("table1").build())
                                .build()
                                .tableMap(),
                        Arrays.asList(
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
                {   // 02: add new foreign key with the legacy ADD_FOREIGN_KEY_REFERENCE migration
                        Arrays.asList(createTableMigration("table1"), createTableMigration("table2")),
                        newTableContext()
                                .addTable(defaultPkTable("table1").build())
                                .addTable(defaultPkTable("table2").build())
                                .build()
                                .tableMap(),
                        Arrays.asList(
                                addForeignKeyReferenceMigration("table1")
                                        .columnName(longCol().build().getColumnName())
                                        .build()
                        ),
                        newTableContext()
                                .addTable(defaultPkTable("table1")
                                        .addTableForeignKey(dbmsDefaultTFKI("table2")
                                                .localToForeignColumnMap(mapOf(longCol().build().getColumnName(), "_id"))
                                                .updateChangeAction("CASCADE")
                                                .deleteChangeAction("CASCADE")
                                                .build())
                                        .addToColumns(longCol()
                                                .foreignKeyInfo(cascadeFKI("table2")
                                                        .apiClassName("table2")
                                                        .build())
                                                .build())
                                        .build())
                                .addTable(defaultPkTable("table2").build())
                                .build()
                                .tableMap()
                },
                {   // 03: add new foreign key with the legacy UPDATE_FOREIGN_KEYS migration
                        Arrays.asList(createTableMigration("table1"), createTableMigration("table2")),
                        newTableContext()
                                .addTable(defaultPkTable("table1").build())
                                .addTable(defaultPkTable("table2").build())
                                .build()
                                .tableMap(),
                        Arrays.asList(
                                updateForeignKeysMigration("table1")
                                        .addExtra("existing_column_names", "[\"_id\",\"created\",\"deleted\",\"modified\"]")
                                        .addExtra("current_foreign_keys", "[]")
                                        .build()
                        ),
                        newTableContext()
                                .addTable(defaultPkTable("table1")
                                        .addTableForeignKey(dbmsDefaultTFKI("table2")
                                                .localToForeignColumnMap(mapOf(longCol().build().getColumnName(), "_id"))
                                                .build())
                                        .addToColumns(longCol()
                                                .foreignKeyInfo(cascadeFKI("table2")
                                                        .apiClassName("table2")
                                                        .build())
                                                .build())
                                        .build())
                                .addTable(defaultPkTable("table2").build())
                                .build()
                                .tableMap()
                },
                {   // 04: update the primary key with a new column
                        Arrays.asList(createTableMigration("table1")),
                        newTableContext()
                                .addTable(defaultPkTable("table1").build())
                                .build()
                                .tableMap(),
                        Arrays.asList(updatePrimaryKeyMigration("table1").build()),
                        newTableContext()
                                .addTable(table("table1")
                                        .addToPrimaryKey(longCol().build().getColumnName())
                                        .addToColumns(longCol().primaryKey(true).build())
                                        .build())
                                .build()
                                .tableMap()
                },
                {   // 05: two tables--composite primary and foreign keys in first migration--add one column in next
                        Arrays.asList(createTableMigration("table1"), createTableMigration("table2")),
                        newTableContext()
                                .addTable(defaultPkTable("table1")
                                        .addToColumns(stringCol().columnName("table2_project").build())
                                        .addToColumns(longCol().columnName("table2_build").build())
                                        .addTableForeignKey(cascadeTFKI("table2")
                                                .localToForeignColumnMap(mapOf(
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
                                .tableMap(),
                        Arrays.asList(
                                addColumnMigration("table1")
                                        .columnName(longCol().build().getColumnName())
                                        .build()
                        ),
                        newTableContext()
                                .addTable(defaultPkTable("table1")
                                        .addToColumns(stringCol().columnName("table2_project").build())
                                        .addToColumns(longCol().columnName("table2_build").build())
                                        .addToColumns(longCol().build())
                                        .addTableForeignKey(cascadeTFKI("table2")
                                                .localToForeignColumnMap(mapOf(
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
                {   // 06: update an existing column's default value when it did not have one previously
                        Arrays.asList(
                                createTableMigration("table1"),
                                addColumnMigration("table1")
                                        .columnName(stringCol().build().getColumnName())
                                        .build()
                        ),
                        newTableContext()
                                .addTable(defaultPkTable("table1")
                                        .addToColumns(stringCol().build())
                                        .build())
                                .build()
                                .tableMap(),
                        Arrays.asList(changeDefaultValueMigration("table1")
                                .columnName(stringCol().build().getColumnName())
                                .build()),
                        newTableContext()
                                .addTable(table("table1")
                                        .addToColumns(stringCol().defaultValue("some default").build())
                                        .build())
                                .build()
                                .tableMap()
                },
                {   // 07: one tableBuilder with a column that has an index added after creation
                        Arrays.asList(
                                createTableMigration("table1"),
                                addColumnMigration("table1").columnName("table_1_index").build()
                        ),
                        newTableContext()
                                .addTable(table("table1")
                                        .addToColumns(longCol().columnName("table_1_index").build())
                                        .build())
                                .build()
                                .tableMap(),
                        Collections.singletonList(addIndexMigration("table1").columnName("table_1_index").build()),
                        newTableContext()
                                .addTable(table("table1")
                                        .addToColumns(longCol().columnName("table_1_index").index(true).build())
                                        .build())
                                .build()
                                .tableMap(),

                },
                {   // 08: one tableBuilder with a column that has a composite index added after creation
                        Arrays.asList(
                                createTableMigration("table1"),
                                addColumnMigration("table1").columnName("table_1_index_col1").build(),
                                addColumnMigration("table1").columnName("table_1_index_col2").build()
                        ),
                        newTableContext()
                                .addTable(table("table1")
                                        .addToColumns(longCol().columnName("table_1_index_col1").build())
                                        .addToColumns(longCol().columnName("table_1_index_col2").build())
                                        .build())
                                .build()
                                .tableMap(),
                        Collections.singletonList(addIndexMigration("table1")
                                .extras(mapOf("order", "table_1_index_col1,table_1_index_col2"))
                                .build()),
                        newTableContext()
                                .addTable(table("table1")
                                        .addToColumns(longCol().columnName("table_1_index_col1").index(true).build())
                                        .addToColumns(longCol().columnName("table_1_index_col2").index(true).build())
                                        .build())
                                .build()
                                .tableMap()
                }
        });
    }
}
