package com.fsryan.forsuredb.sqlitelib;

import com.fsryan.forsuredb.gsonserialization.FSDbInfoGsonSerializer;
import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.Migration;
import com.fsryan.forsuredb.migration.MigrationSet;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.fsryan.forsuredb.info.TableInfoUtil.tableMapOf;
import static com.fsryan.forsuredb.migration.MigrationFixtures.migration;
import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertListEquals;

@Ignore("Ignored while switching to use SchemaDiff")
@RunWith(Parameterized.class)
public class CombinedMigrationTest {

    private static final List<Migration> createAndAddUniqueList = Arrays.asList(
            migration(Migration.Type.CREATE_TABLE)
                    .tableName("table")
                    .build(),
            migration(Migration.Type.ALTER_TABLE_ADD_UNIQUE)
                    .tableName("table")
                    .columnName("column")
                    .build()
    );

    private static final Map<String, TableInfo> testSchema = tableMapOf(
            TableInfo.builder()
                    .qualifiedClassName(SqlGeneratorTest.class.getName())
                    .tableName("table")
                    .addColumn(ColumnInfo.builder()
                            .methodName("column")
                            .columnName("column")
                            .unique(true)
                            .index(true)
                            .qualifiedType(String.class.getName())
                            .build())
                    .build()
    );

    private final MigrationSet inputMigrationSet;
    private final List<String> expectedMigrationScript;

    public CombinedMigrationTest(MigrationSet inputMigrationSet, List<String> expectedMigrationScript) {
        this.inputMigrationSet = inputMigrationSet;
        this.expectedMigrationScript = expectedMigrationScript;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {   // 00: Should not attempt to add unique column twice given an ordered migration list that contains both
                        MigrationSet.builder()
                                .dbVersion(1)
                                .targetSchema(testSchema)
                                .orderedMigrations(createAndAddUniqueList)
                                .build(),
                        Arrays.asList(
                                "CREATE TABLE table(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT(STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW')), deleted INTEGER DEFAULT '0', modified DATETIME DEFAULT(STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW')), column TEXT UNIQUE);",
                                "CREATE TRIGGER table_updated_trigger AFTER UPDATE ON table BEGIN UPDATE table SET modified=STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW') WHERE _id=NEW._id; END;",
                                "CREATE UNIQUE INDEX IF NOT EXISTS table_column ON table(column);"
                        )
                },
                {   // 01: Should attempt to add unique index when not creating the table
                        MigrationSet.builder()
                                .dbVersion(1)
                                .targetSchema(testSchema)
                                .orderedMigrations(Collections.singletonList(createAndAddUniqueList.get(1)))
                                .build(),
                        Arrays.asList(
                                "ALTER TABLE table ADD COLUMN column TEXT;",
                                "CREATE UNIQUE INDEX IF NOT EXISTS table_column ON table(column);"
                        )
                }
        });
    }

    @Test
    public void shouldGenerateExpectedSqlScript() {
        List<String> actualSqlScript = new SqlGenerator()
                .generateMigrationSql(inputMigrationSet, new FSDbInfoGsonSerializer());
        assertListEquals(expectedMigrationScript, actualSqlScript);
    }
}
