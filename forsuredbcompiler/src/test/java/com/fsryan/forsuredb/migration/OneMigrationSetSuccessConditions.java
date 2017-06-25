package com.fsryan.forsuredb.migration;

import com.fsryan.forsuredb.api.info.TableInfo;
import com.fsryan.forsuredb.api.migration.Migration;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
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
                }
        });
    }
}
