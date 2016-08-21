package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.api.*;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

import static com.fsryan.forsuredb.api.adapter.SharedData.columnTypeMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public abstract class SaveHandlerTest<U> {

    @Mock protected FSQueryable<U, RecordContainer> mockFSQueryable;
    @Mock protected FSSelection mockFSSelection;
    @Mock protected RecordContainer mockRecordContainer;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @RunWith(Parameterized.class)
    public static class SaveHandlerTypeSelection extends SaveHandlerTest<String> {

        private final Class<? extends FSSaveApi<String>> fsSaveApiClass;
        private final Class<? extends SaveHandler<String, RecordContainer>> expectedSaveHandlerClass;

        public SaveHandlerTypeSelection(Class<? extends FSSaveApi<String>> fsSaveApiClass, Class<? extends SaveHandler<String, RecordContainer>> expectedSaveHandlerClass) {
            this.fsSaveApiClass = fsSaveApiClass;
            this.expectedSaveHandlerClass = expectedSaveHandlerClass;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {FSGetApiExtensionTestTableSetter.class, RelationalSaveHandler.class},
                    {FSDocStoreGetApiExtensionTestTableSetter.class, DocStoreSaveHandler.class}
            });
        }

        @Test
        public void shouldSelectCorrectSubclass() {
            SaveHandler<String, RecordContainer> shut = SaveHandler.getFor(fsSaveApiClass, mockFSQueryable, mockFSSelection, mockRecordContainer, columnTypeMap(fsSaveApiClass));
            assertEquals(expectedSaveHandlerClass, shut.getClass());
        }
    }

    public static abstract class Set extends SaveHandlerTest<String> {

        protected SaveHandler<String, RecordContainer> shut;
        protected final Class<? extends FSSaveApi<String>> apiClass;

        public Set(Class<? extends FSSaveApi<String>> apiClass) {
            this.apiClass = apiClass;
        }

        @Override
        public void setUp() {
            super.setUp();
            shut = SaveHandler.getFor(apiClass, mockFSQueryable, mockFSSelection, mockRecordContainer, columnTypeMap(apiClass));
        }

        @Test
        public void shouldCallPutStringRecordContainerMethod() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("stringColumn", String.class), new Object[]{"string_value"});
            verify(mockRecordContainer, times(1)).put("string_column", "string_value");
        }

        @Test
        public void shouldCallPutIntRecordContainerMethod() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("intColumn", int.class), new Object[]{1});
            verify(mockRecordContainer, times(1)).put("int_column", 1);
        }

        @Test
        public void shouldCallPutIntegerWrapperRecordContainerMethod() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("integerWrapperColumn", Integer.class), new Object[]{Integer.valueOf(1)});
            verify(mockRecordContainer, times(1)).put("integer_wrapper_column", 1);
        }

        @Test
        public void shouldCallPutLongRecordContainerMethod() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("longColumn", long.class), new Object[]{Long.MAX_VALUE});
            verify(mockRecordContainer, times(1)).put("long_column", Long.MAX_VALUE);
        }

        @Test
        public void shouldCallPutLongWrapperRecordContainerMethod() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("longWrapperColumn", Long.class), new Object[]{Long.valueOf(1)});
            verify(mockRecordContainer, times(1)).put("long_wrapper_column", Long.valueOf(1));
        }

        @Test
        public void shouldCallPutDoubleRecordContainerMethod() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("doubleColumn", double.class), new Object[]{Double.MAX_VALUE});
            verify(mockRecordContainer, times(1)).put("double_column", Double.MAX_VALUE);
        }

        @Test
        public void shouldCallPutDoubleWrapperRecordContainerMethod() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("doubleWrapperColumn", Double.class), new Object[]{Double.valueOf(1)});
            verify(mockRecordContainer, times(1)).put("double_wrapper_column", Double.valueOf(1));
        }

        @Test
        public void shouldCallPutBooleanRecordContainerMethodTrue() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("booleanColumn", boolean.class), new Object[]{true});
            verify(mockRecordContainer, times(1)).put("boolean_column", 1);
        }

        @Test
        public void shouldCallPutBooleanRecordContainerMethodFalse() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("booleanColumn", boolean.class), new Object[]{false});
            verify(mockRecordContainer, times(1)).put("boolean_column", 0);
        }

        @Test
        public void shouldCallPutBooleanWrapperRecordContainerMethodTrue() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("booleanWrapperColumn", Boolean.class), new Object[]{Boolean.valueOf(true)});
            verify(mockRecordContainer, times(1)).put("boolean_wrapper_column", 1);
        }

        @Test
        public void shouldCallPutBooleanWrapperRecordContainerMethodFalse() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("booleanWrapperColumn", Boolean.class), new Object[]{Boolean.valueOf(false)});
            verify(mockRecordContainer, times(1)).put("boolean_wrapper_column", 0);
        }

        @Test
        public void shouldCallPutStringRecordContainerMethodOnBigDecimal() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("bigDecimalColumn", BigDecimal.class), new Object[]{BigDecimal.TEN});
            verify(mockRecordContainer, times(1)).put("big_decimal_column", BigDecimal.TEN.toString());
        }

        @Test
        public void shouldCallPutStringRecordContainerMethodOnDate() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("dateColumn", Date.class), new Object[]{new Date()});
            verify(mockRecordContainer, times(1)).put(eq("date_column"), any(String.class));
        }

        public static class Relational extends Set {
            public Relational() {
                super(FSGetApiExtensionTestTableSetter.class);
            }

            @Test
            public void shouldCallPutByteArrayRecordContainerMethodOnByteArray() throws Throwable {
                byte[] arr = new byte[] {1, 2, 3};
                shut.invoke(shut, apiClass.getMethod("byteArrayColumn", byte[].class), new Object[]{arr});
                verify(mockRecordContainer, times(1)).put("byte_array_column", arr);
            }
        }

        public static class DocStore extends Set {
            public DocStore() {
                super(FSDocStoreGetApiExtensionTestTableSetter.class);
            }

            @Test
            public void shouldCallPutForAllDeclaredMethodsInApi() throws Throwable {
                DocStoreTestBase obj = DocStoreTestBase.builder()
                        .bigDecimalColumn(BigDecimal.ONE)
                        .booleanColumn(true)
                        .booleanWrapperColumn(Boolean.valueOf(false))
                        .dateColumn(new Date())
                        .doubleColumn(Double.MAX_VALUE)
                        .doubleWrapperColumn(Double.valueOf(Double.MIN_VALUE))
                        .intColumn(Integer.MAX_VALUE)
                        .integerWrapperColumn(Integer.valueOf(Integer.MIN_VALUE))
                        .longColumn(Long.MAX_VALUE)
                        .longWrapperColumn(Long.valueOf(Long.MIN_VALUE))
                        .stringColumn("a string")
                        .build();
                shut.invoke(shut, apiClass.getInterfaces()[0].getDeclaredMethod("object", Object.class), new Object[]{obj});
                verify(mockRecordContainer, times(1)).put("big_decimal_column", BigDecimal.ONE.toString());
                verify(mockRecordContainer, times(1)).put("boolean_column", 1);
                verify(mockRecordContainer, times(1)).put("boolean_wrapper_column", 0);
                verify(mockRecordContainer, times(1)).put(eq("date_column"), any(String.class));
                verify(mockRecordContainer, times(1)).put("double_column", Double.MAX_VALUE);
                verify(mockRecordContainer, times(1)).put("double_wrapper_column", Double.valueOf(Double.MIN_VALUE));
                verify(mockRecordContainer, times(1)).put("int_column", Integer.MAX_VALUE);
                verify(mockRecordContainer, times(1)).put("integer_wrapper_column", Integer.valueOf(Integer.MIN_VALUE));
                verify(mockRecordContainer, times(1)).put("long_column", Long.MAX_VALUE);
                verify(mockRecordContainer, times(1)).put("long_wrapper_column", Long.valueOf(Long.MIN_VALUE));
                verify(mockRecordContainer, times(1)).put("string_column", "a string");
                verify(mockRecordContainer, times(1)).put("doc", new Gson().toJson(obj));
            }
        }
    }
}
