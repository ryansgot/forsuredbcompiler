package com.fsryan.forsuredb.sqlitelib.diff;

import com.fsryan.forsuredb.info.ColumnInfoUtil;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.SchemaDiff;
import com.fsryan.forsuredb.sqlitelib.SqlGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;

import static com.fsryan.forsuredb.info.ColumnInfoUtil.colNameByType;
import static com.fsryan.forsuredb.info.DBInfoFixtures.stringCol;
import static com.fsryan.forsuredb.info.DBInfoFixtures.tableBuilder;
import static com.fsryan.forsuredb.info.TableInfoUtil.tableFQClassName;
import static com.fsryan.forsuredb.info.TableInfoUtil.tableMapOf;
import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertListEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * <p>The SQLite documentation says to follow this guide, and you can do any
 * kind of migration that is not supported out-of-the box:
 * <ol>
 *   <li>
 *     If foreign key constraints are enabled, disable them using
 *     {@code PRAGMA foreign_keys=OFF}. This will be necessary because
 *     forsuredb wants foreign key constraints to be on always.
 *   </li>
 *   <li>
 *     Start a transaction.
 *   </li>
 *   <li>
 *     Remember the format of all indexes and triggers associated with table X.
 *     This information will be needed in step 8 below. One way to do this is
 *     to run a query like the following:
 *     {@code SELECT type, sql FROM sqlite_master WHERE tbl_name='X'}. This
 *     probably will not be necessary because the diffs will tell us what needs
 *     to be changed at runtime.
 *   </li>
 *   <li>
 *     Transfer content from X into new_X using a statement like:
 *     {@code INSERT INTO new_X SELECT ... FROM X}.
 *   </li>
 *   <li>
 *     Drop the old table X: DROP TABLE X.
 *   </li>
 *   <li>
 *     Change the name of new_X to X using: ALTER TABLE new_X RENAME TO X.
 *   </li>
 *   <li>
 *     Use CREATE INDEX and CREATE TRIGGER to reconstruct indexes and triggers
 *     associated with table X. Perhaps use the old format of the triggers and
 *     indexes saved from step 3 above as a guide, making changes as
 *     appropriate for the alteration. Do this if you have to, but you may not
 *     have to do it.
 *   </li>
 *   <li>
 *     If any views refer to table X in a way that is affected by the schema
 *     change, then drop those views using DROP VIEW and recreate them with
 *     whatever changes are necessary to accommodate the schema change using
 *     CREATE VIEW. Views are not supported by forsuredb at the moment.
 *   </li>
 *   <li>
 *     If foreign key constraints were originally enabled then run
 *     {@code PRAGMA foreign_key_check} to verify that the schema change did
 *     not break any foreign key constraints. The framework does not allow for
 *     reacting to this regardless of the outcome of this query. Therefore, you
 *     should test all the scenarios you can think of.
 *   </li>
 *   <li>
 *     Commit the transaction started in step 2.
 *   </li>
 *   <li>
 *     If foreign keys constraints were originally enabled, reenable them now.
 *     forsuredb will always do this.
 *   </li>
 * </ol>
 */
public class RecreateTableGeneratorTest {

    public static Iterable<Arguments> recreateTableInput() {
        return Arrays.asList(
                // TODO: index handling when table is recreated
                arguments(
                        "Change default value of column; no extra indices",
                        Arrays.asList(
                                String.format(
                                        "CREATE TABLE t1(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT(%s) , deleted INTEGER DEFAULT('0'), int_col INTEGER, modified DATETIME DEFAULT(%s), %s TEXT DEFAULT('prev'))",
                                        SqlGenerator.CURRENT_UTC_TIME,
                                        SqlGenerator.CURRENT_UTC_TIME,
                                        colNameByType(String.class)
                                ),
                                String.format(
                                        "CREATE TRIGGER t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t SET modified=%s WHERE _id=NEW._id; END",
                                        SqlGenerator.CURRENT_UTC_TIME
                                )
                        ),
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(stringCol().defaultValue("current").build())
                                        .build()
                        ),
                        SchemaDiff.builder()
                                .tableName("t1")
                                .type(SchemaDiff.TYPE_CHANGED)
                                .enrichSubType(SchemaDiff.TYPE_DEFAULT)
                                .addAttribute(SchemaDiff.ATTR_CURR_NAME, "t1")
                                .addAttribute(SchemaDiff.ATTR_DEFAULTS, String.format("%s=%s", colNameByType(String.class), "current"))
                                .build(),
                        Arrays.asList(
                                "PRAGMA foreign_keys = OFF;",
                                "BEGIN TRANSACTION;",
                                "DROP TABLE IF EXISTS forsuredb_new_t1;",
                                String.format(
                                        "CREATE TABLE IF NOT EXISTS forsuredb_new_t1(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT(%s), deleted INTEGER DEFAULT(0), modified DATETIME DEFAULT(%s), string_col TEXT DEFAULT('current'));",
                                        SqlGenerator.CURRENT_UTC_TIME,
                                        SqlGenerator.CURRENT_UTC_TIME
                                ),
                                "INSERT INTO forsuredb_new_t1 SELECT _id, created, deleted, modified, string_col FROM t1;",
                                "DROP TABLE t1;",
                                "ALTER TABLE forsuredb_new_t1 RENAME TO t1;",
                                String.format(
                                        "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=%s WHERE _id=NEW._id; END;",
                                        SqlGenerator.CURRENT_UTC_TIME
                                ),
                                // forsuredb does not support views at this time.
                                // No foreign keys to handle in this test, so we're fine.
                                "END TRANSACTION;",
                                "PRAGMA foreign_keys = ON;"
                        )
                )
        );
    }

    @Test
    public void shouldThrowWhenSchemaDoesNotContainTableInfoForTableClassName() {
        assertThrows(IllegalArgumentException.class, () -> new AddColumnsGenerator("", Collections.emptyMap(), Collections.emptySet()));
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("recreateTableInput")
    @DisplayName("should generate correct table recreation SQL")
    public void sqlGeneration(String desc, List<String> _ignore, String tableClassName, Map<String, TableInfo> schema, SchemaDiff diff, List<String> expectedStatements) {
        List<String> statements = new RecreateTableGenerator(tableClassName, schema, diff).statements();
        assertListEquals(desc, expectedStatements, statements);
    }
}
