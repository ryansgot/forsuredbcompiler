package com.fsryan.forsuredb.queryable;

import com.fsryan.forsuredb.api.*;
import com.fsryan.forsuredb.api.sqlgeneration.DBMSIntegrator;
import com.fsryan.forsuredb.api.sqlgeneration.SqlForPreparedStatement;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.fsryan.forsuredb.util.Randomizer.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public abstract class JdbcQueryableTest {

    static final byte[] byteArray = new byte[] {1, 2, 3, 4, 5, 6, 7, 8};
    static final String tableName = "table";

    @Captor
    protected ArgumentCaptor<List<String>> columnListCaptor;
    @Mock
    protected FSSelection mockSelection;
    @Mock
    protected List<FSOrdering> mockOrderings;
    @Mock
    protected Connection mockConnection;
    @Mock
    protected PreparedStatement mockPreparedStatement;
    @Mock
    protected ResultSet mockResultSet;
    @Mock
    protected JdbcQueryable.DBProvider mockDbProvider;
    @Mock
    protected DBMSIntegrator mockSqlGenerator;

    protected JdbcQueryable queryableUnderTest;

    private final DirectLocator directLocator;

    public JdbcQueryableTest() {
        this(new DirectLocator(tableName));
    }

    public JdbcQueryableTest(DirectLocator directLocator) {
        this.directLocator = directLocator;
    }

    @Before
    public void setUpQueryableUnderTest() throws SQLException {
        MockitoAnnotations.initMocks(this);

        when(mockDbProvider.readableDb()).thenReturn(mockConnection);
        when(mockDbProvider.writeableDb()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(eq(""))).thenReturn(mockPreparedStatement);

        queryableUnderTest = new JdbcQueryable(directLocator, mockDbProvider, mockSqlGenerator);
    }

    public static class Insert extends JdbcQueryableTest {

        @Before
        public void setUpPreparedStatement() throws SQLException {
            // the actual sql string doesn't really matter, as that is delegated to the DBMSIntegrator
            when(mockConnection.prepareStatement(nullable(String.class), eq(Statement.RETURN_GENERATED_KEYS)))
                    .thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);
            when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        }

        @Test
        public void shouldReturnNullIfNothingInserted() throws SQLException {
            when(mockPreparedStatement.executeUpdate()).thenReturn(0);
            assertNull(queryableUnderTest.insert(new TypedRecordContainer()));
        }

        @Test
        public void shouldHackEmptyRecordContainerToContainDeletedDefault() throws SQLException {
            TypedRecordContainer trc = new TypedRecordContainer();

            queryableUnderTest.insert(trc);

            verify(mockSqlGenerator).newSingleRowInsertionSql(eq(tableName), eq(Arrays.asList("deleted")));
            verify(mockPreparedStatement).setInt(eq(1), eq(trc.typedGet("deleted")));
        }

        @Test
        public void shouldBindObjectsAndCallMethodsInCorrectOrder() throws SQLException {
            TypedRecordContainer trc = createRandomStringTRC();

            queryableUnderTest.insert(trc);

            InOrder inOrder = inOrder(mockSqlGenerator, mockPreparedStatement, mockResultSet);
            inOrder.verify(mockSqlGenerator).newSingleRowInsertionSql(eq(tableName), columnListCaptor.capture());
            List<String> actualColumnList = columnListCaptor.getValue();
            for (int pos = 0; pos < actualColumnList.size(); pos++ ) {
                inOrder.verify(mockPreparedStatement).setString(eq(pos + 1), eq(trc.typedGet(actualColumnList.get(pos))));
            }
            inOrder.verify(mockPreparedStatement).executeUpdate();
            inOrder.verify(mockPreparedStatement).getGeneratedKeys();
            inOrder.verify(mockResultSet).next();
            inOrder.verify(mockPreparedStatement).close();
        }

        // tests the binding helper method through insertion--do not need to test the binding helper again

        @Test
        public void shouldBindIntCorrectly() throws SQLException {
            TypedRecordContainer trc = createRandomIntTRC(1);

            queryableUnderTest.insert(trc);

            verify(mockSqlGenerator).newSingleRowInsertionSql(eq(tableName), eq(Arrays.asList("col1")));
            verify(mockPreparedStatement).setInt(eq(1), eq(trc.typedGet("col1")));
        }

        @Test
        public void shouldBindLongCorrectly() throws SQLException {
            TypedRecordContainer trc = createRandomLongTRC(1);

            queryableUnderTest.insert(trc);

            verify(mockSqlGenerator).newSingleRowInsertionSql(eq(tableName), eq(Arrays.asList("col1")));
            verify(mockPreparedStatement).setLong(eq(1), eq(trc.typedGet("col1")));
        }

        @Test
        public void shouldBindFloatCorrectly() throws SQLException {
            TypedRecordContainer trc = createRandomFloatTRC(1);

            queryableUnderTest.insert(trc);

            verify(mockSqlGenerator).newSingleRowInsertionSql(eq(tableName), eq(Arrays.asList("col1")));
            verify(mockPreparedStatement).setFloat(eq(1), eq(trc.typedGet("col1")));
        }

        @Test
        public void shouldBindDoubleCorrectly() throws SQLException {
            TypedRecordContainer trc = createRandomDoubleTRC(1);

            queryableUnderTest.insert(trc);

            verify(mockSqlGenerator).newSingleRowInsertionSql(eq(tableName), eq(Arrays.asList("col1")));
            verify(mockPreparedStatement).setDouble(eq(1), eq(trc.typedGet("col1")));
        }

        @Test
        public void shouldBindByteArrayCorrectly() throws SQLException {
            TypedRecordContainer trc = createRandomByteArrayTRC(1);

            queryableUnderTest.insert(trc);

            verify(mockSqlGenerator).newSingleRowInsertionSql(eq(tableName), eq(Arrays.asList("col1")));
            verify(mockPreparedStatement).setBytes(eq(1), eq(trc.typedGet("col1")));
        }
    }

    public static class Update extends JdbcQueryableTest {

        private String[] replacements = new String[] {"r1", "r2", "r3"};

        @Before
        public void createMockSelectionAndOrderings() throws SQLException {
            when(mockSqlGenerator.createUpdateSql(eq(tableName), anyList(), eq(mockSelection), eq(mockOrderings)))
                    .thenReturn(new SqlForPreparedStatement("", replacements));
        }

        @Test
        public void shouldNotUpdateIfRecordContainerEmpty() {
            assertEquals(0, queryableUnderTest.update(new TypedRecordContainer(), null, null));
        }

        @Test
        public void shouldPassThroughCorrectArgumentsToSqlGenerator() {
            TypedRecordContainer trc = createRandomIntTRC();

            queryableUnderTest.update(trc, mockSelection, mockOrderings);

            verify(mockSqlGenerator).createUpdateSql(eq(tableName), columnListCaptor.capture(), eq(mockSelection), eq(mockOrderings));
            for (String capturedColumn : columnListCaptor.getValue()) {
                assertTrue(trc.keySet().contains(capturedColumn));
            }
        }

        @Test
        public void shouldBindObjectsAndCallMethodsInCorrectOrder() throws SQLException {
            TypedRecordContainer trc = createRandomIntTRC();

            queryableUnderTest.update(trc, mockSelection, mockOrderings);

            InOrder inOrder = inOrder(mockSqlGenerator, mockPreparedStatement, mockResultSet);
            inOrder.verify(mockSqlGenerator).createUpdateSql(eq(tableName), columnListCaptor.capture(), any(FSSelection.class), anyList());
            List<String> actualColumnList = columnListCaptor.getValue();
            for (int pos = 0; pos < actualColumnList.size(); pos++ ) {
                inOrder.verify(mockPreparedStatement).setInt(eq(pos + 1), eq(trc.typedGet(actualColumnList.get(pos))));
            }
            for (int pos = 0; pos < replacements.length; pos++ ) {
                inOrder.verify(mockPreparedStatement).setString(eq(actualColumnList.size() + pos + 1), eq(replacements[pos]));
            }
            inOrder.verify(mockPreparedStatement).executeUpdate();
            inOrder.verify(mockPreparedStatement).close();
        }
    }

    public static class Delete extends JdbcQueryableTest {

        private String[] replacements = new String[] {"r1", "r2", "r3"};

        @Before
        public void createMockSelectionAndOrderings() throws SQLException {
            when(mockSqlGenerator.createDeleteSql(eq(tableName), eq(mockSelection), eq(mockOrderings)))
                    .thenReturn(new SqlForPreparedStatement("", replacements));
        }

        @Test
        public void shouldPassThroughCorrectArgumentsToSqlGenerator() {
            queryableUnderTest.delete(mockSelection, mockOrderings);

            verify(mockSqlGenerator).createDeleteSql(eq(tableName), eq(mockSelection), eq(mockOrderings));
        }

        @Test
        public void shouldBindObjectsAndCallMethodsInCorrectOrder() throws SQLException {
            queryableUnderTest.delete(mockSelection, mockOrderings);

            InOrder inOrder = inOrder(mockSqlGenerator, mockPreparedStatement, mockResultSet);
            inOrder.verify(mockSqlGenerator).createDeleteSql(eq(tableName), any(FSSelection.class), anyList());
            for (int pos = 0; pos < replacements.length; pos++ ) {
                inOrder.verify(mockPreparedStatement).setString(eq( pos + 1), eq(replacements[pos]));
            }
            inOrder.verify(mockPreparedStatement).executeUpdate();
            inOrder.verify(mockPreparedStatement).close();
        }
    }

    public static class Query extends JdbcQueryableTest {

        private String[] replacements = new String[] {"r1", "r2", "r3"};
        private FSProjection mockProjection;

        @Before
        public void setUpMockProjection() throws SQLException {
            mockProjection = mock(FSProjection.class);
            when(mockDbProvider.readableDb()).thenReturn(mockConnection);
            when(mockSqlGenerator.createQuerySql(eq(tableName), eq(mockProjection), eq(mockSelection), eq(mockOrderings)))
                    .thenReturn(new SqlForPreparedStatement("", replacements));
        }

        @Test
        public void shouldPassThroughCorrectArgumentsToSqlGeneratorOnSingleTableQuery() {
            queryableUnderTest.query(mockProjection, mockSelection, mockOrderings);

            verify(mockSqlGenerator).createQuerySql(eq(tableName), eq(mockProjection), eq(mockSelection), eq(mockOrderings));
        }

        @Test
        public void shouldPassThroughCorrectArgumentsToSqlGeneratorOnJoinQuery() {
            List<FSProjection> mockProjections = mock(List.class);
            List<FSJoin> mockJoins = mock(List.class);
            when(mockSqlGenerator.createQuerySql(eq(tableName), eq(mockJoins), eq(mockProjections), eq(mockSelection), eq(mockOrderings)))
                    .thenReturn(new SqlForPreparedStatement("", replacements));
            queryableUnderTest.query(mockJoins, mockProjections, mockSelection, mockOrderings);

            verify(mockSqlGenerator).createQuerySql(eq(tableName), eq(mockJoins), eq(mockProjections), eq(mockSelection), eq(mockOrderings));
        }

        @Test
        public void shouldBindObjectsAndCallMethodsInCorrectOrder() throws SQLException {
            queryableUnderTest.query(mockProjection, mockSelection, mockOrderings);

            InOrder inOrder = inOrder(mockSqlGenerator, mockPreparedStatement, mockResultSet);
            inOrder.verify(mockSqlGenerator).createQuerySql(eq(tableName), any(FSProjection.class), any(FSSelection.class), anyList());
            for (int pos = 0; pos < replacements.length; pos++ ) {
                inOrder.verify(mockPreparedStatement).setString(eq( pos + 1), eq(replacements[pos]));
            }
            inOrder.verify(mockPreparedStatement).executeQuery();

            verify(mockPreparedStatement, times(0)).close();
        }
    }

    @SuppressWarnings("MagicConstant")
    public static class Upsert extends JdbcQueryableTest {

        private InOrder inOrder;

        @Before
        public void setUpConnection() throws SQLException {
            when(mockDbProvider.readableDb()).thenReturn(mockConnection);
            when(mockDbProvider.writeableDb()).thenReturn(mockConnection);

            when(mockSqlGenerator.createQuerySql(eq(tableName), nullable(FSProjection.class), eq(mockSelection), eq(mockOrderings)))
                    .thenReturn(new SqlForPreparedStatement("", new String[0]));
            when(mockSqlGenerator.createUpdateSql(eq(tableName), anyList(), eq(mockSelection), eq(mockOrderings)))
                    .thenReturn(new SqlForPreparedStatement("", new String[0]));

            when(mockConnection.getAutoCommit()).thenReturn(true);
            when(mockConnection.prepareStatement(nullable(String.class))).thenReturn(mockPreparedStatement);
            doReturn(mockPreparedStatement)
                    .when(mockConnection).prepareStatement(nullable(String.class), eq(Statement.RETURN_GENERATED_KEYS));

            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);

            when(mockResultSet.isBeforeFirst()).thenReturn(true);

            inOrder = inOrder(mockDbProvider, mockConnection, mockPreparedStatement, mockSqlGenerator, mockResultSet);
        }

        @Test
        public void shouldInsertWhenNoRecordsExist() throws SQLException {
            when(mockResultSet.next()).thenReturn(false).thenReturn(true);
            when(mockResultSet.getLong(eq(1))).thenReturn(1L);
            when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);

            SaveResult result = queryableUnderTest.upsert(createRandomTRC(), mockSelection, mockOrderings);

            assertNotNull(result.inserted());
            inOrder.verify(mockDbProvider).writeableDb();
            inOrder.verify(mockConnection).getAutoCommit();
            inOrder.verify(mockConnection).setAutoCommit(false);
            inOrder.verify(mockSqlGenerator).createQuerySql(eq(tableName), nullable(FSProjection.class), eq(mockSelection), eq(mockOrderings));
            inOrder.verify(mockDbProvider).readableDb();
            inOrder.verify(mockConnection).prepareStatement(nullable(String.class));
            inOrder.verify(mockPreparedStatement).executeQuery();
            inOrder.verify(mockResultSet).isBeforeFirst();
            inOrder.verify(mockResultSet).next();
            inOrder.verify(mockSqlGenerator).newSingleRowInsertionSql(eq(tableName), anyList());
            inOrder.verify(mockDbProvider).writeableDb();
            inOrder.verify(mockConnection).prepareStatement(nullable(String.class), eq(Statement.RETURN_GENERATED_KEYS));
            inOrder.verify(mockPreparedStatement).executeUpdate();
            inOrder.verify(mockPreparedStatement).getGeneratedKeys();
            inOrder.verify(mockResultSet).next();
            inOrder.verify(mockResultSet).getLong(eq(1));
            inOrder.verify(mockConnection).commit();
            inOrder.verify(mockConnection).setAutoCommit(eq(true));
        }

        @Test
        public void shouldUpdateWhenRecordExists() throws SQLException {
            when(mockResultSet.next()).thenReturn(true);
            when(mockPreparedStatement.executeUpdate()).thenReturn(20);

            SaveResult result = queryableUnderTest.upsert(createRandomTRC(), mockSelection, mockOrderings);

            assertNull(result.inserted());
            assertEquals(20, result.rowsAffected());
            inOrder.verify(mockDbProvider).writeableDb();
            inOrder.verify(mockConnection).getAutoCommit();
            inOrder.verify(mockConnection).setAutoCommit(false);
            inOrder.verify(mockSqlGenerator).createQuerySql(eq(tableName), nullable(FSProjection.class), eq(mockSelection), eq(mockOrderings));
            inOrder.verify(mockDbProvider).readableDb();
            inOrder.verify(mockConnection).prepareStatement(nullable(String.class));
            inOrder.verify(mockPreparedStatement).executeQuery();
            inOrder.verify(mockResultSet).isBeforeFirst();
            inOrder.verify(mockResultSet).next();
            inOrder.verify(mockSqlGenerator).createUpdateSql(eq(tableName), anyList(), eq(mockSelection), eq(mockOrderings));
            inOrder.verify(mockDbProvider).writeableDb();
            inOrder.verify(mockConnection).prepareStatement(nullable(String.class));
            inOrder.verify(mockPreparedStatement).executeUpdate();
            inOrder.verify(mockConnection).commit();
            inOrder.verify(mockConnection).setAutoCommit(eq(true));
        }

        @Test
        public void shouldRollbackOnSqlException() throws SQLException {
            when(mockResultSet.next()).thenReturn(true);
            when(mockPreparedStatement.executeUpdate()).thenThrow(RuntimeException.class);

            queryableUnderTest.upsert(createRandomTRC(), mockSelection, mockOrderings);
            verify(mockConnection).rollback();
            verify(mockConnection, times(0)).commit();
            verify(mockConnection).setAutoCommit(true);
        }
    }

}
