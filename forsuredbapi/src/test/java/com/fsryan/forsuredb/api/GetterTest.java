package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.api.sqlgeneration.DBMSIntegrator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public abstract class GetterTest<T extends BaseGetter> {

    private static final Date testDate = new Date();

    protected final String tableName = "table_name";  // TODO: make this variable

    @Mock protected Retriever mockRetriever;
    @Mock protected DBMSIntegrator mockDBMSIntegrator;
    protected T getterUnderTest;

    @Before
    public void setUpMocks() {
        MockitoAnnotations.initMocks(this);

        when(mockDBMSIntegrator.parseDate(anyString())).thenReturn(testDate);
        getterUnderTest = createGetterToTest();
    }

    protected abstract T createGetterToTest();

    public static abstract class BaseGetterTest extends GetterTest<BaseGetter> {
        @Override
        protected BaseGetter createGetterToTest() {
            return new BaseGetter(mockDBMSIntegrator, tableName) {};
        }
    }

    public static class BaseGetterRetrieval extends BaseGetterTest {

        private InOrder inOrder;

        @Before
        public void reinitInOrder() {
            inOrder = inOrder(mockDBMSIntegrator, mockRetriever);
        }

        @Test
        public void shouldCallRetrieverGetStringMethodWhenGettingCreatedDate() {
            final String unambiguousColumn = tableName + "_created";
            when(mockDBMSIntegrator.unambiguousRetrievalColumn(eq(tableName), eq("created"))).thenReturn(unambiguousColumn);
            when(mockRetriever.getString(eq(unambiguousColumn))).thenReturn(testDate.toString());

            Date actual = getterUnderTest.created(mockRetriever);

            inOrder.verify(mockDBMSIntegrator).unambiguousRetrievalColumn(eq(tableName), eq("created"));
            inOrder.verify(mockRetriever).getString(eq(unambiguousColumn));
            inOrder.verify(mockDBMSIntegrator).parseDate(eq(testDate.toString()));
            assertEquals(testDate, actual);
        }

        @Test
        public void shouldCallRetrieverGetStringMethodWhenGettingModifiedDate() {
            final String unambiguousColumn = tableName + "_modified";
            when(mockDBMSIntegrator.unambiguousRetrievalColumn(eq(tableName), eq("modified"))).thenReturn(unambiguousColumn);
            when(mockRetriever.getString(eq(unambiguousColumn))).thenReturn(testDate.toString());

            Date actual = getterUnderTest.modified(mockRetriever);

            inOrder.verify(mockDBMSIntegrator).unambiguousRetrievalColumn(eq(tableName), eq("modified"));
            inOrder.verify(mockRetriever).getString(eq(unambiguousColumn));
            inOrder.verify(mockDBMSIntegrator).parseDate(eq(testDate.toString()));
            assertEquals(testDate, actual);
        }

        @Test
        public void shouldCallRetrieverGetLongMethodWhenGettingId() {
            final String unambiguousColumn = tableName + "__id";
            final long expected = 4738446L;
            when(mockDBMSIntegrator.unambiguousRetrievalColumn(eq(tableName), eq("_id"))).thenReturn(unambiguousColumn);
            when(mockRetriever.getLong(eq(unambiguousColumn))).thenReturn(expected);

            long actual = getterUnderTest.id(mockRetriever);

            inOrder.verify(mockDBMSIntegrator).unambiguousRetrievalColumn(eq(tableName), eq("_id"));
            inOrder.verify(mockRetriever).getLong(unambiguousColumn);
            assertEquals(expected, actual);
        }

        @Test
        public void shouldCallRetrieverGetIntMethodWhenGettingDeletedValue() {
            final String unambiguousColumn = tableName + "_deleted";
            when(mockDBMSIntegrator.unambiguousRetrievalColumn(eq(tableName), eq("deleted"))).thenReturn(unambiguousColumn);
            when(mockRetriever.getInt(eq(unambiguousColumn))).thenReturn(1);

            boolean actual = getterUnderTest.deleted(mockRetriever);

            inOrder.verify(mockDBMSIntegrator).unambiguousRetrievalColumn(eq(tableName), eq("deleted"));
            inOrder.verify(mockRetriever).getInt(eq(unambiguousColumn));
            assertTrue(actual);
        }

        @Test
        public void shouldCallRetrieverGetStringMethodWhenGettingStringValue() {
            final String expected = "expected";
            final String unambiguousColumn = tableName + "_some_string_column";
            when(mockDBMSIntegrator.unambiguousRetrievalColumn(eq(tableName), eq("some_string_column"))).thenReturn(unambiguousColumn);
            when(mockRetriever.getString(eq(unambiguousColumn))).thenReturn(expected);

            String actual = getterUnderTest.retrieveString(mockRetriever, "some_string_column");

            inOrder.verify(mockDBMSIntegrator).unambiguousRetrievalColumn(eq(tableName), eq("some_string_column"));
            inOrder.verify(mockRetriever).getString(eq(unambiguousColumn));
            assertEquals(expected, actual);
        }

        @Test
        public void shouldCallRetrieverGetStringMethodWhenGettingBigIntegerValue() {
            final BigInteger expected = new BigInteger("2974756327356");
            final String unambiguousColumn = tableName + "_some_big_integer_column";
            when(mockDBMSIntegrator.unambiguousRetrievalColumn(eq(tableName), eq("some_big_integer_column"))).thenReturn(unambiguousColumn);
            when(mockRetriever.getString(eq(unambiguousColumn))).thenReturn(expected.toString());

            BigInteger actual = getterUnderTest.parseBigIntegerColumn(mockRetriever, "some_big_integer_column");

            inOrder.verify(mockDBMSIntegrator).unambiguousRetrievalColumn(eq(tableName), eq("some_big_integer_column"));
            inOrder.verify(mockRetriever).getString(eq(unambiguousColumn));
            assertEquals(expected, actual);
        }

        @Test
        public void shouldCallRetrieverGetStringMethodWhenGettingBigDecimalValue() {
            final BigDecimal expected = new BigDecimal("2973464564.7563273567857856");
            final String unambiguousColumn = tableName + "_some_big_decimal_column";
            when(mockDBMSIntegrator.unambiguousRetrievalColumn(eq(tableName), eq("some_big_decimal_column"))).thenReturn(unambiguousColumn);
            when(mockRetriever.getString(eq(unambiguousColumn))).thenReturn(expected.toString());

            BigDecimal actual = getterUnderTest.parseBigDecimalColumn(mockRetriever, "some_big_decimal_column");

            inOrder.verify(mockDBMSIntegrator).unambiguousRetrievalColumn(eq(tableName), eq("some_big_decimal_column"));
            inOrder.verify(mockRetriever).getString(eq(unambiguousColumn));
            assertEquals(expected, actual);
        }
    }

    public static class BaseGetterErrorCaseTests extends BaseGetterTest {

        @Test(expected = IllegalArgumentException.class)
        public void shouldThrowWhenRetrieverNullAndGettingIdColumn() {
            getterUnderTest.id(null);
        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldThrowWhenRetrieverNullAndGettingCreatedColumn() {
            getterUnderTest.created(null);
        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldThrowWhenRetrieverNullAndGettingModifiedColumn() {
            getterUnderTest.modified(null);
        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldThrowWhenRetrieverNullAndGettingDeletedColumn() {
            getterUnderTest.deleted(null);
        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldThrowWhenRetrieverNullAndGettingStringColumn() {
            getterUnderTest.retrieveString(null, "a_column");
        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldThrowWhenRetrieverNullAndParsingDateColumn() {
            getterUnderTest.parseDateColumn(null, "a_column");
        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldThrowWhenRetrieverNullAndParsingBooleanColumn() {
            getterUnderTest.parseBooleanColumn(null, "a_column");
        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldThrowWhenRetrieverNullAndGettingBigIntegerColumn() {
            getterUnderTest.parseBigIntegerColumn(null, "a_column");
        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldThrowWhenRetrieverNullAndGettingBigDecimalColumn() {
            getterUnderTest.parseBigIntegerColumn(null, "a_column");
        }
    }
}
