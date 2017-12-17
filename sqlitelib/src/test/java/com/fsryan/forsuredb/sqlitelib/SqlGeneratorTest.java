package com.fsryan.forsuredb.sqlitelib;

import com.fsryan.forsuredb.api.*;
import com.fsryan.forsuredb.api.sqlgeneration.SqlForPreparedStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static com.fsryan.forsuredb.sqlitelib.CollectionUtil.stringMapOf;
import static com.fsryan.forsuredb.sqlitelib.QueryBuilder.EMPTY_SQL;
import static com.fsryan.forsuredb.sqlitelib.TestData.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class SqlGeneratorTest {

    protected SqlGenerator generatorUnderTest;

    @Before
    public void setUpSqlGenerator() {
        generatorUnderTest = new SqlGenerator();
    }

    @RunWith(Parameterized.class)
    public static class InsertionQueryGeneration extends SqlGeneratorTest{

        private final String tableName;
        private final List<String> inputColumns;
        private final String expectedOutputSql;

        public InsertionQueryGeneration(String tableName, List<String> inputColumns, String expectedOutputSql) {
            this.tableName = tableName;
            this.inputColumns = inputColumns;
            this.expectedOutputSql = expectedOutputSql;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {   // 00: empty input map
                            TABLE_NAME,
                            Arrays.asList(),
                            EMPTY_SQL
                    },
                    {   // 01: empty input table name
                            "",
                            Arrays.asList("col1"),
                            EMPTY_SQL
                    },
                    {   // 02: null input map
                            "",
                            null,
                            EMPTY_SQL
                    },
                    {   // 03: null input table name
                            null,
                            Arrays.asList("col1"),
                            EMPTY_SQL
                    },
                    {   // 04: null input table name
                            null,
                            Arrays.asList("col1"),
                            EMPTY_SQL
                    },
                    {   // 05: valid args, one column and one value
                            TABLE_NAME,
                            Arrays.asList("col1"),
                            "INSERT INTO test_table (col1) VALUES (?);"
                    },
                    {   // 06: valid args, two columns and two values
                            TABLE_NAME,
                            Arrays.asList("col2", "col1"),
                            "INSERT INTO test_table (col2, col1) VALUES (?, ?);"
                    },  // TODO: determine whether you should still filter at this level
//                    {   // 07: valid args, attempt to insert an _id
//                            TABLE_NAME,
//                            Arrays.asList("_id", "col2", "col1"),
//                            "INSERT INTO test_table (col2, col1) VALUES (?, ?);"
//                    },
//                    {   // 08: valid args, attempt to insert modified
//                            TABLE_NAME,
//                            Arrays.asList("modified", "col2", "col1"),
//                            "INSERT INTO test_table (col2, col1) VALUES (?, ?);"
//                    },
//                    {   // 09: valid args, attempt to insert created
//                            TABLE_NAME,
//                            Arrays.asList("created", "col2", "col1"),
//                            "INSERT INTO test_table (col2, col1) VALUES (?, ?);"
//                    },
//                    {   // 10: valid args, attempt to insert _id, created, modified
//                            TABLE_NAME,
//                            Arrays.asList("_id", "created", "modified", "col1", "col2"),
//                            "INSERT INTO test_table (col1, col2) VALUES (?, ?);"
//                    }
            });
        }

        @Test
        public void shouldOutputCorrectSql() {
            final String actual = generatorUnderTest.newSingleRowInsertionSql(tableName, inputColumns);
            assertEquals(expectedOutputSql, actual);
        }

        @Test
        public void outputShouldBeAtLeastLength1() {
            assertTrue(0 < generatorUnderTest.newSingleRowInsertionSql(tableName, inputColumns).length());
        }

        @Test
        public void outputShouldEndInSemicolon() {
            assertTrue(generatorUnderTest.newSingleRowInsertionSql(tableName, inputColumns).endsWith(";"));
        }
    }

    @RunWith(Parameterized.class)
    public static class ExpressOrdering extends SqlGeneratorTest {

        private final List<FSOrdering> input;
        private final String expectedOutput;

        public ExpressOrdering(List<FSOrdering> input, String expectedSql) {
            this.input = input;
            this.expectedOutput = expectedSql;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {   // 00: number greater than OrderBy.ORDER_ASC treated as ascending
                            Arrays.asList(
                                    new FSOrdering("table", "column1", OrderBy.ORDER_ASC + 1)
                            ),
                            " ORDER BY table.column1 ASC"
                    },
                    {   // 01: number lower than OrderBy.ORDER_DESC treated as descending
                            Arrays.asList(
                                    new FSOrdering("table", "column1", OrderBy.ORDER_DESC - 1)
                            ),
                            " ORDER BY table.column1 DESC"
                    },
                    {   // 02: OrderBy.ORDER_ASC treated as ASC
                            Arrays.asList(
                                    new FSOrdering("table", "column1", OrderBy.ORDER_ASC)
                            ),
                            " ORDER BY table.column1 ASC"
                    },
                    {   // 03: OrderBy.ORDER_DESC treated as DESC
                            Arrays.asList(
                                    new FSOrdering("table", "column1", OrderBy.ORDER_DESC)
                            ),
                            " ORDER BY table.column1 DESC"
                    },
                    {   // 04: Two different ascending on same table
                            Arrays.asList(
                                    new FSOrdering("table", "column1", OrderBy.ORDER_ASC),
                                    new FSOrdering("table", "column2", OrderBy.ORDER_ASC)
                            ),
                            " ORDER BY table.column1 ASC, table.column2 ASC"
                    },
                    {   // 05: Two different descending on same table
                            Arrays.asList(
                                    new FSOrdering("table", "column1", OrderBy.ORDER_DESC),
                                    new FSOrdering("table", "column2", OrderBy.ORDER_DESC)
                            ),
                            " ORDER BY table.column1 DESC, table.column2 DESC"
                    },
                    {   // 06: ASC, then DESC for different columns
                            Arrays.asList(
                                    new FSOrdering("table", "column1", OrderBy.ORDER_ASC),
                                    new FSOrdering("table", "column2", OrderBy.ORDER_DESC)
                            ),
                            " ORDER BY table.column1 ASC, table.column2 DESC"
                    },
                    {   // 07: ASC, then DESC for different columns of different tables same column name
                            Arrays.asList(
                                    new FSOrdering("table", "column1", OrderBy.ORDER_ASC),
                                    new FSOrdering("table2", "column1", OrderBy.ORDER_DESC)
                            ),
                            " ORDER BY table.column1 ASC, table2.column1 DESC"
                    },
                    {   // 08: Empty list returns empty string
                            new ArrayList<FSOrdering>(0),
                            ""
                    },
                    {   // 09: null returns empty string
                            null,
                            ""
                    }
            });
        }

        @Test
        public void should() {
            assertEquals(expectedOutput, generatorUnderTest.expressOrdering(input));
        }
    }

    /*
            final String[] p = projectionHelper.formatProjection(projection);
        final String orderBy = expressOrdering(orderings);
        final QueryCorrector qc = new QueryCorrector(table, null, selection, orderBy);
        final String limit = qc.getLimit() > 0 ? "LIMIT " + qc.getLimit() : null;
        final boolean distinct = projection != null && projection.isDistinct();
        return new SqlForPreparedStatement(
                buildQuery(distinct, table, p, qc.getSelection(true), null, null, orderBy, limit),
                qc.getSelectionArgs()
        );
     */
    @RunWith(Parameterized.class)
    public static class CreateSingleTableQuerySql extends SqlGeneratorTest {

        private final String table;
        private final FSProjection projection;
        private final FSSelection selection;
        private final List<FSOrdering> orderings;
        private final SqlForPreparedStatement expected;

        public CreateSingleTableQuerySql(String table,
                                         FSProjection projection,
                                         FSSelection selection,
                                         List<FSOrdering> orderings,
                                         SqlForPreparedStatement expected) {
            this.table = table;
            this.projection = projection;
            this.selection = selection;
            this.orderings = orderings;
            this.expected = expected;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {   // 00: SELECT with null projection should use wildcard;
                            // null selection should take away WHERE clause;
                            // null ordering should take away ORDER BY clause
                            "table",
                            null,
                            null,
                            null,
                            new SqlForPreparedStatement(
                                    "SELECT * FROM table;",
                                    new String[0]
                            )
                    },
                    {   // 01: SELECT with non-null projection should use aliased columns;
                            // null selection should take away WHERE clause;
                            // null ordering should take away ORDER BY clause
                            "table01",
                            createProjection("table01", "col"),
                            null,
                            null,
                            new SqlForPreparedStatement(
                                    "SELECT table01.col AS table01_col FROM table01;",
                                    new String[0]
                            )
                    },
                    {   // 02: SELECT with non-null projection and multiple columns;
                            // null selection should take away WHERE clause;
                            // null ordering should take away ORDER BY clause
                            "table02",
                            createProjection("table02", "col01", "col02"),
                            null,
                            null,
                            new SqlForPreparedStatement(
                                    "SELECT table02.col01 AS table02_col01, table02.col02 AS table02_col02 FROM table02;",
                                    new String[0]
                            )
                    },
                    {   // 03: SELECT with non-null projection and multiple columns;
                            // non-null selection with one parameter should have correct WHERE clause;
                            // null ordering should take away ORDER BY clause
                            "table03",
                            createProjection("table03", "col01", "col02"),
                            createSelection("table03.col01 < ?", "5"),
                            null,
                            new SqlForPreparedStatement(
                                    "SELECT table03.col01 AS table03_col01, table03.col02 AS table03_col02 FROM table03 WHERE table03.col01 < ?;",
                                    new String[] {"5"}
                            )
                    },
                    {   // 04: SELECT with non-null projection and multiple columns;
                            // non-null selection with multiple parameters should have correct WHERE clause;
                            // null ordering should take away ORDER BY clause
                            "table04",
                            createProjection("table04", "col01", "col02"),
                            createSelection("table04.col01 < ? AND table04.col01 > ?", "5", "0"),
                            null,
                            new SqlForPreparedStatement(
                                    "SELECT table04.col01 AS table04_col01, table04.col02 AS table04_col02 FROM table04 WHERE table04.col01 < ? AND table04.col01 > ?;",
                                    new String[] {"5", "0"}
                            )
                    },
                    {   // 05: SELECT with non-null projection and multiple columns;
                            // non-null selection with multiple parameters should have correct WHERE clause;
                            // ordering with single condition should have correct ORDER BY clause
                            "table05",
                            createProjection("table05", "col01", "col02"),
                            createSelection("table05.col01 < ? AND table05.col01 > ?", "5", "0"),
                            Arrays.asList(new FSOrdering("table05", "_id", OrderBy.ORDER_DESC)),
                            new SqlForPreparedStatement(
                                    "SELECT table05.col01 AS table05_col01, table05.col02 AS table05_col02 FROM table05 WHERE table05.col01 < ? AND table05.col01 > ? ORDER BY table05._id DESC;",
                                    new String[] {"5", "0"}
                            )
                    },
                    {   // 06: SELECT with non-null projection and multiple columns
                            // non-null selection with multiple parameters should have correct WHERE clause;
                            // ordering with multiple conditions should have correct ORDER BY clause
                            "table06",
                            createProjection("table06", "col01", "col02"),
                            createSelection("table06.col01 < ? AND table06.col01 > ?", "5", "0"),
                            Arrays.asList(
                                    new FSOrdering("table06", "_id", OrderBy.ORDER_DESC),
                                    new FSOrdering("table06", "modified", OrderBy.ORDER_ASC)
                            ),
                            new SqlForPreparedStatement(
                                    "SELECT table06.col01 AS table06_col01, table06.col02 AS table06_col02 FROM table06 WHERE table06.col01 < ? AND table06.col01 > ? ORDER BY table06._id DESC, table06.modified ASC;",
                                    new String[] {"5", "0"}
                            )
                    },
                    {   // 07: SELECT with non-null projection and multiple columns--distinct
                            // non-null selection with multiple parameters should have correct WHERE clause;
                            // ordering with multiple conditions should have correct ORDER BY clause
                            "table07",
                            createDistinctProjection("table07", "col01", "col02"),
                            createSelection("table07.col01 < ? AND table07.col01 > ?", "5", "0"),
                            Arrays.asList(
                                    new FSOrdering("table07", "_id", OrderBy.ORDER_DESC),
                                    new FSOrdering("table07", "modified", OrderBy.ORDER_ASC)
                            ),
                            new SqlForPreparedStatement(
                                    "SELECT DISTINCT table07.col01 AS table07_col01, table07.col02 AS table07_col02 FROM table07 WHERE table07.col01 < ? AND table07.col01 > ? ORDER BY table07._id DESC, table07.modified ASC;",
                                    new String[] {"5", "0"}
                            )
                    },
                    {   // 08: SELECT with non-null projection and multiple columns--distinct
                            // non-null selection with multiple parameters should have correct WHERE clause--with limit from top without offset
                            // ordering with multiple conditions should have correct ORDER BY clause
                            "table08",
                            createDistinctProjection("table08", "col01", "col02"),
                            createSelection(createLimits(3), "table08.col01 < ? AND table08.col01 > ?", "5", "0"),
                            Arrays.asList(
                                    new FSOrdering("table08", "_id", OrderBy.ORDER_DESC),
                                    new FSOrdering("table08", "modified", OrderBy.ORDER_ASC)
                            ),
                            new SqlForPreparedStatement(
                                    "SELECT DISTINCT table08.col01 AS table08_col01, table08.col02 AS table08_col02 FROM table08 WHERE table08.col01 < ? AND table08.col01 > ? ORDER BY table08._id DESC, table08.modified ASC LIMIT 3;",
                                    new String[] {"5", "0"}
                            )
                    },
                    {   // 09: SELECT with non-null projection and multiple columns--distinct
                            // non-null selection with multiple parameters should have correct WHERE clause--with limit from top with offset
                            // ordering with multiple conditions should have correct ORDER BY clause
                            "table09",
                            createDistinctProjection("table09", "col01", "col02"),
                            createSelection(createLimits(3, 10), "table09.col01 < ? AND table09.col01 > ?", "5", "0"),
                            Arrays.asList(
                                    new FSOrdering("table09", "_id", OrderBy.ORDER_DESC),
                                    new FSOrdering("table09", "modified", OrderBy.ORDER_ASC)
                            ),
                            new SqlForPreparedStatement(
                                    "SELECT DISTINCT table09.col01 AS table09_col01, table09.col02 AS table09_col02 FROM table09 WHERE table09.col01 < ? AND table09.col01 > ? ORDER BY table09._id DESC, table09.modified ASC LIMIT 3 OFFSET 10;",
                                    new String[] {"5", "0"}
                            )
                    },
                    {   // 10: SELECT with non-null projection and multiple columns--distinct
                            // non-null selection with multiple parameters should have correct WHERE clause--with limit from top from bottom
                            // ordering with multiple conditions should have correct ORDER BY clause
                            "table10",
                            createDistinctProjection("table10", "col01", "col02"),
                            createSelection(createLimits(3, true), "table10.col01 < ? AND table10.col01 > ?", "5", "0"),
                            Arrays.asList(
                                    new FSOrdering("table10", "_id", OrderBy.ORDER_DESC),
                                    new FSOrdering("table10", "modified", OrderBy.ORDER_ASC)
                            ),
                            new SqlForPreparedStatement(
                                    // from the spec: In a compound SELECT, only the last or right-most simple SELECT may contain a LIMIT clause
                                    "SELECT DISTINCT table10.col01 AS table10_col01, table10.col02 AS table10_col02 FROM table10 WHERE table10.rowid IN (SELECT table10.rowid FROM table10 WHERE table10.col01 < ? AND table10.col01 > ? ORDER BY table10._id ASC, table10.modified DESC LIMIT 3) ORDER BY table10._id DESC, table10.modified ASC;",
                                    new String[] {"5", "0"}
                            )
                    }
            });
        }

        @Test
        public void shouldGenerateCorrectSqlForPreparedStatement() {
            SqlForPreparedStatement actual = generatorUnderTest.createQuerySql(table, projection, selection, orderings);
            assertEquals(expected, actual);
        }
    }
}
