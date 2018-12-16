package com.fsryan.forsuredb.sqlitelib.diff;

import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static com.fsryan.forsuredb.info.ColumnInfoUtil.colNameByType;
import static com.fsryan.forsuredb.info.DBInfoFixtures.*;
import static com.fsryan.forsuredb.info.TableInfoUtil.tableFQClassName;
import static com.fsryan.forsuredb.info.TableInfoUtil.tableMapOf;
import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertListEquals;
import static com.fsryan.forsuredb.test.tools.CollectionUtil.setOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 *<p>Tests the sql generation for adding one or more columns to an existing
 * table
 */
public class AddColumnsGeneratorTest {

    public static Iterable<Arguments> shouldGenerateCorrectSqlInput() {
        return Arrays.asList(
                // Adding a single column
                // TODO: make this nullable
                arguments(
                        "Basic table with big decimal column",
                        Collections.singleton(bigDecimalCol().build()),
                        Collections.singletonList(
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(BigDecimal.class) + " TEXT;"
                        )
                ),
                // TODO: make this nullable
                arguments(
                        "Basic table with big integer column",
                        Collections.singleton(bigIntegerCol().build()),
                        Collections.singletonList(
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(BigInteger.class) + " TEXT;"
                        )
                ),
                arguments(
                        "Basic table with boolean column",
                        Collections.singleton(booleanCol().build()),
                        Collections.singletonList(
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(boolean.class) + " INTEGER;"
                        )
                ),
                // TODO: make this nullable if there is no default
                arguments(
                        "Basic table with boolean wrapper column",
                        Collections.singleton(booleanWrapperCol().build()),
                        Collections.singletonList(
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(Boolean.class) + " INTEGER;"
                        )
                ),
                // TODO: make this nullable if there is no default
                arguments(
                        "Basic table with byte array column",
                        Collections.singleton(byteArrayCol().build()),
                        Collections.singletonList(
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(byte[].class) + " BLOB;"
                        )
                ),
                // TODO: make this nullable if there is no default
                arguments(
                        "Basic table with date column",
                        Collections.singleton(dateCol().build()),
                        Collections.singletonList(
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(Date.class) + " DATETIME;"
                        )
                ),
                arguments(
                        "Basic table with double column",
                        Collections.singleton(doubleCol().build()),
                        Collections.singletonList(
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(double.class) + " REAL;"
                        )
                ),
                // TODO: make this nullable if there is no default
                arguments(
                        "Basic table with double wrapper column",
                        Collections.singleton(doubleWrapperCol().build()),
                        Collections.singletonList(
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(Double.class) + " REAL;"
                        )
                ),
                arguments(
                        "Basic table with float column",
                        Collections.singleton(floatCol().build()),
                        Collections.singletonList(
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(float.class) + " REAL;"
                        )
                ),
                // TODO: make this nullable if there is no default
                arguments(
                        "Basic table with float wrapper column",
                        Collections.singleton(floatWrapperCol().build()),
                        Collections.singletonList(
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(Float.class) + " REAL;"
                        )
                ),
                arguments(
                        "Basic table with int column",
                        Collections.singleton(intCol().build()),
                        Collections.singletonList(
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(int.class) + " INTEGER;"
                        )
                ),
                // TODO: make this nullable if there is no default
                arguments(
                        "Basic table with integer wrapper column",
                        Collections.singleton(integerWrapperCol().build()),
                        Collections.singletonList(
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(Integer.class) + " INTEGER;"
                        )
                ),
                arguments(
                        "Basic table with long column",
                        Collections.singleton(longCol().build()),
                        Collections.singletonList(
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(long.class) + " INTEGER;"
                        )
                ),
                // TODO: make this nullable if there is no default
                arguments(
                        "Basic table with long wrapper column",
                        Collections.singleton(longWrapperCol().build()),
                        Collections.singletonList(
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(Long.class) + " INTEGER;"
                        )
                ),
                // TODO: make this nullable if there is no default
                arguments(
                        "Basic table with String column",
                        Collections.singleton(stringCol().build()),
                        Collections.singletonList(
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(String.class) + " TEXT;"
                        )
                ),
                arguments(
                        "Basic table with String column that has a default value",
                        Collections.singleton(stringCol()
                                .defaultValue("Hello!")
                                .build()),
                        Collections.singletonList(
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(String.class) + " TEXT DEFAULT('Hello!');"
                        )
                ),
                arguments(
                        "Basic table with String column that has a default value containing a '",
                        Collections.singleton(stringCol()
                                .defaultValue("Hel'lo!")
                                .build()),
                        Collections.singletonList(
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(String.class) + " TEXT DEFAULT('Hel''lo!');"
                        )
                ),
                arguments(
                        "Basic table with unique String column",
                        Collections.singleton(stringCol()
                                .unique(true)
                                .build()),
                        Arrays.asList(
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(String.class) + " TEXT;",
                                "CREATE UNIQUE INDEX IF NOT EXISTS t1_unique_" + colNameByType(String.class) + " ON t1(" + colNameByType(String.class) + ");"
                        )
                ),
                arguments(
                        "Basic table with a column of each type",
                        setOf(
                                bigDecimalCol().build(),
                                bigIntegerCol().build(),
                                booleanCol().build(),
                                booleanWrapperCol().build(),
                                byteArrayCol().build(),
                                dateCol().build(),
                                doubleCol().build(),
                                doubleWrapperCol().build(),
                                floatCol().build(),
                                floatWrapperCol().build(),
                                intCol().build(),
                                integerWrapperCol().build(),
                                longCol().build(),
                                longWrapperCol().build(),
                                stringCol().build()
                        ),
                        Arrays.asList(
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(BigDecimal.class) + " TEXT;",
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(BigInteger.class) + " TEXT;",
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(boolean.class) + " INTEGER;",
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(Boolean.class) + " INTEGER;",
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(byte[].class) + " BLOB;",
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(Date.class) + " DATETIME;",
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(double.class) + " REAL;",
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(Double.class) + " REAL;",
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(float.class) + " REAL;",
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(Float.class) + " REAL;",
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(int.class) + " INTEGER;",
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(Integer.class) + " INTEGER;",
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(long.class) + " INTEGER;",
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(Long.class) + " INTEGER;",
                                "ALTER TABLE t1 ADD COLUMN " + colNameByType(String.class) + " TEXT;"
                        )
                )
        );
    }

