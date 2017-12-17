package com.fsryan.forsuredb.queryable;

import com.fsryan.forsuredb.api.TypedRecordContainer;
import com.fsryan.forsuredb.api.sqlgeneration.DBMSIntegrator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.fsryan.forsuredb.util.Randomizer.*;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class JdbcQueryableTest {

    static final byte[] byteArray = new byte[] {1, 2, 3, 4, 5, 6, 7, 8};
    static final String tableName = "table";

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
        public void shouldBindObjectsInCorrectOrder() throws SQLException {
            ArgumentCaptor<List<String>> columnListCaptor = ArgumentCaptor.forClass(List.class);
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
        }

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

        @Test
        public void shouldHackEmptyRecordContainerToContainDeletedDefault() throws SQLException {
            TypedRecordContainer trc = new TypedRecordContainer();

            queryableUnderTest.insert(trc);

            verify(mockSqlGenerator).newSingleRowInsertionSql(eq(tableName), eq(Arrays.asList("deleted")));
            verify(mockPreparedStatement).setInt(eq(1), eq(trc.typedGet("deleted")));
        }

        @Test
        public void shouldReturnNullIfNothingInserted() throws SQLException {
            when(mockPreparedStatement.executeUpdate()).thenReturn(0);
            assertNull(queryableUnderTest.insert(new TypedRecordContainer()));
        }
    }

    static String stringify(TypedRecordContainer trc) {
        StringBuilder buf = new StringBuilder("TypedRecordContainer{");
        List<String> keys = new ArrayList<>(trc.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            buf.append(key).append('=').append(trc.get(key))
                    .append('(').append(trc.getType(key)).append(')')
                    .append(", ");
        }
        return buf.delete(buf.length() - 2, buf.length()).append('}').toString();
    }

}
