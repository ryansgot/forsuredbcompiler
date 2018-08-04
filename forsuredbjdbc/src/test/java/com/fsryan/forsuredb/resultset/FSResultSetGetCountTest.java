package com.fsryan.forsuredb.resultset;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

public abstract class FSResultSetGetCountTest {

    @Mock
    protected ResultSet mockResultSet;

    protected FSResultSet fsResultSetUnderTest;

    protected final int resultSetType;
    protected final int position;
    protected final boolean isLast;

    public FSResultSetGetCountTest(int resultSetType, boolean isLast, int position) {
        this.resultSetType = resultSetType;
        this.isLast = isLast;
        this.position = position;
    }

    @Before
    public void setUpMock() throws SQLException {
        MockitoAnnotations.initMocks(this);

        when(mockResultSet.getType()).thenReturn(resultSetType);
        when(mockResultSet.getRow()).thenReturn(position);
        when(mockResultSet.isLast()).thenReturn(isLast);

        fsResultSetUnderTest = new FSResultSet(mockResultSet);
    }

    public static class ForwardOnlyFailureCases extends FSResultSetGetCountTest {

        public ForwardOnlyFailureCases() {
            super(ResultSet.TYPE_FORWARD_ONLY, false, 0);
        }

        @Test(expected = RuntimeException.class)
        public void shouldPropagateAsRuntimeException() throws Exception {
            when(mockResultSet.isLast()).thenThrow(new SQLException());
            fsResultSetUnderTest.getCount();
        }

        @Test(expected = IllegalStateException.class)
        public void shouldThrowWhenPositionNotLast() throws Exception {
            when(mockResultSet.isLast()).thenReturn(false);
            fsResultSetUnderTest.getCount();
        }
    }

    public static abstract class SuccessCases extends FSResultSetGetCountTest {

        private final int expectedCount;

        public SuccessCases(int resultSetType, boolean isLast, int position, int expectedCount) {
            super(resultSetType, isLast, position);
            this.expectedCount = expectedCount;
        }

        @Test
        public void shouldReturnCorrectRowCount() throws SQLException {
            assertEquals(expectedCount, fsResultSetUnderTest.getCount());
        }
    }

    public static class ForwardOnlySuccessCase extends SuccessCases {
        public ForwardOnlySuccessCase() {
            super(ResultSet.TYPE_FORWARD_ONLY, true, 99, 100);
        }

        @Test
        public void shouldCallMethodsInCorrectOrder() throws SQLException {
            fsResultSetUnderTest.getCount();

            InOrder inOrder = inOrder(mockResultSet);
            inOrder.verify(mockResultSet).getType();
            inOrder.verify(mockResultSet).isLast();
            inOrder.verify(mockResultSet).getRow();
        }
    }

    @RunWith(Parameterized.class)
    public static class NonForwardOnlySuccessCases extends SuccessCases {

        private final int positionAfterMoveToLast;

        public NonForwardOnlySuccessCases(int resultSetType,
                                          int position,
                                          int positionAfterMoveToLast,
                                          int expectedCount) {
            super(resultSetType, false, position, expectedCount);
            this.positionAfterMoveToLast = positionAfterMoveToLast;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {
                            ResultSet.TYPE_SCROLL_INSENSITIVE,  // resultSetType
                            0,                                  // position
                            4,                                  // positionAfterMoveToLast
                            5                                   // expectedCount
                    },
                    {
                            ResultSet.TYPE_SCROLL_SENSITIVE,    // resultSetType
                            4,                                  // position
                            78,                                 // positionAfterMoveToLast
                            79                                  // expectedCount
                    },
                    {
                            ResultSet.TYPE_SCROLL_INSENSITIVE,  // resultSetType
                            4,                                  // position
                            4,                                  // positionAfterMoveToLast
                            5                                   // expectedCount
                    }
            });
        }

        @Before
        public void setUpPositionAfterMoveToLast() throws SQLException {
            when(mockResultSet.getRow()).thenReturn(position)
                    .thenReturn(positionAfterMoveToLast);
        }

        @Test
        public void shouldCallMethodsInCorrectOrder() throws SQLException {
            fsResultSetUnderTest.getCount();

            InOrder inOrder = inOrder(mockResultSet);
            inOrder.verify(mockResultSet).getType();
            inOrder.verify(mockResultSet).getRow();
            inOrder.verify(mockResultSet).last();
            inOrder.verify(mockResultSet).getRow();
            inOrder.verify(mockResultSet).absolute(eq(position));
        }
    }
}
