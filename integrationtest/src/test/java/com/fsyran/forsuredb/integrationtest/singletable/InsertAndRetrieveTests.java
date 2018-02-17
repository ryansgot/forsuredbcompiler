package com.fsyran.forsuredb.integrationtest.singletable;

import com.fsryan.forsuredb.integrationtest.singletable.AllTypesTable;
import com.fsyran.forsuredb.integrationtest.DBSetup;
import com.fsyran.forsuredb.integrationtest.ExecutionLog;
import com.fsyran.forsuredb.integrationtest.AttemptedSavePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static com.fsryan.forsuredb.integrationtest.ForSure.allTypesTable;
import static com.fsyran.forsuredb.integrationtest.singletable.AllTypesTableTestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@ExtendWith({DBSetup.class, ExecutionLog.class})
public class InsertAndRetrieveTests {

    private static final Logger log = LogManager.getLogger(InsertAndRetrieveTests.class);

    @BeforeEach
    public void startFromEmptyTable() {
        log.debug("clearing all_types_table_test");
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

    @Test
    @DisplayName("multiple record insertion")
    public void shouldCorrectlyInsertMultipleRecords() {
        List<AttemptedSavePair<AllTypesTable.Record>> insertedPairs = insertRandomRecords(10, 1L);
        verifyConsecutiveRecords(insertedPairs, 1L);
    }

//    @Test
//    @DisplayName("multiple record insertion")
//    public void shouldCorrectlyInsertMultipleRecords() {
//        List<AttemptedSavePair<AllTypesTable.Record>> insertedPairs = insertRandomRecords(10, 1L);
//        verifyConsecutiveRecords(insertedPairs, 1L);
//    }
}
