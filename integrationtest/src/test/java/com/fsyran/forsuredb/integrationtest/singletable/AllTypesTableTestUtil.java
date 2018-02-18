package com.fsyran.forsuredb.integrationtest.singletable;

import com.fsryan.forsuredb.api.Retriever;
import com.fsryan.forsuredb.api.SaveResult;
import com.fsryan.forsuredb.integrationtest.singletable.AllTypesTable;
import com.fsryan.forsuredb.integrationtest.singletable.AllTypesTableResolver;
import com.fsryan.forsuredb.queryable.DirectLocator;
import com.fsyran.forsuredb.integrationtest.AttemptedSavePair;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.fsryan.forsuredb.integrationtest.ForSure.allTypesTable;
import static com.fsyran.forsuredb.integrationtest.MoreAssertions.assertSuccessfulInsertion;
import static com.fsyran.forsuredb.integrationtest.MoreAssertions.assertSuccessfulUpdate;
import static com.fsyran.forsuredb.integrationtest.TestUtil.SMALL_DOUBLE;
import static com.fsyran.forsuredb.integrationtest.TestUtil.SMALL_FLOAT;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;

abstract class AllTypesTableTestUtil {

    private static final AllTypesTable allTypesApi = allTypesTable().getApi();

    public static AttemptedSavePair<AllTypesTable.Record> insertRandomRecord(long expectedId) {
        AllTypesTable.Record record = AllTypesTable.Record.createRandom();

        SaveResult<DirectLocator> result = allTypesTable().set()
                .bigDecimalColumn(record.bigDecimalColumn())
                .bigIntegerColumn(record.bigIntegerColumn())
                .booleanColumn(record.booleanColumn())
                .booleanWrapperColumn(record.booleanWrapperColumn())
                .byteArrayColumn(record.byteArrayColumn())
                .dateColumn(record.dateColumn())
                .doubleColumn(record.doubleColumn())
                .doubleWrapperColumn(record.doubleWrapperColumn())
                .floatColumn(record.floatColumn())
                .floatWrapperColumn(record.floatWrapperColumn())
                .intColumn(record.intColumn())
                .integerWrapperColumn(record.integerWrapperColumn())
                .longColumn(record.longColumn())
                .longWrapperColumn(record.longWrapperColumn())
                .stringColumn(record.stringColumn())
                .save();

        assertSuccessfulInsertion(result, "all_types", expectedId);

        return new AttemptedSavePair<>(result, record);
    }

    public static void assertListEquals(List<AllTypesTable.Record> expectedList, List<AllTypesTable.Record> actualList) {
        IntStream.range(0, expectedList.size())
                .forEach(i -> {
                    final AllTypesTable.Record expected = expectedList.get(i);
                    final AllTypesTable.Record actual = actualList.get(i);
                    if (!(expected.equals(actual))) {
                        fail("ASC[" + i + "]: expected = " + expected + "; but was = " + actual);
                    }
                });
        assertEquals(expectedList.size(), actualList.size());
    }

    public static AttemptedSavePair<AllTypesTable.Record> updateWithRandom(long id) {
        AllTypesTable.Record overwriteRecord = AllTypesTable.Record.createRandom();
        SaveResult<DirectLocator> result = allTypesTable()
                .find()
                    .byId(id)
                .then()
                .set()
                    .bigDecimalColumn(overwriteRecord.bigDecimalColumn())
                    .bigIntegerColumn(overwriteRecord.bigIntegerColumn())
                    .booleanColumn(overwriteRecord.booleanColumn())
                    .booleanWrapperColumn(overwriteRecord.booleanWrapperColumn())
                    .byteArrayColumn(overwriteRecord.byteArrayColumn())
                    .dateColumn(overwriteRecord.dateColumn())
                    .doubleColumn(overwriteRecord.doubleColumn())
                    .doubleWrapperColumn(overwriteRecord.doubleWrapperColumn())
                    .floatColumn(overwriteRecord.floatColumn())
                    .floatWrapperColumn(overwriteRecord.floatWrapperColumn())
                    .intColumn(overwriteRecord.intColumn())
                    .integerWrapperColumn(overwriteRecord.integerWrapperColumn())
                    .longColumn(overwriteRecord.longColumn())
                    .longWrapperColumn(overwriteRecord.longWrapperColumn())
                    .stringColumn(overwriteRecord.stringColumn())
                .save();

        assertSuccessfulUpdate(result, 1);

        return new AttemptedSavePair<>(result, overwriteRecord);
    }

    public static void verifyConsecutiveRecords(List<AttemptedSavePair<AllTypesTable.Record>> attemptedSaves, long startingIdInclusive) {
        verifyConsecutiveRecords(attemptedSaves, startingIdInclusive, Long.MAX_VALUE);
    }

    public static void verifyConsecutiveRecords(List<AttemptedSavePair<AllTypesTable.Record>> attemptedSaves, long startingIdInclusive, long endingIdExclusive) {
        try (Retriever r = allTypesTable()
                .find().byIdBetweenInclusive(startingIdInclusive).and(endingIdExclusive)
                .then()
                .get()) {
            if (!r.moveToFirst()) {
                fail("there were no records at or above id: " + startingIdInclusive);
            }
            int i = 0;
            do {
                assertEquals(startingIdInclusive + i, allTypesApi.id(r));
                assertEquals(attemptedSaves.get(i).attempted, extractRecordFrom(r));
                i++;
            } while (r.moveToNext());
            assertEquals(attemptedSaves.size(), i);
        }
    }

