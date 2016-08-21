package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.api.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static com.fsryan.forsuredb.api.adapter.SharedData.COLUMN_NAME_TO_METHOD_NAME_BI_MAP;
import static org.junit.Assert.assertEquals;

public class RetrieveHandlerTest<U> {

    @Mock protected FSQueryable<U, RecordContainer> mockFSQueryable;
    @Mock protected FSSelection mockFSSelection;
    @Mock protected RecordContainer mockRecordContainer;

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
            RetrieveHandler rhut = RetrieveHandler.getFor(fsGetApiClass, tableName, COLUMN_NAME_TO_METHOD_NAME_BI_MAP, true);
            assertEquals(expectedRetrieveHandlerClass, rhut.getClass());
        }
    }
}
