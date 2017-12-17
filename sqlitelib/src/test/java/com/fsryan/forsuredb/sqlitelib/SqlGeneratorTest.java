package com.fsryan.forsuredb.sqlitelib;

import com.fsryan.forsuredb.api.*;
import com.fsryan.forsuredb.api.FSJoin.Type;
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

    static abstract class GenerateSqlForPreparedStatementTest extends SqlGeneratorTest {

        private final String table;
        private final SqlForPreparedStatement expected;

        GenerateSqlForPreparedStatementTest(String table, SqlForPreparedStatement expected) {
            this.table = table;
            this.expected = expected;
        }

        @Test
        public void shouldGenerateCorrectSqlForPreparedStatement() {
            SqlForPreparedStatement actual = createQuery(generatorUnderTest, table);
            assertEquals(expected, actual);
        }

        protected abstract SqlForPreparedStatement createQuery(SqlGenerator generator, String table);
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

    static abstract class QuerySqlCreationTest extends GenerateSqlForPreparedStatementTest {

        protected final FSSelection selection;
        protected final List<FSOrdering> orderings;

        public QuerySqlCreationTest(String table,
                                           FSSelection selection,
                                           List<FSOrdering> orderings,
                                           SqlForPreparedStatement expected) {
            super(table, expected);
            this.selection = selection;
            this.orderings = orderings;
        }
    }

    @RunWith(Parameterized.class)
    public static class SingleTableQuerySqlCreation extends QuerySqlCreationTest {

        private final FSProjection projection;

        public SingleTableQuerySqlCreation(String table,
                                           FSProjection projection,
                                           FSSelection selection,
                                           List<FSOrdering> orderings,
                                           SqlForPreparedStatement expected) {
            super(table, selection, orderings, expected);
            this.projection = projection;
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
                            // non-null selection with multiple parameters should have correct WHERE clause--with limit from bottom
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
                    },
                    {   // 11: SELECT with non-null projection and multiple columns--distinct
                            // non-null selection with multiple parameters should have correct WHERE clause--with limit and offset from bottom
                            // ordering with multiple conditions should have correct ORDER BY clause
                            "table11",
                            createDistinctProjection("table11", "col01", "col02"),
                            createSelection(createLimits(3, 4, true), "table11.col01 < ? AND table11.col01 > ?", "5", "0"),
                            Arrays.asList(
                                    new FSOrdering("table11", "_id", OrderBy.ORDER_DESC),
                                    new FSOrdering("table11", "modified", OrderBy.ORDER_ASC)
                            ),
                            new SqlForPreparedStatement(
                                    // from the spec: In a compound SELECT, only the last or right-most simple SELECT may contain a LIMIT clause
                                    "SELECT DISTINCT table11.col01 AS table11_col01, table11.col02 AS table11_col02 FROM table11 WHERE table11.rowid IN (SELECT table11.rowid FROM table11 WHERE table11.col01 < ? AND table11.col01 > ? ORDER BY table11._id ASC, table11.modified DESC LIMIT 3 OFFSET 4) ORDER BY table11._id DESC, table11.modified ASC;",
                                    new String[] {"5", "0"}
                            )
                    }
            });
        }

        @Override
        protected SqlForPreparedStatement createQuery(SqlGenerator generator, String table) {
            return generator.createQuerySql(table, projection, selection, orderings);
        }
    }

    @RunWith(Parameterized.class)
    public static class JoinQuery extends QuerySqlCreationTest {

        private final List<FSJoin> joins;
        private final List<FSProjection> projections;

        public JoinQuery(String table,
                         List<FSJoin> joins,
                         List<FSProjection> projections,
                         FSSelection selection,
                         List<FSOrdering> orderings,
                         SqlForPreparedStatement expected) {
            super(table, selection, orderings, expected);
            this.joins = joins;
            this.projections = projections;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {   // 00: SELECT with null joins should not create join query;
                            // null projections should use wildcard;
                            // null selection should take away WHERE clause;
                            // null ordering should take away ORDER BY clause
                            "table",
                            null,
                            null,
                            null,
                            null,
                            new SqlForPreparedStatement(
                                    "SELECT * FROM table;",
                                    new String[0]
                            )
                    },

                    // 01-08 are testing kinds of joins

                    {   // 01: SELECT with one natural join should properly add the join;
                            // null projections should use wildcard;
                            // null selection should take away WHERE clause;
                            // null ordering should take away ORDER BY clause
                            "child01",
                            Arrays.asList(
                                    new FSJoin(Type.NATURAL, "parent01", "child01", stringMapOf())
                            ),
                            null,
                            null,
                            null,
                            new SqlForPreparedStatement(
                                    "SELECT * FROM child01 NATURAL JOIN parent01;",
                                    new String[0]
                            )
                    },
                    {   // 02: SELECT with one left join should properly add the join;
                            // null projections should use wildcard;
                            // null selection should take away WHERE clause;
                            // null ordering should take away ORDER BY clause
                            "child02",
                            Arrays.asList(
                                    new FSJoin(Type.LEFT, "parent02", "child02", stringMapOf("parent02_id", "_id"))
                            ),
                            null,
                            null,
                            null,
                            new SqlForPreparedStatement(
                                    "SELECT * FROM child02 LEFT JOIN parent02 ON child02.parent02_id=parent02._id;",
                                    new String[0]
                            )
                    },
                    {   // 03: SELECT with one inner join should properly add the join;
                            // null projections should use wildcard;
                            // null selection should take away WHERE clause;
                            // null ordering should take away ORDER BY clause
                            "child03",
                            Arrays.asList(
                                    new FSJoin(Type.INNER, "parent03", "child03", stringMapOf("parent03_id", "_id"))
                            ),
                            null,
                            null,
                            null,
                            new SqlForPreparedStatement(
                                    "SELECT * FROM child03 INNER JOIN parent03 ON child03.parent03_id=parent03._id;",
                                    new String[0]
                            )
                    },
                    {   // 04: SELECT with one outer join should properly add the join;
                            // null projections should use wildcard;
                            // null selection should take away WHERE clause;
                            // null ordering should take away ORDER BY clause
                            "child04",
                            Arrays.asList(
                                    new FSJoin(Type.OUTER, "parent04", "child04", stringMapOf("parent04_id", "_id"))
                            ),
                            null,
                            null,
                            null,
                            new SqlForPreparedStatement(
                                    "SELECT * FROM child04 OUTER JOIN parent04 ON child04.parent04_id=parent04._id;",
                                    new String[0]
                            )
                    },
                    {   // 05: SELECT with one left outer join should properly add the join;
                            // null projections should use wildcard;
                            // null selection should take away WHERE clause;
                            // null ordering should take away ORDER BY clause
                            "child05",
                            Arrays.asList(
                                    new FSJoin(Type.LEFT_OUTER, "parent05", "child05", stringMapOf("parent05_id", "_id"))
                            ),
                            null,
                            null,
                            null,
                            new SqlForPreparedStatement(
                                    "SELECT * FROM child05 LEFT OUTER JOIN parent05 ON child05.parent05_id=parent05._id;",
                                    new String[0]
                            )
                    },
                    {   // 06: SELECT with one cross join should properly add the join;
                            // null projections should use wildcard;
                            // null selection should take away WHERE clause;
                            // null ordering should take away ORDER BY clause
                            "child06",
                            Arrays.asList(
                                    new FSJoin(Type.CROSS, "parent06", "child06", stringMapOf("parent06_id", "_id"))
                            ),
                            null,
                            null,
                            null,
                            new SqlForPreparedStatement(
                                    "SELECT * FROM child06 CROSS JOIN parent06 ON child06.parent06_id=parent06._id;",
                                    new String[0]
                            )
                    },
                    {   // 06: SELECT with two joins should properly add the join;
                            // null projections should use wildcard;
                            // null selection should take away WHERE clause;
                            // null ordering should take away ORDER BY clause
                            "child06",
                            Arrays.asList(
                                    new FSJoin(Type.CROSS, "parent06", "child06", stringMapOf("parent06_id", "_id"))
                            ),
                            null,
                            null,
                            null,
                            new SqlForPreparedStatement(
                                    "SELECT * FROM child06 CROSS JOIN parent06 ON child06.parent06_id=parent06._id;",
                                    new String[0]
                            )
                    },
                    {   // 07: SELECT with two joins should properly add the joins;
                            // null projections should use wildcard;
                            // null selection should take away WHERE clause;
                            // null ordering should take away ORDER BY clause
                            "child07",
                            Arrays.asList(
                                    new FSJoin(Type.LEFT, "parent07", "child07", stringMapOf("parent07_id", "_id")),
                                    new FSJoin(Type.INNER, "child07", "grandchild07", stringMapOf("child07_id", "_id"))
                            ),
                            null,
                            null,
                            null,
                            new SqlForPreparedStatement(
                                    "SELECT * FROM child07 LEFT JOIN parent07 ON child07.parent07_id=parent07._id INNER JOIN grandchild07 ON grandchild07.child07_id=child07._id;",
                                    new String[0]
                            )
                    },
                    {   // 08: SELECT with single composite join should properly add the join;
                            // null projections should use wildcard;
                            // null selection should take away WHERE clause;
                            // null ordering should take away ORDER BY clause
                            "child08",
                            Arrays.asList(
                                    new FSJoin(Type.INNER, "parent08", "child08", stringMapOf(
                                            "parent08_first_name", "first_name",
                                            "parent08_last_name", "last_name"
                                    ))
                            ),
                            null,
                            null,
                            null,
                            new SqlForPreparedStatement(
                                    "SELECT * FROM child08 INNER JOIN parent08 ON child08.parent08_last_name=parent08.last_name AND child08.parent08_first_name=parent08.first_name;",
                                    new String[0]
                            )
                    },

                    // 09 - 11 test the projections

                    {   // 09: SELECT with single join;
                            // single projection should unambiguously alias the columns;
                            // null selection should take away WHERE clause;
                            // null ordering should take away ORDER BY clause
                            "child09",
                            Arrays.asList(
                                    new FSJoin(Type.INNER, "parent09", "child09", stringMapOf("parent09_id", "_id"))
                            ),
                            Arrays.asList(
                                    createProjection("child09", "col1", "col2")
                            ),
                            null,
                            null,
                            new SqlForPreparedStatement(
                                    "SELECT child09.col1 AS child09_col1, child09.col2 AS child09_col2 FROM child09 INNER JOIN parent09 ON child09.parent09_id=parent09._id;",
                                    new String[0]
                            )
                    },
                    {   // 10: SELECT with single join;
                            // multiple projections should unambiguously alias the columns;
                            // null selection should take away WHERE clause;
                            // null ordering should take away ORDER BY clause
                            "child10",
                            Arrays.asList(
                                    new FSJoin(Type.INNER, "parent10", "child10", stringMapOf("parent10_id", "_id"))
                            ),
                            Arrays.asList(
                                    createProjection("child10", "col1", "col2"),
                                    createProjection("parent10", "col1", "col2")
                            ),
                            null,
                            null,
                            new SqlForPreparedStatement(
                                    "SELECT child10.col1 AS child10_col1, child10.col2 AS child10_col2, parent10.col1 AS parent10_col1, parent10.col2 AS parent10_col2 FROM child10 INNER JOIN parent10 ON child10.parent10_id=parent10._id;",
                                    new String[0]
                            )
                    },
                    {   // 11: SELECT with single join;
                            // single distinct projection;
                            // null selection should take away WHERE clause;
                            // null ordering should take away ORDER BY clause
                            "child11",
                            Arrays.asList(
                                    new FSJoin(Type.INNER, "parent11", "child11", stringMapOf("parent11_id", "_id"))
                            ),
                            Arrays.asList(
                                    createDistinctProjection("child11", "col1", "col2")
                            ),
                            null,
                            null,
                            new SqlForPreparedStatement(
                                    "SELECT DISTINCT child11.col1 AS child11_col1, child11.col2 AS child11_col2 FROM child11 INNER JOIN parent11 ON child11.parent11_id=parent11._id;",
                                    new String[0]
                            )
                    },

                    // Further testing should not be necessary due to the fact that it would be doubling testing with SingleTableQuerySqlCreation
            });
        }

        @Override
        protected SqlForPreparedStatement createQuery(SqlGenerator generator, String table) {
            return generator.createQuerySql(table, joins, projections, selection, orderings);
        }
    }
}
