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

import static com.fsryan.forsuredb.api.adapter.SharedData.DATE;
import static com.fsryan.forsuredb.api.adapter.SharedData.DATE_STRING;
import static com.fsryan.forsuredb.api.adapter.SharedData.columnTypeMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

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
            verifyNoOtherPutsCalled("string_column");
        }

        @Test
        public void shouldCallPutIntRecordContainerMethod() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("intColumn", int.class), new Object[]{1});
            verify(mockRecordContainer, times(1)).put("int_column", 1);
            verifyNoOtherPutsCalled("int_column");
        }

        @Test
        public void shouldCallPutIntegerWrapperRecordContainerMethod() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("integerWrapperColumn", Integer.class), new Object[]{Integer.valueOf(1)});
            verify(mockRecordContainer, times(1)).put("integer_wrapper_column", 1);
            verifyNoOtherPutsCalled("integer_wrapper_column");
        }

        @Test
        public void shouldCallPutLongRecordContainerMethod() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("longColumn", long.class), new Object[]{Long.MAX_VALUE});
            verify(mockRecordContainer, times(1)).put("long_column", Long.MAX_VALUE);
            verifyNoOtherPutsCalled("long_column");
        }

        @Test
        public void shouldCallPutLongWrapperRecordContainerMethod() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("longWrapperColumn", Long.class), new Object[]{Long.valueOf(1)});
            verify(mockRecordContainer, times(1)).put("long_wrapper_column", Long.valueOf(1));
            verifyNoOtherPutsCalled("long_wrapper_column");
        }

        @Test
        public void shouldCallPutDoubleRecordContainerMethod() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("doubleColumn", double.class), new Object[]{Double.MAX_VALUE});
            verify(mockRecordContainer, times(1)).put("double_column", Double.MAX_VALUE);
            verifyNoOtherPutsCalled("double_column");
        }

        @Test
        public void shouldCallPutDoubleWrapperRecordContainerMethod() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("doubleWrapperColumn", Double.class), new Object[]{Double.valueOf(1)});
            verify(mockRecordContainer, times(1)).put("double_wrapper_column", Double.valueOf(1));
            verifyNoOtherPutsCalled("double_wrapper_column");
        }

        @Test
        public void shouldCallPutBooleanRecordContainerMethodTrue() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("booleanColumn", boolean.class), new Object[]{true});
            verify(mockRecordContainer, times(1)).put("boolean_column", 1);
            verifyNoOtherPutsCalled("boolean_column");
        }

        @Test
        public void shouldCallPutBooleanRecordContainerMethodFalse() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("booleanColumn", boolean.class), new Object[]{false});
            verify(mockRecordContainer, times(1)).put("boolean_column", 0);
            verifyNoOtherPutsCalled("boolean_column");
        }

        @Test
        public void shouldCallPutBooleanWrapperRecordContainerMethodTrue() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("booleanWrapperColumn", Boolean.class), new Object[]{Boolean.valueOf(true)});
            verify(mockRecordContainer, times(1)).put("boolean_wrapper_column", 1);
            verifyNoOtherPutsCalled("boolean_wrapper_column");
        }

        @Test
        public void shouldCallPutBooleanWrapperRecordContainerMethodFalse() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("booleanWrapperColumn", Boolean.class), new Object[]{Boolean.valueOf(false)});
            verify(mockRecordContainer, times(1)).put("boolean_wrapper_column", 0);
            verifyNoOtherPutsCalled("boolean_wrapper_column");
        }

        @Test
        public void shouldCallPutStringRecordContainerMethodOnBigDecimal() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("bigDecimalColumn", BigDecimal.class), new Object[]{BigDecimal.TEN});
            verify(mockRecordContainer, times(1)).put("big_decimal_column", BigDecimal.TEN.toString());
            verifyNoOtherPutsCalled("big_decimal_column");
        }

        @Test
        public void shouldCallPutStringRecordContainerMethodOnDate() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("dateColumn", Date.class), new Object[]{new Date()});
            verify(mockRecordContainer, times(1)).put(eq("date_column"), any(String.class));    // <-- not validating format
            verifyNoOtherPutsCalled("date_column");
        }

        protected void verifyNoOtherPutsCalled(String columnName) {
            if (!"big_decimal_column".equals(columnName)) {
                verify(mockRecordContainer, times(0)).put(eq("big_decimal_column"), any(String.class));
            }
            if (!"boolean_column".equals(columnName)) {
                verify(mockRecordContainer, times(0)).put(eq("boolean_column"), any(int.class));
            }
            if (!"boolean_wrapper_column".equals(columnName)) {
                verify(mockRecordContainer, times(0)).put(eq("boolean_wrapper_column"), any(int.class));
            }
            if (!"byte_array_column".equals(columnName)) {
                verify(mockRecordContainer, times(0)).put(eq("byte_array_column"), any(byte[].class));
            }
            if (!"date_column".equals(columnName)) {
                verify(mockRecordContainer, times(0)).put(eq("date_column"), any(String.class));
            }
            if (!"double_column".equals(columnName)) {
                verify(mockRecordContainer, times(0)).put(eq("double_column"), any(double.class));
            }
            if (!"double_wrapper_column".equals(columnName)) {
                verify(mockRecordContainer, times(0)).put(eq("double_wrapper_column"), any(Double.class));
            }
            if (!"int_column".equals(columnName)) {
                verify(mockRecordContainer, times(0)).put(eq("int_column"), any(int.class));
            }
            if (!"integer_wrapper_column".equals(columnName)) {
                verify(mockRecordContainer, times(0)).put(eq("integer_wrapper_column"), any(Integer.class));
            }
            if (!"long_column".equals(columnName)) {
                verify(mockRecordContainer, times(0)).put(eq("long_column"), any(long.class));
            }
            if (!"long_wrapper_column".equals(columnName)) {
                verify(mockRecordContainer, times(0)).put(eq("long_wrapper_column"), any(Long.class));
            }
            if (!"string_column".equals(columnName)) {
                verify(mockRecordContainer, times(0)).put(eq("string_column"), any(String.class));
            }
            if (!"doc".equals(columnName)) {
                verify(mockRecordContainer, times(0)).put(eq("doc"), any(String.class));
            }
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
                verifyNoOtherPutsCalled("byte_array_column");
            }
        }

        @RunWith(Parameterized.class)
        public static class DocStore extends Set {

            private static final DocStoreTestBase dstb = DocStoreTestBase.builder()
                    .bigDecimalColumn(BigDecimal.ONE)
                    .booleanColumn(true)
                    .booleanWrapperColumn(Boolean.valueOf(false))
                    .dateColumn(DATE)
                    .doubleColumn(Double.MAX_VALUE)
                    .doubleWrapperColumn(Double.valueOf(Double.MIN_VALUE))
                    .intColumn(Integer.MAX_VALUE)
                    .integerWrapperColumn(Integer.valueOf(Integer.MIN_VALUE))
                    .longColumn(Long.MAX_VALUE)
                    .longWrapperColumn(Long.valueOf(Long.MIN_VALUE))
                    .stringColumn("a string")
                    .build();

            private final DocStoreTestBase dstObject;

            public DocStore(DocStoreTestBase dstObject) {
                super(FSDocStoreGetApiExtensionTestTableSetter.class);
                this.dstObject = dstObject;
            }

            @Parameterized.Parameters
            public static Iterable<Object[]> data() {
                return Arrays.asList(new Object[][] {
                        {dstb},
                        {new DocStoreTestBase.Extension(dstb, "another string")}
                });
            }

            @Test
            public void shouldUpdateAllIndicesSaveDocAndSaveClassName() throws Throwable {
                shut.invoke(shut, apiClass.getInterfaces()[0].getDeclaredMethod("object", Object.class), new Object[]{dstObject});
                verify(mockRecordContainer, times(1)).put("big_decimal_column", BigDecimal.ONE.toString());
                verify(mockRecordContainer, times(1)).put("boolean_column", 1);
                verify(mockRecordContainer, times(1)).put("boolean_wrapper_column", 0);
                verify(mockRecordContainer, times(1)).put("date_column", DATE_STRING);
                verify(mockRecordContainer, times(1)).put("double_column", Double.MAX_VALUE);
                verify(mockRecordContainer, times(1)).put("double_wrapper_column", Double.valueOf(Double.MIN_VALUE));
                verify(mockRecordContainer, times(1)).put("int_column", Integer.MAX_VALUE);
                verify(mockRecordContainer, times(1)).put("integer_wrapper_column", Integer.valueOf(Integer.MIN_VALUE));
                verify(mockRecordContainer, times(1)).put("long_column", Long.MAX_VALUE);
                verify(mockRecordContainer, times(1)).put("long_wrapper_column", Long.valueOf(Long.MIN_VALUE));
                verify(mockRecordContainer, times(1)).put("string_column", "a string");
                verify(mockRecordContainer, times(1)).put("class_name", dstObject.getClass().getName());
                verify(mockRecordContainer, times(1)).put("doc", new Gson().toJson(dstObject));
            }
        }
    }

    public static class Upsertion extends SaveHandlerTest<String> {

        private SaveHandler<String, RecordContainer> shut;

        @Before
        public void setUp() {
            super.setUp();
            shut = SaveHandler.getFor(FSGetApiExtensionTestTableSetter.class, mockFSQueryable, mockFSSelection, mockRecordContainer, columnTypeMap(FSGetApiExtensionTestTableSetter.class));
        }

        @Test
        public void shouldCallInsertWhenNoSearchCriteriaUsed() throws Throwable {
            // overwrite so that the FSSelection object is null
            shut = SaveHandler.getFor(FSGetApiExtensionTestTableSetter.class, mockFSQueryable, null, mockRecordContainer, columnTypeMap(FSGetApiExtensionTestTableSetter.class));
            shut.invoke(shut, FSSaveApi.class.getMethod("save"), new Class<?>[0]);
            verify(mockFSQueryable, times(1)).insert(mockRecordContainer);
            verify(mockFSQueryable, times(0)).update(any(RecordContainer.class), any(FSSelection.class));
        }

        @Test
        public void shouldCallInsertWhenNoRecordsMatchingFSSelectionExist() throws Throwable {
            when(mockFSQueryable.query(eq((FSProjection) null), any(FSSelection.class), eq((String) null))).thenReturn(null);
            shut.invoke(shut, FSSaveApi.class.getMethod("save"), new Class<?>[0]);
            verify(mockFSQueryable, times(1)).insert(mockRecordContainer);
            verify(mockFSQueryable, times(0)).update(any(RecordContainer.class), any(FSSelection.class));
        }

        @Test
        public void shouldCallUpdateWithSelectionCriteriaWhenRecordsMatchingFSSelectionExist() throws Throwable {
            Retriever mockRetriever = mock(Retriever.class);
            when(mockRetriever.getCount()).thenReturn(1);
            when(mockFSQueryable.query(null, mockFSSelection, null)).thenReturn(mockRetriever);
            shut.invoke(shut, FSSaveApi.class.getMethod("save"), new Class<?>[0]);
            verify(mockFSQueryable, times(0)).insert(mockRecordContainer);
            verify(mockFSQueryable, times(1)).update(mockRecordContainer, mockFSSelection);
        }
    }

    public static class Deletion extends SaveHandlerTest<String> {

        private SaveHandler<String, RecordContainer> shut;

        @Before
        public void setUp() {
            super.setUp();
            shut = SaveHandler.getFor(FSGetApiExtensionTestTableSetter.class, mockFSQueryable, mockFSSelection, mockRecordContainer, columnTypeMap(FSGetApiExtensionTestTableSetter.class));
        }

        @Test
        public void shouldUpdateOnlyDeletedFieldWhenSoftDeleteCalled() throws Throwable {
            // overwrite so that the FSSelection object is null
            shut.invoke(shut, FSSaveApi.class.getMethod("softDelete"), new Class<?>[0]);
            verify(mockRecordContainer, times(1)).clear();
            verify(mockRecordContainer, times(1)).put("deleted", 1);
            verify(mockFSQueryable, times(0)).insert(mockRecordContainer);
            verify(mockFSQueryable, times(1)).update(mockRecordContainer, mockFSSelection);
        }

        @Test
        public void shouldCallDeleteOnSelectionWhenDeleteCalled() throws Throwable {
            // overwrite so that the FSSelection object is null
            shut.invoke(shut, FSSaveApi.class.getMethod("hardDelete"), new Class<?>[0]);
            verify(mockRecordContainer, times(1)).clear();
            verify(mockFSQueryable, times(0)).insert(mockRecordContainer);
            verify(mockFSQueryable, times(0)).update(any(RecordContainer.class), any(FSSelection.class));
            verify(mockFSQueryable, times(1)).delete(mockFSSelection);
        }
    }
}
