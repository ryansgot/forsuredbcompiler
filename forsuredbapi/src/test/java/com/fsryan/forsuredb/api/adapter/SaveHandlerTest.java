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
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
            SaveHandler<String, RecordContainer> shut = SaveHandler.getFor(
                    fsSaveApiClass,
                    mockFSQueryable,
                    mockFSSelection,
                    Collections.<FSOrdering>emptyList(),
                    mockRecordContainer,
                    columnTypeMap(fsSaveApiClass)
            );
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
            shut = SaveHandler.getFor(
                    apiClass,
                    mockFSQueryable,
                    mockFSSelection,
                    Collections.<FSOrdering>emptyList(),
                    mockRecordContainer,
                    columnTypeMap(apiClass)
            );
        }

        @Test
        public void shouldCallPutStringRecordContainerMethod() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("stringColumn", String.class), new Object[]{"string_value"});
            verify(mockRecordContainer).put("string_column", "string_value");
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldCallPutIntRecordContainerMethod() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("intColumn", int.class), new Object[]{1});
            verify(mockRecordContainer).put("int_column", 1);
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldCallPutIntegerWrapperRecordContainerMethod() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("integerWrapperColumn", Integer.class), new Object[]{Integer.valueOf(1)});
            verify(mockRecordContainer).put("integer_wrapper_column", 1);
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldCallPutLongRecordContainerMethod() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("longColumn", long.class), new Object[]{Long.MAX_VALUE});
            verify(mockRecordContainer).put("long_column", Long.MAX_VALUE);
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldCallPutLongWrapperRecordContainerMethod() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("longWrapperColumn", Long.class), new Object[]{Long.valueOf(1)});
            verify(mockRecordContainer).put("long_wrapper_column", Long.valueOf(1));
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldCallPutFloatRecordContainerMethod() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("floatColumn", float.class), new Object[]{Float.MAX_VALUE});
            verify(mockRecordContainer).put("float_column", Float.MAX_VALUE);
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldCallPutFloatWrapperRecordContainerMethod() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("floatWrapperColumn", Float.class), new Object[]{Float.valueOf(1)});
            verify(mockRecordContainer).put("float_wrapper_column", Float.valueOf(1));
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldCallPutDoubleRecordContainerMethod() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("doubleColumn", double.class), new Object[]{Double.MAX_VALUE});
            verify(mockRecordContainer).put("double_column", Double.MAX_VALUE);
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldCallPutDoubleWrapperRecordContainerMethod() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("doubleWrapperColumn", Double.class), new Object[]{Double.valueOf(1)});
            verify(mockRecordContainer).put("double_wrapper_column", Double.valueOf(1));
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldCallPutBooleanRecordContainerMethodTrue() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("booleanColumn", boolean.class), new Object[]{true});
            verify(mockRecordContainer).put("boolean_column", 1);
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldCallPutBooleanRecordContainerMethodFalse() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("booleanColumn", boolean.class), new Object[]{false});
            verify(mockRecordContainer).put("boolean_column", 0);
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldCallPutBooleanWrapperRecordContainerMethodTrue() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("booleanWrapperColumn", Boolean.class), new Object[]{Boolean.valueOf(true)});
            verify(mockRecordContainer).put("boolean_wrapper_column", 1);
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldCallPutBooleanWrapperRecordContainerMethodFalse() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("booleanWrapperColumn", Boolean.class), new Object[]{Boolean.valueOf(false)});
            verify(mockRecordContainer).put("boolean_wrapper_column", 0);
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldCallPutStringRecordContainerMethodOnBigDecimal() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("bigDecimalColumn", BigDecimal.class), new Object[]{BigDecimal.TEN});
            verify(mockRecordContainer).put("big_decimal_column", BigDecimal.TEN.toString());
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldCallPutStringRecordContainerMethodOnBigInteger() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("bigIntegerColumn", BigInteger.class), new Object[]{BigInteger.TEN});
            verify(mockRecordContainer).put("big_integer_column", BigInteger.TEN.toString());
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldCallPutStringRecordContainerMethodOnDate() throws Throwable {
            shut.invoke(shut, apiClass.getMethod("dateColumn", Date.class), new Object[]{new Date()});
            verify(mockRecordContainer).put(eq("date_column"), any(String.class));    // <-- not validating format
            verifyNoMoreInteractions(mockRecordContainer);
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
                verifyNoMoreInteractions(mockRecordContainer);
            }
        }

        @RunWith(Parameterized.class)
        public static class DocStore extends Set {

            private static final DocStoreTestBase dstb = DocStoreTestBase.builder()
                    .bigIntegerColumn(BigInteger.ONE)
                    .bigDecimalColumn(BigDecimal.ONE)
                    .booleanColumn(true)
                    .booleanWrapperColumn(false)
                    .dateColumn(DATE)
                    .floatColumn(Float.MAX_VALUE)
                    .floatWrapperColumn(Float.MIN_VALUE)
                    .doubleColumn(Double.MAX_VALUE)
                    .doubleWrapperColumn(Double.MIN_VALUE)
                    .intColumn(Integer.MAX_VALUE)
                    .integerWrapperColumn(Integer.MIN_VALUE)
                    .longColumn(Long.MAX_VALUE)
                    .longWrapperColumn(Long.MIN_VALUE)
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
                // verifies that, for each index, the correct value gets put into the record container
                verify(mockRecordContainer).put("big_integer_column", BigInteger.ONE.toString());
                verify(mockRecordContainer).put("big_decimal_column", BigDecimal.ONE.toString());
                verify(mockRecordContainer).put("boolean_column", 1);
                verify(mockRecordContainer).put("boolean_wrapper_column", 0);
                verify(mockRecordContainer).put("date_column", DATE_STRING);
                verify(mockRecordContainer).put("float_column", Float.MAX_VALUE);
                verify(mockRecordContainer).put("float_wrapper_column", Float.valueOf(Float.MIN_VALUE));
                verify(mockRecordContainer).put("double_column", Double.MAX_VALUE);
                verify(mockRecordContainer).put("double_wrapper_column", Double.valueOf(Double.MIN_VALUE));
                verify(mockRecordContainer).put("int_column", Integer.MAX_VALUE);
                verify(mockRecordContainer).put("integer_wrapper_column", Integer.valueOf(Integer.MIN_VALUE));
                verify(mockRecordContainer).put("long_column", Long.MAX_VALUE);
                verify(mockRecordContainer).put("long_wrapper_column", Long.valueOf(Long.MIN_VALUE));
                verify(mockRecordContainer).put("string_column", "a string");
                // verifies that the doc and class name got put into the record container
                verify(mockRecordContainer).put("class_name", dstObject.getClass().getName());
                verify(mockRecordContainer).put("doc", new Gson().toJson(dstObject));
            }
        }
    }

    public static class Upsertion extends SaveHandlerTest<String> {

        private SaveHandler<String, RecordContainer> shut;

        @Before
        public void setUp() {
            super.setUp();
            shut = SaveHandler.getFor(
                    FSGetApiExtensionTestTableSetter.class,
                    mockFSQueryable,
                    mockFSSelection,
                    Collections.<FSOrdering>emptyList(),
                    mockRecordContainer,
                    columnTypeMap(FSGetApiExtensionTestTableSetter.class)
            );
        }

        @Test
        public void shouldCallInsertWhenNoSearchCriteriaUsed() throws Throwable {
            // overwrite so that the FSSelection object is null
            shut = SaveHandler.getFor(
                    FSGetApiExtensionTestTableSetter.class,
                    mockFSQueryable,
                    null,
                    Collections.<FSOrdering>emptyList(),
                    mockRecordContainer,
                    columnTypeMap(FSGetApiExtensionTestTableSetter.class)
            );
            shut.invoke(shut, FSSaveApi.class.getMethod("save"), new Class<?>[0]);
            verify(mockFSQueryable).insert(mockRecordContainer);
            verify(mockFSQueryable, times(0)).update(any(RecordContainer.class), any(FSSelection.class), any(List.class));
        }

        @Test
        public void shouldCallInsertWhenNoRecordsMatchingFSSelectionExist() throws Throwable {
            when(mockFSQueryable.query(eq((FSProjection) null), any(FSSelection.class), any(List.class))).thenReturn(null);
            shut.invoke(shut, FSSaveApi.class.getMethod("save"), new Class<?>[0]);
            verify(mockFSQueryable, times(1)).insert(mockRecordContainer);
            verify(mockFSQueryable, times(0)).update(any(RecordContainer.class), any(FSSelection.class), any(List.class));
        }

        @Test
        public void shouldCallUpdateWithSelectionCriteriaWhenRecordsMatchingFSSelectionExist() throws Throwable {
            Retriever mockRetriever = mock(Retriever.class);
            when(mockRetriever.getCount()).thenReturn(1);
            when(mockFSQueryable.query(null, mockFSSelection, null)).thenReturn(mockRetriever);
            shut.invoke(shut, FSSaveApi.class.getMethod("save"), new Class<?>[0]);
            verify(mockFSQueryable, times(0)).insert(mockRecordContainer);
            verify(mockFSQueryable).update(eq(mockRecordContainer), eq(mockFSSelection), any(List.class));
        }
    }

    public static class Deletion extends SaveHandlerTest<String> {

        private SaveHandler<String, RecordContainer> shut;

        @Before
        public void setUp() {
            super.setUp();
            shut = SaveHandler.getFor(
                    FSGetApiExtensionTestTableSetter.class,
                    mockFSQueryable,
                    mockFSSelection,
                    Collections.<FSOrdering>emptyList(),
                    mockRecordContainer,
                    columnTypeMap(FSGetApiExtensionTestTableSetter.class)
            );
        }

        @Test
        public void shouldUpdateOnlyDeletedFieldWhenSoftDeleteCalled() throws Throwable {
            // overwrite so that the FSSelection object is null
            shut.invoke(shut, FSSaveApi.class.getMethod("softDelete"), new Class<?>[0]);
            verify(mockRecordContainer).clear();
            verify(mockRecordContainer).put("deleted", 1);
            verify(mockFSQueryable, times(0)).insert(mockRecordContainer);
            verify(mockFSQueryable).update(eq(mockRecordContainer), eq(mockFSSelection), any(List.class));
        }

        @Test
        public void shouldCallDeleteOnSelectionWhenDeleteCalled() throws Throwable {
            // overwrite so that the FSSelection object is null
            shut.invoke(shut, FSSaveApi.class.getMethod("hardDelete"), new Class<?>[0]);
            verify(mockRecordContainer).clear();
            verify(mockFSQueryable, times(0)).insert(mockRecordContainer);
            verify(mockFSQueryable, times(0)).update(any(RecordContainer.class), any(FSSelection.class), any(List.class));
            verify(mockFSQueryable).delete(eq(mockFSSelection), any(List.class));
        }
    }
}
