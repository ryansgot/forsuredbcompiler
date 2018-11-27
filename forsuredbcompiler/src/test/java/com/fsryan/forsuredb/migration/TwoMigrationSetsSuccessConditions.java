package com.fsryan.forsuredb.migration;

import com.fsryan.forsuredb.info.TableInfo;
import com.google.common.collect.ImmutableMap;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static com.fsryan.forsuredb.TestData.*;
import static com.fsryan.forsuredb.migration.MigrationFixtures.createTableMigration;

@RunWith(Parameterized.class)
public class TwoMigrationSetsSuccessConditions extends MigrationContextTest.TwoMigrationSetTest {

    public TwoMigrationSetsSuccessConditions(List<Migration> firstMigrations,
                                             Map<String, TableInfo> firstExpectedSchema,
                                             List<Migration> secondMigrations,
                                             Map<String, TableInfo> secondExpectedSchema) {
        super(firstMigrations, firstExpectedSchema, secondMigrations, secondExpectedSchema);
    }
//
//    @Parameterized.Parameters
//    public static Iterable<Object[]> data() {
//        return Arrays.asList(new Object[][]{
//                {   // 00: one table with no extra columns, add a column
//                        Arrays.asList(createTableMigration("table1")),
//                        newTableContext()
//                                .addTable(defaultPkTable("table1").build())
//                                .build()
//                                .tableMap(),
//                        Arrays.asList(addColumnMigration("table1").columnName(longCol().build().getColumnName()).build()),
//                        newTableContext()
//                                .addTable(defaultPkTable("table1")
//                                        .addColumn(longCol().build())
//                                        .build())
//                                .build()
//                                .tableMap()
//                },
//                {   // 01: one table with no extra columns, add two columns
//                        Arrays.asList(createTableMigration("table1")),
//                        newTableContext()
//                                .addTable(defaultPkTable("table1").build())
//                                .build()
//                                .tableMap(),
//                        Arrays.asList(
//                                addColumnMigration("table1").columnName(longCol().build().getColumnName()).build(),
//                                addColumnMigration("table1").columnName(stringCol().build().getColumnName()).build()
//                        ),
//                        newTableContext()
//                                .addTable(defaultPkTable("table1")
//                                        .addColumn(longCol().build())
//                                        .addColumn(stringCol().build())
//                                        .build())
//                                .build()
//                                .tableMap()
//                },
//                {   // 02: add new foreign key with the legacy ADD_FOREIGN_KEY_REFERENCE migration
//                        Arrays.asList(createTableMigration("table1"), createTableMigration("table2")),
//                        newTableContext()
//                                .addTable(defaultPkTable("table1").build())
//                                .addTable(defaultPkTable("table2").build())
//                                .build()
//                                .tableMap(),
//                        Arrays.asList(
//                                addForeignKeyReferenceMigration("table1")
//                                        .columnName(longCol().build().getColumnName())
//                                        .build()
//                        ),
//                        newTableContext()
//                                .addTable(defaultPkTable("table1")
//                                        .addForeignKey(dbmsDefaultTFKI("table2")
//                                                .localToForeignColumnMap(ImmutableMap.of(longCol().build().getColumnName(), "_id"))
//                                                .build())
//                                        .addColumn(longCol()
//                                                .foreignKeyInfo(cascadeFKI("table2")
//                                                        .apiClassName(TABLE_CLASS_NAME)
//                                                        .build())
//                                                .build())
//                                        .build())
//                                .addTable(defaultPkTable("table2").build())
//                                .build()
//                                .tableMap()
//                },
//                {   // 03: add new foreign key with the legacy UPDATE_FOREIGN_KEYS migration
//                        Arrays.asList(createTableMigration("table1"), createTableMigration("table2")),
//                        newTableContext()
//                                .addTable(defaultPkTable("table1").build())
//                                .addTable(defaultPkTable("table2").build())
//                                .build()
//                                .tableMap(),
//                        Arrays.asList(
//                                updateForeignKeysMigration("table1")
//                                        .addExtra("existing_column_names", "[\"_id\",\"created\",\"deleted\",\"modified\"]")
//                                        .addExtra("current_foreign_keys", "[]")
//                                        .build()
//                        ),
//                        newTableContext()
//                                .addTable(defaultPkTable("table1")
//                                        .addForeignKey(dbmsDefaultTFKI("table2")
//                                                .localToForeignColumnMap(ImmutableMap.of(longCol().build().getColumnName(), "_id"))
//                                                .build())
//                                        .addColumn(longCol()
//                                                .foreignKeyInfo(cascadeFKI("table2")
//                                                        .apiClassName(TABLE_CLASS_NAME)
//                                                        .build())
//                                                .build())
//                                        .build())
//                                .addTable(defaultPkTable("table2").build())
//                                .build()
//                                .tableMap()
//                },
//                {   // 04: update the primary key with a new column
//                        Arrays.asList(createTableMigration("table1")),
//                        newTableContext()
//                                .addTable(defaultPkTable("table1").build())
//                                .build()
//                                .tableMap(),
//                        Arrays.asList(updatePrimaryKeyMigration("table1").build()),
//                        newTableContext()
//                                .addTable(table("table1")
//                                        .resetPrimaryKey(Collections.singleton(longCol().build().getColumnName()))
//                                        .addColumn(longCol().primaryKey(true).build())
//                                        .build())
//                                .build()
//                                .tableMap()
//                },
//                {   // 05: two tables--composite primary and foreign keys in first migration--add one column in next
//                        Arrays.asList(createTableMigration("table1"), createTableMigration("table2")),
//                        tableMapOf(defaultPkTable("table1")
//                                .addColumn(stringCol().columnName("table2_project").build())
//                                .addColumn(longCol().columnName("table2_build").build())
//                                .addForeignKey(cascadeTFKI("table2")
//                                        .localToForeignColumnMap(ImmutableMap.of(
//                                                "table2_project", "project",
//                                                "table2_build", "build")
//                                        ).build())
//                                .build(),
//                                table("table2")
//                                        .addColumn(stringCol().columnName("project").build())
//                                        .addColumn(longCol().columnName("build").build())
//                                        .resetPrimaryKey(new HashSet<>(Arrays.asList("project", "build")))
//                                        .build()),
//                        Arrays.asList(
//                                addColumnMigration("table1")
//                                        .columnName(longCol().build().getColumnName())
//                                        .build()
//                        ),
//                        tableMapOf(defaultPkTable("table1")
//                                .addColumn(stringCol().columnName("table2_project").build())
//                                .addColumn(longCol().columnName("table2_build").build())
//                                .addColumn(longCol().build())
//                                .addForeignKey(cascadeTFKI("table2")
//                                        .localToForeignColumnMap(ImmutableMap.of(
//                                                "table2_project", "project",
//                                                "table2_build", "build")
//                                        ).build())
//                                .build(),
//                                table("table2")
//                                        .addColumn(stringCol().columnName("project").build())
//                                        .addColumn(longCol().columnName("build").build())
//                                        .resetPrimaryKey(new HashSet<>(Arrays.asList("project", "build")))
//                                        .build())
//                },
//                {   // 06: update an existing column's default value when it did not have one previously
//                        Arrays.asList(
//                                createTableMigration("table1"),
//                                addColumnMigration("table1")
//                                        .columnName(stringCol().build().getColumnName())
//                                        .build()
//                        ),
//                        newTableContext()
//                                .addTable(defaultPkTable("table1")
//                                        .addColumn(stringCol().build())
//                                        .build())
//                                .build()
//                                .tableMap(),
//                        Arrays.asList(changeDefaultValueMigration("table1")
//                                .columnName(stringCol().build().getColumnName())
//                                .build()),
//                        newTableContext()
//                                .addTable(table("table1")
//                                        .addColumn(stringCol().defaultValue("some default").build())
//                                        .build())
//                                .build()
//                                .tableMap()
//                },
//                {   // 07: one table with a column that has an index added after creation
//                        Arrays.asList(
//                                createTableMigration("table1"),
//                                addColumnMigration("table1").columnName("table_1_index").build()
//                        ),
//                        newTableContext()
//                                .addTable(table("table1")
//                                        .addColumn(longCol().columnName("table_1_index").build())
//                                        .build())
//                                .build()
//                                .tableMap(),
//                        Collections.singletonList(addIndexMigration("table1").columnName("table_1_index").build()),
//                        newTableContext()
//                                .addTable(table("table1")
//                                        .addColumn(longCol().columnName("table_1_index").index(true).build())
//                                        .build())
//                                .build()
//                                .tableMap(),
//
//                }
//        });
//    }
}
