package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.api.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

import static com.fsryan.forsuredb.api.adapter.SharedData.COLUMN_NAME_TO_METHOD_NAME_BI_MAP;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public abstract class RetrieveHandlerTest<U> {

    @Mock protected FSQueryable<U, RecordContainer> mockFSQueryable;
    @Mock protected FSSelection mockFSSelection;
    @Mock protected RecordContainer mockRecordContainer;
    @Mock protected Retriever mockRetriever;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @RunWith(Parameterized.class)
    public static class RetrieveHandlerTypeSelection extends RetrieveHandlerTest<String> {

        private final Class<? extends FSGetApi> fsGetApiClass;
        private final String tableName;
        private final Class<? extends RetrieveHandler> expectedRetrieveHandlerClass;

        public RetrieveHandlerTypeSelection(Class<? extends FSGetApi> fsGetApiClass, String tableName, Class<? extends RetrieveHandler> expectedRetrieveHandlerClass) {
            this.fsGetApiClass = fsGetApiClass;
            this.tableName = tableName;
            this.expectedRetrieveHandlerClass = expectedRetrieveHandlerClass;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {FSGetApiExtensionTestTable.class, "forsuredb_test_table", RelationalRetrieveHandler.class},
                    {FSDocStoreGetApiExtensionTestTable.class, "forsuredb_doc_store_test", DocStoreRetrieveHandler.class}
            });
        }

        @Test
        public void shouldSelectCorrectSubclass() {
            RetrieveHandler rhut = RetrieveHandler.getFor(fsGetApiClass, tableName, COLUMN_NAME_TO_METHOD_NAME_BI_MAP.inverse(), true);
            assertEquals(expectedRetrieveHandlerClass, rhut.getClass());
        }
    }

    public static abstract class Get extends RetrieveHandlerTest<String> {

        protected RetrieveHandler rhut;
        protected final Class<? extends FSGetApi> apiClass;
        protected final String tableName;

        public Get(Class<? extends FSGetApi> apiClass, String tableName) {
            this.apiClass = apiClass;
            this.tableName = tableName;
        }

        @Override
        public void setUp() {
            super.setUp();
            rhut = RetrieveHandler.getFor(apiClass, tableName, COLUMN_NAME_TO_METHOD_NAME_BI_MAP.inverse(), true);
        }

        @Test
        public void shouldCallRetrieverGetStringMethodWhenInvokingMethodReturningString() throws Throwable {
            rhut.invoke(rhut, apiClass.getMethod("stringColumn", Retriever.class), new Object[]{mockRetriever});
            verify(mockRetriever, times(1)).getString(tableName + "_string_column");
        }

        @Test
        public void shouldCallRetrieverGetIntMethodWhenInvokingMethodReturningInt() throws Throwable {
            rhut.invoke(rhut, apiClass.getMethod("intColumn", Retriever.class), new Object[]{mockRetriever});
            verify(mockRetriever, times(1)).getInt(tableName + "_int_column");
        }

        @Test
        public void shouldCallRetrieverGetIntMethodWhenInvokingMethodReturningIntegerWrapper() throws Throwable {
            rhut.invoke(rhut, apiClass.getMethod("integerWrapperColumn", Retriever.class), new Object[]{mockRetriever});
            verify(mockRetriever, times(1)).getInt(tableName + "_integer_wrapper_column");
        }

        @Test
        public void shouldCallRetrieverGetLongMethodWhenInvokingMethodReturningLong() throws Throwable {
            rhut.invoke(rhut, apiClass.getMethod("longColumn", Retriever.class), new Object[]{mockRetriever});
            verify(mockRetriever, times(1)).getLong(tableName + "_long_column");
        }

        @Test
        public void shouldCallRetrieverGetLongMethodWhenInvokingMethodReturningLongWrapper() throws Throwable {
            rhut.invoke(rhut, apiClass.getMethod("longWrapperColumn", Retriever.class), new Object[]{mockRetriever});
            verify(mockRetriever, times(1)).getLong(tableName + "_long_wrapper_column");
        }

        @Test
        public void shouldCallRetrieverGetDoubleMethodWhenInvokingMethodReturningDouble() throws Throwable {
            rhut.invoke(rhut, apiClass.getMethod("doubleColumn", Retriever.class), new Object[]{mockRetriever});
            verify(mockRetriever, times(1)).getDouble(tableName + "_double_column");
        }

        @Test
        public void shouldCallRetrieverGetDoubleMethodWhenInvokingMethodReturningDoubleWrapper() throws Throwable {
            rhut.invoke(rhut, apiClass.getMethod("doubleWrapperColumn", Retriever.class), new Object[]{mockRetriever});
            verify(mockRetriever, times(1)).getDouble(tableName + "_double_wrapper_column");
        }

        @Test
        public void shouldCallRetrieverGetIntMethodWhenInvokingMethodReturningBoolean() throws Throwable {
            rhut.invoke(rhut, apiClass.getMethod("booleanColumn", Retriever.class), new Object[]{mockRetriever});
            verify(mockRetriever, times(1)).getInt(tableName + "_boolean_column");
        }

        @Test
        public void shouldCallRetrieverGetBooleanMethodWhenInvokingMethodReturningBooleanWrapper() throws Throwable {
            rhut.invoke(rhut, apiClass.getMethod("booleanWrapperColumn", Retriever.class), new Object[]{mockRetriever});
            verify(mockRetriever, times(1)).getInt(tableName + "_boolean_wrapper_column");
        }

        @Test
        public void shouldCallPutStringRecordContainerMethodOnBigDecimal() throws Throwable {
            rhut.invoke(rhut, apiClass.getMethod("bigDecimalColumn", Retriever.class), new Object[]{mockRetriever});
            verify(mockRetriever, times(1)).getString(tableName + "_big_decimal_column");
        }

        @Test
        public void shouldCallPutStringRecordContainerMethodOnDate() throws Throwable {
            rhut.invoke(rhut, apiClass.getMethod("dateColumn", Retriever.class), new Object[]{mockRetriever});
            verify(mockRetriever, times(1)).getString(tableName + "_date_column");
        }

        public static class Relational extends Get {
            public Relational() {
                super(FSGetApiExtensionTestTable.class, "forsuredb_test_table");
            }
        }

        public static class DocStore extends Get {
            public DocStore() {
                super(FSDocStoreGetApiExtensionTestTable.class, "forsuredb_doc_store_test");
            }
        }
    }
}
