package com.fsyran.forsuredb.integrationtest.singletable;

import com.fsryan.forsuredb.FSDBHelper;
import com.fsryan.forsuredb.api.Retriever;
import com.fsryan.forsuredb.api.SaveResult;
import com.fsryan.forsuredb.integrationtest.singletable.AllTypesTable;
import com.fsryan.forsuredb.queryable.DirectLocator;
import com.fsyran.forsuredb.integrationtest.DBSetup;
import com.fsyran.forsuredb.integrationtest.ExecutionLog;
import com.fsyran.forsuredb.integrationtest.AttemptedSavePair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.SQLException;

import static com.fsryan.forsuredb.integrationtest.ForSure.allTypesTable;
import static com.fsyran.forsuredb.integrationtest.singletable.AllTypesTableTestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({DBSetup.class, ExecutionLog.class})
public class InsertionAndDeletionTest {

    private static AllTypesTable allTypesApi;

    @BeforeAll
    public static void initApi() throws SQLException {
        allTypesApi = allTypesTable().getApi();
    }

    @BeforeEach
    public void startFromEmptyTable() {
        allTypesTable().set().hardDelete();
    }

    @Test
    @DisplayName("single record insertion")
    public void shouldCorrectlyInsertASingleRecord() {
        AttemptedSavePair<AllTypesTable.Record> attemptedSavePair = insertRandomRecord(1);
        assertEquals(attemptedSavePair.attempted, recordWithId(1L));
    }

    @Test
    @DisplayName("basic record update by id")
    public void shouldCorrectlyUpdateASingleRecord() {
        AttemptedSavePair<AllTypesTable.Record> insertionPair = insertRandomRecord(1);
        AttemptedSavePair<AllTypesTable.Record> updatePair = updateWithRandom(1);

        AllTypesTable.Record stored = recordWithId(1L);
        assertNotEquals(insertionPair.attempted, stored);
        assertEquals(updatePair.attempted, stored);
    }

//    @Test
//    @DisplayName("multiple record insertion")
//    public void shouldCorrectlyInsertMultipleRecords() {
//        insertConsecutivelyIncreasingRecords();
//    }
}