    public static void verifyColumnsAtCurrentPosition(Retriever r, long id, AllTypesTable.Record record) {
        verifyColumnsAtCurrentPosition(
                r,
                id,
                record.bigDecimalColumn(),
                record.bigIntegerColumn(),
                record.booleanColumn(),
                record.booleanWrapperColumn(),
                record.byteArrayColumn(),
                record.dateColumn(),
                record.doubleColumn(),
                record.doubleWrapperColumn(),
                record.floatColumn(),
                record.floatWrapperColumn(),
                record.intColumn(),
                record.integerWrapperColumn(),
                record.longColumn(),
                record.longWrapperColumn(),
                record.stringColumn()
        );
    }

    public static void verifyColumnsAtCurrentPosition(Retriever r,
                                                      long id,
                                                      BigDecimal expectedBigDecimalColumn,
                                                      BigInteger expectedBigIntegerColumn,
                                                      boolean expectedBooleanColumn,
                                                      Boolean expectedBooleanWrapperColumn,
                                                      byte[] expectedBytArrayColumn,
                                                      Date expectedDateColumn,
                                                      double expectedDoubleColumn,
                                                      Double expectedDoubleWrapperColumn,
                                                      float expetedFloatColumn,
                                                      Float expectedFloatWrapperColumn,
                                                      int expectedIntColumn,
                                                      Integer expectedIntegerWrapperColumn,
                                                      long expectedLongColumn,
                                                      Long expectedLongWrapperColumn,
                                                      String expectedStringColumn) {
        assertEquals(id, allTypesApi.id(r));
        assertEquals(expectedBigDecimalColumn, allTypesApi.bigDecimalColumn(r));
        assertEquals(expectedBigIntegerColumn, allTypesApi.bigIntegerColumn(r));
        assertEquals(expectedBooleanColumn, allTypesApi.booleanColumn(r));
        assertEquals(expectedBooleanWrapperColumn, allTypesApi.booleanWrapperColumn(r));
        assertArrayEquals(expectedBytArrayColumn, allTypesApi.byteArrayColumn(r));
        assertEquals(expectedDateColumn, allTypesApi.dateColumn(r));
        assertEquals(expectedDoubleColumn, allTypesApi.doubleColumn(r), SMALL_DOUBLE);
        assertEquals(expectedDoubleWrapperColumn, allTypesApi.doubleWrapperColumn(r), SMALL_DOUBLE);
        assertEquals(expetedFloatColumn, allTypesApi.floatColumn(r), SMALL_FLOAT);
        assertEquals(expectedFloatWrapperColumn, allTypesApi.floatWrapperColumn(r), SMALL_FLOAT);
        assertEquals(expectedIntColumn, allTypesApi.intColumn(r));
        assertEquals(expectedIntegerWrapperColumn, allTypesApi.integerWrapperColumn(r));
        assertEquals(expectedLongColumn, allTypesApi.longColumn(r));
        assertEquals(expectedLongWrapperColumn, allTypesApi.longWrapperColumn(r));
        assertEquals(expectedStringColumn, allTypesApi.stringColumn(r));
    }

    public static void verifyDefaultValuesAtCurrentPosition(Retriever r, long id, int expectedIntColumn) {
        assertEquals(id, allTypesApi.id(r));
        assertEquals(expectedIntColumn, allTypesApi.intColumn(r));
    }

    public static List<AttemptedSavePair<AllTypesTable.Record>> insertRandomRecords(int count, long expectedFirstId) {
        return IntStream.range(0, count)
                .mapToObj(i -> insertRandomRecord(expectedFirstId + i))
                .collect(toList());
    }

    public static List<AllTypesTable.Record> retrieveToList(Retriever r) {
        try {
            if (!r.moveToFirst()) {
                return Collections.emptyList();
            }
            List<AllTypesTable.Record> ret = new ArrayList<>();
            do {
                ret.add(extractRecordFrom(r));
            } while (r.moveToNext());

            return ret;
        } finally {
            r.close();
        }
    }

    public static AllTypesTable.Record recordWithId(long id) {
        try (Retriever r = allTypesTable()
                .find().byId(id)
                .then()
                .get()) {
            if (!r.moveToFirst()) {
                throw new RuntimeException("Did not find record with id: " + id);
            }
            return extractRecordFrom(r);
        }
    }

    private static AllTypesTable.Record extractRecordFrom(Retriever r) {
        return AllTypesTable.Record.builder()
                .bigDecimalColumn(allTypesApi.bigDecimalColumn(r))
                .bigIntegerColumn(allTypesApi.bigIntegerColumn(r))
                .booleanColumn(allTypesApi.booleanColumn(r))
                .booleanWrapperColumn(allTypesApi.booleanWrapperColumn(r))
                .byteArrayColumn(allTypesApi.byteArrayColumn(r))
                .dateColumn(allTypesApi.dateColumn(r))
                .doubleColumn(allTypesApi.doubleColumn(r))
                .doubleWrapperColumn(allTypesApi.doubleWrapperColumn(r))
                .floatColumn(allTypesApi.floatColumn(r))
                .floatWrapperColumn(allTypesApi.floatWrapperColumn(r))
                .intColumn(allTypesApi.intColumn(r))
                .integerWrapperColumn(allTypesApi.integerWrapperColumn(r))
                .longColumn(allTypesApi.longColumn(r))
                .longWrapperColumn(allTypesApi.longWrapperColumn(r))
                .stringColumn(allTypesApi.stringColumn(r))
                .build();
    }
}
