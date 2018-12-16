package com.fsryan.forsuredb.sqlitelib.diff;

import com.fsryan.forsuredb.info.TableInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static com.fsryan.forsuredb.info.ColumnInfoUtil.colNameByType;
import static com.fsryan.forsuredb.info.DBInfoFixtures.*;
import static com.fsryan.forsuredb.info.TableInfoUtil.tableFQClassName;
import static com.fsryan.forsuredb.info.TableInfoUtil.tableMapOf;
import static com.fsryan.forsuredb.sqlitelib.SqlGenerator.CURRENT_UTC_TIME;
import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertListEquals;
import static com.fsryan.forsuredb.test.tools.CollectionUtil.setOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class CreateTableGeneratorTest {

    public static Iterable<Arguments> shouldGenerateCorrectSqlInput() {
        return Arrays.asList(
                arguments(
                        "Basic table with only the default columns",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .build()
                        ),
                        Arrays.asList(
                                "DROP TABLE IF EXISTS t1;",
                                "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT(" + CURRENT_UTC_TIME + "), deleted INTEGER DEFAULT('0'), modified DATETIME DEFAULT(" + CURRENT_UTC_TIME + "));",
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;"
                        )
                ),
                // Adding a single column
                arguments(
                        "Basic table with big decimal column",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(bigDecimalCol().build())
                                        .build()
                        ),
                        Arrays.asList(
                                "DROP TABLE IF EXISTS t1;",
                                "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, " + colNameByType(BigDecimal.class) + " TEXT, created DATETIME DEFAULT(" + CURRENT_UTC_TIME + "), deleted INTEGER DEFAULT('0'), modified DATETIME DEFAULT(" + CURRENT_UTC_TIME + "));",
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;"
                        )
                ),
                arguments(
                        "Basic table with big integer column",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(bigIntegerCol().build())
                                        .build()
                        ),
                        Arrays.asList(
                                "DROP TABLE IF EXISTS t1;",
                                "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, " + colNameByType(BigInteger.class) + " TEXT, created DATETIME DEFAULT(" + CURRENT_UTC_TIME + "), deleted INTEGER DEFAULT('0'), modified DATETIME DEFAULT(" + CURRENT_UTC_TIME + "));",
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;"
                        )
                ),
                arguments(
                        "Basic table with boolean column",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(booleanCol().build())
                                        .build()
                        ),
                        Arrays.asList(
                                "DROP TABLE IF EXISTS t1;",
                                "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, " + colNameByType(boolean.class) + " INTEGER, created DATETIME DEFAULT(" + CURRENT_UTC_TIME + "), deleted INTEGER DEFAULT('0'), modified DATETIME DEFAULT(" + CURRENT_UTC_TIME + "));",
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;"
                        )
                ),
                arguments(
                        "Basic table with boolean wrapper column",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(booleanWrapperCol().build())
                                        .build()
                        ),
                        Arrays.asList(
                                "DROP TABLE IF EXISTS t1;",
                                "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, " + colNameByType(Boolean.class) + " INTEGER, created DATETIME DEFAULT(" + CURRENT_UTC_TIME + "), deleted INTEGER DEFAULT('0'), modified DATETIME DEFAULT(" + CURRENT_UTC_TIME + "));",
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;"
                        )
                ),
                arguments(
                        "Basic table with byte array column",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(byteArrayCol().build())
                                        .build()
                        ),
                        Arrays.asList(
                                "DROP TABLE IF EXISTS t1;",
                                "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, " + colNameByType(byte[].class) + " BLOB, created DATETIME DEFAULT(" + CURRENT_UTC_TIME + "), deleted INTEGER DEFAULT('0'), modified DATETIME DEFAULT(" + CURRENT_UTC_TIME + "));",
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;"
                        )
                ),
                arguments(
                        "Basic table with date column",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(dateCol().build())
                                        .build()
                        ),
                        Arrays.asList(
                                "DROP TABLE IF EXISTS t1;",
                                "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT(" + CURRENT_UTC_TIME + "), " + colNameByType(Date.class) + " DATETIME, deleted INTEGER DEFAULT('0'), modified DATETIME DEFAULT(" + CURRENT_UTC_TIME + "));",
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;"
                        )
                ),
                arguments(
                        "Basic table with double column",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(doubleCol().build())
                                        .build()
                        ),
                        Arrays.asList(
                                "DROP TABLE IF EXISTS t1;",
                                "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT(" + CURRENT_UTC_TIME + "), deleted INTEGER DEFAULT('0'), " + colNameByType(double.class) + " REAL, modified DATETIME DEFAULT(" + CURRENT_UTC_TIME + "));",
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;"
                        )
                ),
                arguments(
                        "Basic table with double wrapper column",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(doubleWrapperCol().build())
                                        .build()
                        ),
                        Arrays.asList(
                                "DROP TABLE IF EXISTS t1;",
                                "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT(" + CURRENT_UTC_TIME + "), deleted INTEGER DEFAULT('0'), " + colNameByType(Double.class) + " REAL, modified DATETIME DEFAULT(" + CURRENT_UTC_TIME + "));",
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;"
                        )
                ),
                arguments(
                        "Basic table with float column",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(floatCol().build())
                                        .build()
                        ),
                        Arrays.asList(
                                "DROP TABLE IF EXISTS t1;",
                                "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT(" + CURRENT_UTC_TIME + "), deleted INTEGER DEFAULT('0'), " + colNameByType(float.class) + " REAL, modified DATETIME DEFAULT(" + CURRENT_UTC_TIME + "));",
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;"
                        )
                ),
                arguments(
                        "Basic table with float wrapper column",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(floatWrapperCol().build())
                                        .build()
                        ),
                        Arrays.asList(
                                "DROP TABLE IF EXISTS t1;",
                                "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT(" + CURRENT_UTC_TIME + "), deleted INTEGER DEFAULT('0'), " + colNameByType(Float.class) + " REAL, modified DATETIME DEFAULT(" + CURRENT_UTC_TIME + "));",
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;"
                        )
                ),
                arguments(
                        "Basic table with int column",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(intCol().build())
                                        .build()
                        ),
                        Arrays.asList(
                                "DROP TABLE IF EXISTS t1;",
                                "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT(" + CURRENT_UTC_TIME + "), deleted INTEGER DEFAULT('0'), " + colNameByType(int.class) + " INTEGER, modified DATETIME DEFAULT(" + CURRENT_UTC_TIME + "));",
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;"
                        )
                ),
                arguments(
                        "Basic table with integer wrapper column",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(integerWrapperCol().build())
                                        .build()
                        ),
                        Arrays.asList(
                                "DROP TABLE IF EXISTS t1;",
                                "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT(" + CURRENT_UTC_TIME + "), deleted INTEGER DEFAULT('0'), " + colNameByType(Integer.class) + " INTEGER, modified DATETIME DEFAULT(" + CURRENT_UTC_TIME + "));",
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;"
                        )
                ),
                arguments(
                        "Basic table with long column",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .build()
                        ),
                        Arrays.asList(
                                "DROP TABLE IF EXISTS t1;",
                                "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT(" + CURRENT_UTC_TIME + "), deleted INTEGER DEFAULT('0'), " + colNameByType(long.class) + " INTEGER, modified DATETIME DEFAULT(" + CURRENT_UTC_TIME + "));",
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;"
                        )
                ),
                arguments(
                        "Basic table with long wrapper column",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longWrapperCol().build())
                                        .build()
                        ),
                        Arrays.asList(
                                "DROP TABLE IF EXISTS t1;",
                                "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT(" + CURRENT_UTC_TIME + "), deleted INTEGER DEFAULT('0'), " + colNameByType(Long.class) + " INTEGER, modified DATETIME DEFAULT(" + CURRENT_UTC_TIME + "));",
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;"
                        )
                ),
                arguments(
                        "Basic table with String column",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(stringCol().build())
                                        .build()
                        ),
                        Arrays.asList(
                                "DROP TABLE IF EXISTS t1;",
                                "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT(" + CURRENT_UTC_TIME + "), deleted INTEGER DEFAULT('0'), modified DATETIME DEFAULT(" + CURRENT_UTC_TIME + "), " + colNameByType(String.class) + " TEXT);",
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;"
                        )
                ),
                // adding one of each column
                arguments(
                        "Basic table with one column of each type",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(bigDecimalCol().build())
                                        .addColumn(bigIntegerCol().build())
                                        .addColumn(booleanCol().build())
                                        .addColumn(booleanWrapperCol().build())
                                        .addColumn(byteArrayCol().build())
                                        .addColumn(dateCol().build())
                                        .addColumn(doubleCol().build())
                                        .addColumn(doubleWrapperCol().build())
                                        .addColumn(floatCol().build())
                                        .addColumn(floatWrapperCol().build())
                                        .addColumn(intCol().build())
                                        .addColumn(integerWrapperCol().build())
                                        .addColumn(longCol().build())
                                        .addColumn(longWrapperCol().build())
                                        .addColumn(stringCol().build())
                                        .build()
                        ),
                        Arrays.asList(
                                "DROP TABLE IF EXISTS t1;",
                                String.format(
                                        "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, %s TEXT, %s TEXT, %s INTEGER, %s INTEGER, %s BLOB, created DATETIME DEFAULT(%s), %s DATETIME, deleted INTEGER DEFAULT('0'), %s REAL, %s REAL, %s REAL, %s REAL, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, modified DATETIME DEFAULT(%s), %s TEXT);",
                                        colNameByType(BigDecimal.class),
                                        colNameByType(BigInteger.class),
                                        colNameByType(boolean.class),
                                        colNameByType(Boolean.class),
                                        colNameByType(byte[].class),
                                        CURRENT_UTC_TIME,
                                        colNameByType(Date.class),
                                        colNameByType(double.class),
                                        colNameByType(Double.class),
                                        colNameByType(float.class),
                                        colNameByType(Float.class),
                                        colNameByType(int.class),
                                        colNameByType(Integer.class),
                                        colNameByType(long.class),
                                        colNameByType(Long.class),
                                        CURRENT_UTC_TIME,
                                        colNameByType(String.class)
                                ),
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;"
                        )
                ),
                arguments(
                        "Basic table with String column that has a default value",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(stringCol().defaultValue("Something").build())
                                        .build()
                        ),
                        Arrays.asList(
                                "DROP TABLE IF EXISTS t1;",
                                "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT(" + CURRENT_UTC_TIME + "), deleted INTEGER DEFAULT('0'), modified DATETIME DEFAULT(" + CURRENT_UTC_TIME + "), " + colNameByType(String.class) + " TEXT DEFAULT('Something'));",
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;"
                        )
                ),
                arguments(
                        "Basic table with String column that has a default value that contains a ' character",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(stringCol().defaultValue("Something's up").build())
                                        .build()
                        ),
                        Arrays.asList(
                                "DROP TABLE IF EXISTS t1;",
                                String.format(
                                        "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT(%s), deleted INTEGER DEFAULT('0'), modified DATETIME DEFAULT(%s), %s TEXT DEFAULT('%s'));",
                                        CURRENT_UTC_TIME,
                                        CURRENT_UTC_TIME,
                                        colNameByType(String.class),
                                        "Something''s up"
                                ),
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;"
                        )
                ),
                arguments(
                        "Basic table with a unique String column",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(stringCol().unique(true).build())
                                        .build()
                        ),
                        Arrays.asList(
                                "DROP TABLE IF EXISTS t1;",
                                String.format(
                                        "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT(%s), deleted INTEGER DEFAULT('0'), modified DATETIME DEFAULT(%s), %s TEXT UNIQUE);",
                                        CURRENT_UTC_TIME,
                                        CURRENT_UTC_TIME,
                                        colNameByType(String.class)
                                ),
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;"
                        )
                ),
                arguments(
                        "Basic table with composite primary key",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(stringCol().build())
                                        .addColumn(intCol().build())
                                        .resetPrimaryKey(setOf(colNameByType(String.class), colNameByType(int.class)))
                                        .build()
                        ),
                        Arrays.asList(
                                "DROP TABLE IF EXISTS t1;",
                                String.format(
                                        "CREATE TABLE IF NOT EXISTS t1(_id INTEGER, created DATETIME DEFAULT(%s), deleted INTEGER DEFAULT('0'), %s INTEGER, modified DATETIME DEFAULT(%s), %s TEXT, PRIMARY KEY(%s, %s));",
                                        CURRENT_UTC_TIME,
                                        colNameByType(int.class),
                                        CURRENT_UTC_TIME,
                                        colNameByType(String.class),
                                        colNameByType(int.class),
                                        colNameByType(String.class)
                                ),
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;"
                        )
                ),
                arguments(
                        "Basic table with single column foreignKey that has no ON UPDATE and no ON DELETE actions",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .addForeignKey(
                                                foreignKeyTo("t2")
                                                        .mapLocalToForeignColumn(colNameByType(long.class), "_id")
                                                        .build()
                                        ).build(),
                                tableBuilder("t2")
                                        .build()
                        ),
                        Arrays.asList(
                                "PRAGMA foreign_keys = false;",
                                "DROP TABLE IF EXISTS t1;",
                                String.format(
                                        "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT(%s), deleted INTEGER DEFAULT('0'), %s INTEGER, modified DATETIME DEFAULT(%s), FOREIGN KEY(%s) REFERENCES t2(%s));",
                                        CURRENT_UTC_TIME,
                                        colNameByType(long.class),
                                        CURRENT_UTC_TIME,
                                        colNameByType(long.class),
                                        "_id"
                                ),
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;",
                                "PRAGMA foreign_keys = true;"
                        )
                ),
                arguments(
                        "Basic table with single column foreignKey that has ON UPDATE CASCADE and no ON DELETE actions",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .addForeignKey(
                                                foreignKeyTo("t2")
                                                        .mapLocalToForeignColumn(colNameByType(long.class), "_id")
                                                        .updateChangeAction("CASCADE")
                                                        .build()
                                        ).build(),
                                tableBuilder("t2")
                                        .build()
                        ),
                        Arrays.asList(
                                "PRAGMA foreign_keys = false;",
                                "DROP TABLE IF EXISTS t1;",
                                String.format(
                                        "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT(%s), deleted INTEGER DEFAULT('0'), %s INTEGER, modified DATETIME DEFAULT(%s), FOREIGN KEY(%s) REFERENCES t2(%s) ON UPDATE CASCADE);",
                                        CURRENT_UTC_TIME,
                                        colNameByType(long.class),
                                        CURRENT_UTC_TIME,
                                        colNameByType(long.class),
                                        "_id"
                                ),
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;",
                                "PRAGMA foreign_keys = true;"
                        )
                ),
                arguments(
                        "Basic table with single column foreignKey that has no UPDATE action and ON DELETE SET NULL",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .addForeignKey(
                                                foreignKeyTo("t2")
                                                        .mapLocalToForeignColumn(colNameByType(long.class), "_id")
                                                        .deleteChangeAction("SET NULL")
                                                        .build()
                                        ).build(),
                                tableBuilder("t2")
                                        .build()
                        ),
                        Arrays.asList(
                                "PRAGMA foreign_keys = false;",
                                "DROP TABLE IF EXISTS t1;",
                                String.format(
                                        "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT(%s), deleted INTEGER DEFAULT('0'), %s INTEGER, modified DATETIME DEFAULT(%s), FOREIGN KEY(%s) REFERENCES t2(%s) ON DELETE SET NULL);",
                                        CURRENT_UTC_TIME,
                                        colNameByType(long.class),
                                        CURRENT_UTC_TIME,
                                        colNameByType(long.class),
                                        "_id"
                                ),
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;",
                                "PRAGMA foreign_keys = true;"
                        )
                ),
                arguments(
                        "Basic table with single column foreignKey that has ON UPDATE CASCADE and ON DELETE SET DEFAULT",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .addForeignKey(
                                                foreignKeyTo("t2")
                                                        .mapLocalToForeignColumn(colNameByType(long.class), "_id")
                                                        .updateChangeAction("CASCADE")
                                                        .deleteChangeAction("SET DEFAULT")
                                                        .build()
                                        ).build(),
                                tableBuilder("t2")
                                        .build()
                        ),
                        Arrays.asList(
                                "PRAGMA foreign_keys = false;",
                                "DROP TABLE IF EXISTS t1;",
                                String.format(
                                        "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT(%s), deleted INTEGER DEFAULT('0'), %s INTEGER, modified DATETIME DEFAULT(%s), FOREIGN KEY(%s) REFERENCES t2(%s) ON DELETE SET DEFAULT ON UPDATE CASCADE);",
                                        CURRENT_UTC_TIME,
                                        colNameByType(long.class),
                                        CURRENT_UTC_TIME,
                                        colNameByType(long.class),
                                        "_id"
                                ),
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;",
                                "PRAGMA foreign_keys = true;"
                        )
                ),
                arguments(
                        "Basic table with composite foreignKey that has ON UPDATE CASCADE and ON DELETE CASCADE",
                        tableFQClassName("t1"),
                        tableMapOf(
                                tableBuilder("t1")
                                        .addColumn(longCol().build())
                                        .addColumn(stringCol().build())
                                        .addForeignKey(
                                                cascadeForeignKeyTo("t2")
                                                        .mapLocalToForeignColumn(colNameByType(long.class), colNameByType(long.class))
                                                        .mapLocalToForeignColumn(colNameByType(String.class), colNameByType(String.class))
                                                        .build()
                                        ).build(),
                                tableBuilder("t2")
                                        .addColumn(longCol().build())
                                        .addColumn(stringCol().build())
                                        .resetPrimaryKey(setOf(colNameByType(long.class), colNameByType(String.class)))
                                        .build()
                        ),
                        Arrays.asList(
                                "PRAGMA foreign_keys = false;",
                                "DROP TABLE IF EXISTS t1;",
                                String.format(
                                        "CREATE TABLE IF NOT EXISTS t1(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT(%s), deleted INTEGER DEFAULT('0'), %s INTEGER, modified DATETIME DEFAULT(%s), %s TEXT, FOREIGN KEY(%s, %s) REFERENCES t2(%s, %s) ON DELETE CASCADE ON UPDATE CASCADE);",
                                        CURRENT_UTC_TIME,
                                        colNameByType(long.class),
                                        CURRENT_UTC_TIME,
                                        colNameByType(String.class),
                                        colNameByType(long.class),
                                        colNameByType(String.class),
                                        colNameByType(long.class),
                                        colNameByType(String.class)
                                ),
                                "CREATE TRIGGER IF NOT EXISTS t1_modified_trigger AFTER UPDATE ON t1 BEGIN UPDATE t1 SET modified=" + CURRENT_UTC_TIME + " WHERE _id=NEW._id; END;",
                                "PRAGMA foreign_keys = true;"
                        )
                )
                // TODO: tests for table indices
        );
    }

    @Test
    public void shouldThrowWhenSchemaDoesNotContainTableInfoForTableClassName() {
        assertThrows(IllegalArgumentException.class, () -> new CreateTableGenerator("", Collections.emptyMap()));
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("shouldGenerateCorrectSqlInput")
    @DisplayName("should generate correct TABLE CREATE sql given tableclassName and schema")
    public void shouldCorrectlyGenerateSql(String desc, String tableClassName, Map<String, TableInfo> schema, List<String> expectedStatements) {
        List<String> statements = new CreateTableGenerator(tableClassName, schema).statements();
        assertListEquals(desc, expectedStatements, statements);
    }
}
