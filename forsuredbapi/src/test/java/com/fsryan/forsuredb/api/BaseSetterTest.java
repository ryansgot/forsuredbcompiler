package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.api.adapter.SaveResultFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public abstract class BaseSetterTest<S extends BaseSetter<String, RecordContainer, S>> {

    @Mock
    protected FSQueryable<String, RecordContainer> mockQueryable;
    @Mock
    protected FSSelection mockSelection;
    @Mock
    protected List<FSOrdering> mockOrderings;
    @Mock
    protected RecordContainer mockRecordContainer;

    protected final DateFormat dateFormat;

    protected S setterUnderTest;

    public BaseSetterTest() {
        this(new SimpleDateFormat("yyyy-MM-DD hh:mm:ss.SSS"));
    }

    public BaseSetterTest(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Before
    public void setUpSetter() {
        MockitoAnnotations.initMocks(this);

        setterUnderTest = createSetterUnderTest();
    }

    protected abstract S createSetterUnderTest();

    public static class Insert extends BaseSetterTest<ConcreteSetter> {

        private InOrder inOrder;

        @Before
        public void setUpInOrderVerifications() {
            inOrder = inOrder(mockQueryable, mockRecordContainer);
            inOrder.verify(mockRecordContainer).clear();    // <-- should be cleared when Setter constructed
        }

        @After
        public void shouldClearRecordAfterInsertionAttempted() {
            inOrder.verify(mockRecordContainer).clear();
        }

        @Test
        public void shouldCallInsertWhenFSSelectionNull() {
            setterUnderTest.save();

            inOrder.verify(mockQueryable).insert(eq(mockRecordContainer));
            inOrder.verify(mockQueryable, times(0)).upsert(any(RecordContainer.class), any(FSSelection.class), any(List.class));
        }

        @Test
        public void shouldReturnZeroRowsAffectedInSaveResultWhenNullLocatorReturned() {
            when(mockQueryable.insert(eq(mockRecordContainer))).thenReturn(null);

            SaveResult<String> actual = setterUnderTest.save();

            assertNull(null, actual.inserted());
            assertEquals(0, actual.rowsAffected());
            assertNull(actual.exception());
        }

        @Test
        public void shouldReturnOneRowAffectedWhenNonNullLocatorReturned() {
            when(mockQueryable.insert(eq(mockRecordContainer))).thenReturn("Hello World!");

            SaveResult<String> actual = setterUnderTest.save();

            assertEquals("Hello World!", actual.inserted());
            assertEquals(1, actual.rowsAffected());
            assertNull(actual.exception());
        }

        @Test
        public void shouldReturnSaveResultWithExceptionWhenExceptionThrown() {
            RuntimeException expectedException = new RuntimeException();
            when(mockQueryable.insert(eq(mockRecordContainer))).thenThrow(expectedException);

            SaveResult<String> actual = setterUnderTest.save();

            assertNull(actual.inserted());
            assertEquals(0, actual.rowsAffected());
            assertEquals(expectedException, actual.exception());
        }

        @Override
        protected ConcreteSetter createSetterUnderTest() {
            return new ConcreteSetter(dateFormat, mockQueryable, null, mockOrderings, mockRecordContainer);
        }
    }

    public static class Upsert extends BaseSetterTest<ConcreteSetter> {

        @Test
        public void shouldUpsertWhenFSSelectionNotNull() {
            setterUnderTest.save();
            verify(mockQueryable).upsert(eq(mockRecordContainer), eq(mockSelection), eq(mockOrderings));
        }

        @Test
        public void shouldReturnExactlyReturnOfUpsert() {
            SaveResult<String> expected = SaveResultFactory.create("inserted", 1, null);
            when(mockQueryable.upsert(eq(mockRecordContainer), eq(mockSelection), eq(mockOrderings))).thenReturn(expected);

            SaveResult<String> actual = setterUnderTest.save();

            assertEquals(expected.inserted(), actual.inserted());
            assertEquals(expected.rowsAffected(), actual.rowsAffected());
            assertEquals(expected.exception(), actual.exception());
        }

        @Override
        protected ConcreteSetter createSetterUnderTest() {
            return new ConcreteSetter(dateFormat, mockQueryable, mockSelection, mockOrderings, mockRecordContainer);
        }
    }

    public static class SoftDelete extends BaseSetterTest<ConcreteSetter> {

        @Test
        public void shouldClearRecordContainerAndPutDeleted1PriorToUpdatingAndClearAfter() {
            InOrder inOrder = inOrder(mockQueryable, mockRecordContainer);

            setterUnderTest.softDelete();

            inOrder.verify(mockRecordContainer, times(2)).clear();  // <-- first when constructed, next before updating deleted
            inOrder.verify(mockRecordContainer).put(eq("deleted"), eq(1));
            inOrder.verify(mockQueryable).update(eq(mockRecordContainer), eq(mockSelection), eq(mockOrderings));
            inOrder.verify(mockRecordContainer).clear();
        }

        @Test
        public void shouldReturnNumRowsAffectedFromQueryable() {
            when(mockQueryable.update(eq(mockRecordContainer), eq(mockSelection), eq(mockOrderings)))
                    .thenReturn(564);
            assertEquals(564, setterUnderTest.softDelete().rowsAffected());
        }

        @Test
        public void shouldReturnNullExceptionWhenNoneThrown() {
            assertNull(setterUnderTest.softDelete().exception());
        }

        @Test
        public void shouldReturnNullInserted() {
            assertNull(setterUnderTest.softDelete().inserted());
        }

        @Test
        public void shouldReturnExceptionIfExceptionCaught() {
            RuntimeException expected = new RuntimeException();
            when(mockQueryable.update(eq(mockRecordContainer), eq(mockSelection), eq(mockOrderings)))
                    .thenThrow(expected);
            assertEquals(expected, setterUnderTest.softDelete().exception());
        }

        @Override
        protected ConcreteSetter createSetterUnderTest() {
            return new ConcreteSetter(dateFormat, mockQueryable, mockSelection, mockOrderings, mockRecordContainer);
        }
    }

    public static class HardDelete extends BaseSetterTest<ConcreteSetter> {

        @Test
        public void shouldClearRecordContainerAfterDeletion() {
            InOrder inOrder = inOrder(mockQueryable, mockRecordContainer);

            setterUnderTest.hardDelete();

            inOrder.verify(mockRecordContainer).clear();  // <-- first when constructed
            inOrder.verify(mockQueryable).delete(eq(mockSelection), eq(mockOrderings));
            inOrder.verify(mockRecordContainer).clear();
        }

        @Test
        public void shouldReturnNumRowsAffectedFromQueryable() {
            when(mockQueryable.delete(eq(mockSelection), eq(mockOrderings))).thenReturn(564);

            assertEquals(564, setterUnderTest.hardDelete());
        }

        @Test
        public void shouldReturn0IfExceptionCaught() {
            RuntimeException expected = new RuntimeException();
            when(mockQueryable.delete(eq(mockSelection), eq(mockOrderings))).thenThrow(expected);

            assertEquals(0, setterUnderTest.hardDelete());
        }

        @Override
        protected ConcreteSetter createSetterUnderTest() {
            return new ConcreteSetter(dateFormat, mockQueryable, mockSelection, mockOrderings, mockRecordContainer);
        }
    }

    public static abstract class DocStore extends BaseSetterTest<NamedDocStoreSetter> {

        protected int enrichRecordContainerFromPropertiesOfCallCount;

        @Before
        public void resetEnrichRecordContainerFromPropertiesOfCallCount() {
            enrichRecordContainerFromPropertiesOfCallCount = 0;
        }

        @Override
        protected NamedDocStoreSetter createSetterUnderTest() {
            return new NamedDocStoreSetter(dateFormat, mockQueryable, mockSelection, mockOrderings, mockRecordContainer) {
                @Override
                protected void enrichRecordContainerFromPropertiesOf(Object obj) {
                    enrichRecordContainerFromPropertiesOfCallCount++;
                }
            };
        }
    }

    public static class DocStoreObject extends DocStore {

        // currently,tests do not override FSSerializerFactory plugin
        @Test
        public void shouldSetClassNameAndDocumentOnRecordContainerAndEntrichFromPropertiesOfObject() {
            setterUnderTest.obj(1);
            verify(mockRecordContainer).put(eq("class_name"), eq(Integer.class.getName()));
            verify(mockRecordContainer).put(eq("blob_doc"), any(byte[].class));
            assertEquals(1, enrichRecordContainerFromPropertiesOfCallCount);
        }
    }

    public static class DocStorePerformPropertyEnrichment extends DocStore {

        private static final String columnName = "column_name";

        @Before
        public void performConstructionVerification() {
            verify(mockRecordContainer).clear();    // <-- should clear on Setter construction
        }

        @Test
        public void shouldPut0OnRecordContainerToEnrichFalseBooleanColumn() {
            setterUnderTest.performPropertyEnrichment(columnName,false);
            verify(mockRecordContainer).put(eq(columnName), eq(0));
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldPut1OnRecordContainerToEnrichTrueBooleanColumn() {
            setterUnderTest.performPropertyEnrichment(columnName,true);
            verify(mockRecordContainer).put(eq(columnName), eq(1));
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldPutIntOnRecordContainerToEnrichIntColumn() {
            setterUnderTest.performPropertyEnrichment(columnName,1);
            verify(mockRecordContainer).put(eq(columnName), eq(1));
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldPutLongOnRecordContainerToEnrichLongColumn() {
            setterUnderTest.performPropertyEnrichment(columnName,10L);
            verify(mockRecordContainer).put(eq(columnName), eq(10L));
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldPutFloatOnRecordContainerToEnrichFloatColumn() {
            setterUnderTest.performPropertyEnrichment(columnName,10.5F);
            verify(mockRecordContainer).put(eq(columnName), eq(10.5F));
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldPutDoubleOnRecordContainerToEnrichDoubleColumn() {
            setterUnderTest.performPropertyEnrichment(columnName,104.563823D);
            verify(mockRecordContainer).put(eq(columnName), eq(104.563823D));
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldPutStringOnRecordContainerToEnrichStringColumn() {
            setterUnderTest.performPropertyEnrichment(columnName, "hello world");
            verify(mockRecordContainer).put(eq(columnName), eq("hello world"));
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldPutByteArrayOnRecordContainerToEnrichByteArrayColumn() {
            setterUnderTest.performPropertyEnrichment(columnName, "hello world".getBytes());
            verify(mockRecordContainer).put(eq(columnName), eq("hello world".getBytes()));
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldPutStringOnRecordContainerToEnrichBigDecimalColumn() {
            setterUnderTest.performPropertyEnrichment(columnName, new BigDecimal("6348.486"));
            verify(mockRecordContainer).put(eq(columnName), eq("6348.486"));
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldPutStringOnRecordContainerToEnrichBigIntegerColumn() {
            setterUnderTest.performPropertyEnrichment(columnName, new BigInteger("63484864435645745678450357587430"));
            verify(mockRecordContainer).put(eq(columnName), eq("63484864435645745678450357587430"));
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldPutFormattedStringOnRecordContainerToEnrichDateColumn() {
            Date d = new Date();
            setterUnderTest.performPropertyEnrichment(columnName, d);
            verify(mockRecordContainer).put(eq(columnName), eq(dateFormat.format(d)));
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldThrowWhenUnsupportedObjectAttemptedToBeEnriched() {
            setterUnderTest.performPropertyEnrichment(columnName, new Object());
        }

        @Test
        public void shouldExitEarlyWhenColumnNull() {
            setterUnderTest.performPropertyEnrichment(null, new Object());
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldExitEarlyWhenColumnEmpty() {
            setterUnderTest.performPropertyEnrichment("", new Object());
            verifyNoMoreInteractions(mockRecordContainer);
        }

        @Test
        public void shouldExitEarlyWhenObjectNull() {
            setterUnderTest.performPropertyEnrichment(columnName, null);
            verifyNoMoreInteractions(mockRecordContainer);
        }
    }

    static class ConcreteSetter extends BaseSetter<String, RecordContainer, ConcreteSetter> {
        protected ConcreteSetter(DateFormat dateFormat, FSQueryable<String, RecordContainer> queryable, FSSelection selection, List<FSOrdering> orderings, RecordContainer recordContainer) {
            super(dateFormat, queryable, selection, orderings, recordContainer);
        }
    }

    static abstract class NamedDocStoreSetter extends BaseDocStoreSetter<String, RecordContainer, Object, NamedDocStoreSetter> {

        protected NamedDocStoreSetter(DateFormat dateFormat, FSQueryable<String, RecordContainer> queryable, FSSelection selection, List<FSOrdering> orderings, RecordContainer recordContainer) {
            super(dateFormat, queryable, selection, orderings, recordContainer);
        }
    }
}
