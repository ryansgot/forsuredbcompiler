package com.fsyran.forsuredb.integrationtest.singletable;

import com.fsryan.forsuredb.api.Retriever;
import com.fsryan.forsuredb.integrationtest.singletable.AllTypesTable;
import com.fsyran.forsuredb.integrationtest.DBSetup;
import com.fsyran.forsuredb.integrationtest.ExecutionLog;
import com.fsyran.forsuredb.integrationtest.AttemptedSavePair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.SQLException;
import java.util.List;

import static com.fsryan.forsuredb.integrationtest.ForSure.allTypesTable;
import static com.fsyran.forsuredb.integrationtest.MoreAssertions.assertCount;
import static com.fsyran.forsuredb.integrationtest.singletable.AllTypesTableTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({DBSetup.class, EnsureAllTypesTableEmptyBeforeTest.class, ExecutionLog.class})
public class BasicCreateRetrieveTest {

    @Test
    @DisplayName("single record insertion")
    public void shouldCorrectlyInsertASingleRecord() {
        AttemptedSavePair<AllTypesTable.Record> attemptedSavePair = insertRandomRecord(1);
        assertEquals(attemptedSavePair.getAttemptedRecord(), recordWithId(1L));
    }

    @Test
    @DisplayName("updating should not add record")
    public void shouldCorrectlyUpdateASingleRecord() throws SQLException {
        AttemptedSavePair<AllTypesTable.Record> insertionPair = insertRandomRecord(1);
        AttemptedSavePair<AllTypesTable.Record> updatePair = updateWithRandom(1);

        AllTypesTable.Record stored = recordWithId(1L);
        assertNotEquals(insertionPair.getAttemptedRecord(), stored);
        assertEquals(updatePair.getAttemptedRecord(), stored);

        assertCount("all_types", 1);
    }

    @Test
    @DisplayName("multiple record insertion")
    public void shouldCorrectlyInsertMultipleRecords() {
        List<AttemptedSavePair<AllTypesTable.Record>> insertedPairs = insertRandomRecords(10);
        verifyConsecutiveRecords(insertedPairs, 1L);
    }

    @Test
    @DisplayName("Insert and retrieve records that have null values")
    public void shouldCorrectlyInsertAndRetrieveRecordsWithNullValues() throws Exception {
        AllTypesTable.Record inserted = AllTypesTable.Record.createRandomWithNullableElementsNull();
        AttemptedSavePair<AllTypesTable.Record> savePair = insertRecord(inserted);
        if (savePair.getResult().exception() != null) {
            throw savePair.getResult().exception();
        }

        Retriever r = allTypesTable()
                .find().byBigDecimalColumn(null)
                    .and().byBigIntegerColumn(null)
                    .and().byBooleanWrapperColumn(null)
                    .and().byDateColumnOn(null)
                    .and().byDoubleWrapperColumn(null)
                    .and().byFloatWrapperColumn(null)
                    .and().byIntegerWrapperColumn(null)
                    .and().byLongWrapperColumn(null)
                    .and().byStringColumn(null)
                    .and().byByteArrayColumn(null)
                .then()
                .get();
        AllTypesTable.Record retrieved = extractRecordFrom(r);
        r.close();
        assertEquals(inserted, retrieved);
    }

    @Test
    @DisplayName("Get the count of records")
    public void shouldCorrectlyRetrieveRecordCount() {
        insertRandomRecords(10);
        assertEquals(10, allTypesTable().getCount());
    }

    @Test
    @DisplayName("Get the count of records with selection")
    public void shouldCorrectlyRetrieveRecordCountWithSelection() {
        insertRandomRecords(10);
        assertEquals(4, allTypesTable().find().byIdGreaterThan(6).then().getCount());
    }
}