    @Test
    public void shouldThrowWhenSchemaDoesNotContainTableInfoForTableClassName() {
        assertThrows(IllegalArgumentException.class, () -> new AddColumnsGenerator("", Collections.emptyMap(), Collections.emptySet()));
    }

    @Test
    public void shouldThrowWhenNotAllInputColumnNamesAreInTheTable() {
        final Map<String, TableInfo> schema = tableMapOf(tableBuilder("t1").build());
        assertThrows(IllegalArgumentException.class, () -> new AddColumnsGenerator(tableFQClassName("t1"), schema, Collections.singleton("not_here")));
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("shouldGenerateCorrectSqlInput")
    @DisplayName("should generate correct ALTER TABLE ADD COLUMN sql given a basic forsuredb table and a list of columns to add")
    public void shouldCorrectlyGenerateSql(String desc, Set<ColumnInfo> columnsToAdd, List<String> expectedStatements) {
        final String tableClassName = tableFQClassName("t1");
        final Map<String, TableInfo> schema = tableMapOf(
                tableBuilder("t1")
                        .addAllColumns(columnsToAdd)
                        .build()
        );
        Set<String> columnNames = columnsToAdd.stream().map(ColumnInfo::getColumnName).collect(Collectors.toSet());
        List<String> statements = new AddColumnsGenerator(tableClassName, schema, columnNames).statements();
        assertListEquals(desc, expectedStatements, statements);
    }
}
