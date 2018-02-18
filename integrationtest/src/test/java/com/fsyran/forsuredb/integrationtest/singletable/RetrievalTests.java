package com.fsyran.forsuredb.integrationtest.singletable;

import com.fsryan.forsuredb.api.OrderBy;
import com.fsryan.forsuredb.integrationtest.singletable.AllTypesTable;
import com.fsyran.forsuredb.integrationtest.AttemptedSavePair;
import com.fsyran.forsuredb.integrationtest.DBSetup;
import com.fsyran.forsuredb.integrationtest.ExecutionLog;
import com.fsyran.forsuredb.integrationtest.Pair;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.fsryan.forsuredb.integrationtest.ForSure.allTypesTable;
import static com.fsyran.forsuredb.integrationtest.MoreAssertions.assertAscending;
import static com.fsyran.forsuredb.integrationtest.MoreAssertions.assertDescending;
import static com.fsyran.forsuredb.integrationtest.MoreAssertions.assertListEquals;
import static com.fsyran.forsuredb.integrationtest.TestUtil.MEMCMP_COMPARATOR;
import static com.fsyran.forsuredb.integrationtest.singletable.AllTypesTableTestUtil.*;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>No tests in this class may modify the database. They can only retrieve from the 128 records
 * that get inserted for all tests in the class
 */
@ExtendWith({DBSetup.class, EnsureAllTypesTableEmptyBeforeClass.class, ExecutionLog.class})
public class RetrievalTests {
    
    private static final int NUM_RECORDS = 128;

    // TWO COLUMN COMPARATORS

    private static final Comparator<Pair<Boolean, String>> sameDirectionComparator = (o1, o2) -> {
        int compare = Boolean.compare(o1.first, o2.first);
        return compare != 0 ? compare : o1.second.compareTo(o2.second);
    };

    private static final Comparator<Pair<Boolean, String>> firstDescSecondAscComparator = (o1, o2) -> {
        int compare = Boolean.compare(o1.first, o2.first);
        return compare != 0 ? compare * -1 : o1.second.compareTo(o2.second);
    };

    private static final Comparator<Pair<Boolean, String>> firstASCSecondDescComparator = (o1, o2) -> {
        int compare = Boolean.compare(o1.first, o2.first);
        return compare != 0 ? compare : o1.second.compareTo(o2.second) * -1;
    };

    private static List<AttemptedSavePair<AllTypesTable.Record>> savedRecords;

    @BeforeAll
    public static void insert128RandomRecords() {
        savedRecords = insertRandomRecords(NUM_RECORDS);
    }

    // ONE COLUMN SORTS

    @Test
    @DisplayName("sort retrieval by boolean ASC")
    public void shouldCorrectlySortRetrievalByBooleanASC() {
        List<Boolean> expectedAscending = retrieveToList(allTypesTable().order().byBooleanColumn(OrderBy.ORDER_ASC).then().get())
                .stream()
                .map(AllTypesTable.Record::booleanColumn)
                .collect(toList());
        assertAscending(expectedAscending);
    }

    @Test
    @DisplayName("sort retrieval by boolean DESC")
    public void shouldCorrectlySortRetrievalByBooleanDESC() {
        List<Boolean> expectedDescending = retrieveToList(allTypesTable().order().byBooleanColumn(OrderBy.ORDER_DESC).then().get())
                .stream()
                .map(AllTypesTable.Record::booleanColumn)
                .collect(toList());
        assertDescending(expectedDescending);
    }

    @Test
    @DisplayName("sort retrieval by Boolean ASC")
    public void shouldCorrectlySortRetrievalBooleanWrapperASC() {
        List<Boolean> expectedAscending = retrieveToList(allTypesTable().order().byBooleanWrapperColumn(OrderBy.ORDER_ASC).then().get())
                .stream()
                .map(AllTypesTable.Record::booleanWrapperColumn)
                .collect(toList());
        assertAscending(expectedAscending);
    }

    @Test
    @DisplayName("sort retrieval by Boolean DESC")
    public void shouldCorrectlySortRetrievalBooleanWrapperDESC() {
        List<Boolean> expectedDescending = retrieveToList(allTypesTable().order().byBooleanWrapperColumn(OrderBy.ORDER_DESC).then().get())
                .stream()
                .map(AllTypesTable.Record::booleanWrapperColumn)
                .collect(toList());
        assertDescending(expectedDescending);
    }

    @Test
    @DisplayName("sort retrieval by int ASC")
    public void shouldCorrectlySortRetrievalByIntASC() {
        List<Integer> expectedAscending = retrieveToList(allTypesTable().order().byIntColumn(OrderBy.ORDER_ASC).then().get())
                .stream()
                .map(AllTypesTable.Record::intColumn)
                .collect(toList());
        assertAscending(expectedAscending);
    }

    @Test
    @DisplayName("sort retrieval by int DESC")
    public void shouldCorrectlySortRetrievalByIntDESC() {
        List<Integer> expectedDescending = retrieveToList(allTypesTable().order().byIntColumn(OrderBy.ORDER_DESC).then().get())
                .stream()
                .map(AllTypesTable.Record::intColumn)
                .collect(toList());
        assertDescending(expectedDescending);
    }

    @Test
    @DisplayName("sort retrieval by Integer ASC")
    public void shouldCorrectlySortRetrievalByIntegerWrapperASC() {
        List<Integer> expectedAscending = retrieveToList(allTypesTable().order().byIntegerWrapperColumn(OrderBy.ORDER_ASC).then().get())
                .stream()
                .map(AllTypesTable.Record::integerWrapperColumn)
                .collect(toList());
        assertAscending(expectedAscending);
    }

    @Test
    @DisplayName("sort retrieval by Integer DESC")
    public void shouldCorrectlySortRetrievalByIntegerWrapperDESC() {
        List<Integer> expectedDescending = retrieveToList(allTypesTable().order().byIntegerWrapperColumn(OrderBy.ORDER_DESC).then().get())
                .stream()
                .map(AllTypesTable.Record::integerWrapperColumn)
                .collect(toList());
        assertDescending(expectedDescending);
    }

    @Test
    @DisplayName("sort retrieval by long ASC")
    public void shouldCorrectlySortRetrievalByLongASC() {
        List<Long> expectedAscending = retrieveToList(allTypesTable().order().byLongColumn(OrderBy.ORDER_ASC).then().get())
                .stream()
                .map(AllTypesTable.Record::longColumn)
                .collect(toList());
        assertAscending(expectedAscending);
    }

    @Test
    @DisplayName("sort retrieval by long DESC")
    public void shouldCorrectlySortRetrievalByLongDESC() {
        List<Long> expectedDescending = retrieveToList(allTypesTable().order().byLongColumn(OrderBy.ORDER_DESC).then().get())
                .stream()
                .map(AllTypesTable.Record::longColumn)
                .collect(toList());
        assertDescending(expectedDescending);
    }

    @Test
    @DisplayName("sort retrieval by Long ASC")
    public void shouldCorrectlySortRetrievalByLongWrapperASC() {
        List<Long> expectedAscending = retrieveToList(allTypesTable().order().byLongWrapperColumn(OrderBy.ORDER_ASC).then().get())
                .stream()
                .map(AllTypesTable.Record::longWrapperColumn)
                .collect(toList());
        assertAscending(expectedAscending);
    }

    @Test
    @DisplayName("sort retrieval by Long DESC")
    public void shouldCorrectlySortRetrievalByLongWrapperDESC() {
        List<Long> expectedDescending = retrieveToList(allTypesTable().order().byLongWrapperColumn(OrderBy.ORDER_DESC).then().get())
                .stream()
                .map(AllTypesTable.Record::longWrapperColumn)
                .collect(toList());
        assertDescending(expectedDescending);
    }

    @Test
    @DisplayName("sort retrieval by float ASC")
    public void shouldCorrectlySortRetrievalByFloatASC() {
        List<Float> expectedAscending = retrieveToList(allTypesTable().order().byFloatColumn(OrderBy.ORDER_ASC).then().get())
                .stream()
                .map(AllTypesTable.Record::floatColumn)
                .collect(toList());
        assertAscending(expectedAscending);
    }

    @Test
    @DisplayName("sort retrieval by float DESC")
    public void shouldCorrectlySortRetrievalByFloatDESC() {
        List<Float> expectedDescending = retrieveToList(allTypesTable().order().byFloatColumn(OrderBy.ORDER_DESC).then().get())
                .stream()
                .map(AllTypesTable.Record::floatColumn)
                .collect(toList());
        assertDescending(expectedDescending);
    }

    @Test
    @DisplayName("sort retrieval by Float ASC")
    public void shouldCorrectlySortRetrievalByFloatWrapperASC() {
        List<Float> expectedAscending = retrieveToList(allTypesTable().order().byFloatWrapperColumn(OrderBy.ORDER_ASC).then().get())
                .stream()
                .map(AllTypesTable.Record::floatWrapperColumn)
                .collect(toList());
        assertAscending(expectedAscending);
    }

    @Test
    @DisplayName("sort retrieval by Float DESC")
    public void shouldCorrectlySortRetrievalByFloatWrapperDESC() {
        List<Float> expectedDescending = retrieveToList(allTypesTable().order().byFloatWrapperColumn(OrderBy.ORDER_DESC).then().get())
                .stream()
                .map(AllTypesTable.Record::floatWrapperColumn)
                .collect(toList());
        assertDescending(expectedDescending);
    }

    @Test
    @DisplayName("sort retrieval by double ASC")
    public void shouldCorrectlySortRetrievalByDoubleASC() {
        List<Double> expectedAscending = retrieveToList(allTypesTable().order().byDoubleColumn(OrderBy.ORDER_ASC).then().get())
                .stream()
                .map(AllTypesTable.Record::doubleColumn)
                .collect(toList());
        assertAscending(expectedAscending);
    }

    @Test
    @DisplayName("sort retrieval by double DESC")
    public void shouldCorrectlySortRetrievalByDoubleDESC() {
        List<Double> expectedDescending = retrieveToList(allTypesTable().order().byDoubleColumn(OrderBy.ORDER_DESC).then().get())
                .stream()
                .map(AllTypesTable.Record::doubleColumn)
                .collect(toList());
        assertDescending(expectedDescending);
    }

    @Test
    @DisplayName("sort retrieval by Double ASC")
    public void shouldCorrectlySortRetrievalByDoubleWrapperASC() {
        List<Double> expectedAscending = retrieveToList(allTypesTable().order().byDoubleWrapperColumn(OrderBy.ORDER_ASC).then().get())
                .stream()
                .map(AllTypesTable.Record::doubleWrapperColumn)
                .collect(toList());
        assertAscending(expectedAscending);
    }

    @Test
    @DisplayName("sort retrieval by Double DESC")
    public void shouldCorrectlySortRetrievalByDoubleWrapperDESC() {
        List<Double> expectedDescending = retrieveToList(allTypesTable().order().byDoubleWrapperColumn(OrderBy.ORDER_DESC).then().get())
                .stream()
                .map(AllTypesTable.Record::doubleWrapperColumn)
                .collect(toList());
        assertDescending(expectedDescending);
    }

    @Test
    @DisplayName("sort retrieval by Date ASC")
    @Disabled("not sure why this is not currently working") // <-- TODO: figure out why this test is failing. It should not.
    public void shouldCorrectlySortRetrievalByDateASC() {
        List<Date> expectedAscending = retrieveToList(allTypesTable().order().byDateColumn(OrderBy.ORDER_ASC).then().get())
                .stream()
                .map(AllTypesTable.Record::dateColumn)
                .collect(toList());
        assertAscending(expectedAscending);
    }

    @Test
    @DisplayName("sort retrieval by Date DESC")
    @Disabled("not sure why this is not currently working") // <-- TODO: figure out why this test is failing. It should not.
    public void shouldCorrectlySortRetrievalByDateDESC() {
        List<Date> expectedDescending = retrieveToList(allTypesTable().order().byDateColumn(OrderBy.ORDER_DESC).then().get())
                .stream()
                .map(AllTypesTable.Record::dateColumn)
                .collect(toList());
        assertDescending(expectedDescending);
    }

    @Test
    @DisplayName("sort retrieval by byte[] ASC")
    public void shouldCorrectlySortByByteArrayASC() {
        List<byte[]> expectedAscending = retrieveToList(allTypesTable().order().byByteArrayColumn(OrderBy.ORDER_ASC).then().get())
                .stream()
                .map(AllTypesTable.Record::byteArrayColumn)
                .collect(toList());
        assertAscending(expectedAscending, MEMCMP_COMPARATOR);
    }

    @Test
    @DisplayName("sort retrieval by byte[] DESC")
    public void shouldCorrectlySortByByteArrayDESC() {
        List<byte[]> expectedDescending = retrieveToList(allTypesTable().order().byByteArrayColumn(OrderBy.ORDER_DESC).then().get())
                .stream()
                .map(AllTypesTable.Record::byteArrayColumn)
                .collect(toList());
        assertDescending(expectedDescending, MEMCMP_COMPARATOR);
    }

    @Test
    @DisplayName("sort retrieval by String ASC")
    public void shouldCorrectlySortByStringASC() {
        List<String> expectedAscending = retrieveToList(allTypesTable().order().byStringColumn(OrderBy.ORDER_ASC).then().get())
                .stream()
                .map(AllTypesTable.Record::stringColumn)
                .collect(toList());
        assertAscending(expectedAscending);
    }

    @Test
    @DisplayName("sort retrieval by String DESC")
    public void shouldCorrectlySortByStringDESC() {
        List<String> expectedDescending = retrieveToList(allTypesTable().order().byStringColumn(OrderBy.ORDER_DESC).then().get())
                .stream()
                .map(AllTypesTable.Record::stringColumn)
                .collect(toList());
        assertDescending(expectedDescending);
    }

    @Test
    @DisplayName("sort retrieval by BigInteger ASC")
    @Disabled("Not working because stored as a string") // TODO: make sorting work properly or make them not sortable
    public void shouldCorrectlySortByBigIntegerASC() {
        List<BigInteger> expectedAscending = retrieveToList(allTypesTable().order().byBigIntegerColumn(OrderBy.ORDER_ASC).then().get())
                .stream()
                .map(AllTypesTable.Record::bigIntegerColumn)
                .collect(toList());
        assertAscending(expectedAscending);
    }

    @Test
    @DisplayName("sort retrieval by BigInteger DESC")
    @Disabled("Not working because stored as a string") // TODO: make sorting work properly or make them not sortable
    public void shouldCorrectlySortByBigIntegerDESC() {
        List<BigInteger> expectedDescending = retrieveToList(allTypesTable().order().byBigIntegerColumn(OrderBy.ORDER_DESC).then().get())
                .stream()
                .map(AllTypesTable.Record::bigIntegerColumn)
                .collect(toList());
        assertDescending(expectedDescending);
    }

    @Test
    @DisplayName("sort retrieval by BigDecimal ASC")
    @Disabled("Not working because stored as a string")  // TODO: make sorting work properly or make them not sortable
    public void shouldCorrectlySortByBigDecimalASC() {
        List<BigDecimal> expectedAscending = retrieveToList(allTypesTable().order().byBigDecimalColumn(OrderBy.ORDER_ASC).then().get())
                .stream()
                .map(AllTypesTable.Record::bigDecimalColumn)
                .collect(toList());
        assertAscending(expectedAscending);
    }

    @Test
    @DisplayName("sort retrieval by BigDecimal DESC")
    @Disabled("Not working because stored as a string")  // TODO: make sorting work properly or make them not sortable
    public void shouldCorrectlySortByBigDecimalDESC() {
        List<BigDecimal> expectedDescending = retrieveToList(allTypesTable().order().byBigDecimalColumn(OrderBy.ORDER_DESC).then().get())
                .stream()
                .map(AllTypesTable.Record::bigDecimalColumn)
                .collect(toList());
        assertDescending(expectedDescending);
    }

    // TWO COLUMN SORTS: boolean column will have a fair number of duplicates, so it's ideal to use as the first sorting criteria for the tests

    @Test
    @DisplayName("sort by two columns both ASC")
    public void sortByTwoColumnsBothASC() {
        List<Pair<Boolean, String>> expectedAscending = retrieveToList(
                allTypesTable()
                        .order().byBooleanColumn(OrderBy.ORDER_ASC)
                        .and().byStringColumn(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        ).stream()
                .map(r -> new Pair<>(r.booleanColumn(), r.stringColumn()))
                .collect(toList());
        assertAscending(expectedAscending, sameDirectionComparator);
    }

    @Test
    @DisplayName("sort by two columns both DESC")
    public void sortByTwoColumnsBothDESC() {
        List<Pair<Boolean, String>> expectedDescending = retrieveToList(
                allTypesTable()
                        .order().byBooleanColumn(OrderBy.ORDER_DESC)
                        .and().byStringColumn(OrderBy.ORDER_DESC)
                        .then()
                        .get()
        ).stream()
                .map(r -> new Pair<>(r.booleanColumn(), r.stringColumn()))
                .collect(toList());
        assertDescending(expectedDescending, sameDirectionComparator);
    }

    @Test
    @DisplayName("sort by two columns first ASC second DESC")
    public void sortByTwoColumnsFirstASCSecondDESC() {
        List<Pair<Boolean, String>> expectedAscending = retrieveToList(
                allTypesTable()
                        .order().byBooleanColumn(OrderBy.ORDER_ASC)
                        .and().byStringColumn(OrderBy.ORDER_DESC)
                        .then()
                        .get()
        ).stream()
                .map(r -> new Pair<>(r.booleanColumn(), r.stringColumn()))
                .collect(toList());
        assertAscending(expectedAscending, firstASCSecondDescComparator);
    }

    @Test
    @DisplayName("sort by two columns first DESC second ASC")
    public void sortByTwoColumnsFirstDESCSecondASC() {
        List<Pair<Boolean, String>> expectedAscending = retrieveToList(
                allTypesTable()
                        .order().byBooleanColumn(OrderBy.ORDER_DESC)
                        .and().byStringColumn(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        ).stream()
                .map(r -> new Pair<>(r.booleanColumn(), r.stringColumn()))
                .collect(toList());
        assertAscending(expectedAscending, firstDescSecondAscComparator);
    }

    // FROM BOTTOM SORTS (which have inner queries that have sorting options flipped):

    @Test
    @DisplayName("sort retrieval by int ASC from bottom")
    public void shouldCorrectlySortRetrievalByIntASCFromBottom() {
        List<Integer> expectedAscending = retrieveToList(
                allTypesTable()
                        .find().last(64).byNotDeleted()
                        .then()
                        .order().byIntColumn(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        ).stream()
                .map(AllTypesTable.Record::intColumn)
                .collect(toList());
        assertAscending(expectedAscending);
    }

    @Test
    @DisplayName("sort retrieval by int DESC")
    public void shouldCorrectlySortRetrievalByIntDESCFromBottom() {
        List<Integer> expectedDescending = retrieveToList(
                allTypesTable()
                        .find().last(64).byNotDeleted()
                        .then()
                        .order().byIntColumn(OrderBy.ORDER_DESC)
                        .then()
                        .get()
        ).stream()
                .map(AllTypesTable.Record::intColumn)
                .collect(toList());
        assertDescending(expectedDescending);
    }

    @Test
    @DisplayName("sort by two columns both ASC from bottom")
    public void sortByTwoColumnsBothASCFromBottom() {
        List<Pair<Boolean, String>> expectedAscending = retrieveToList(
                allTypesTable()
                        .find().last(64).byNotDeleted()
                        .then()
                        .order().byBooleanColumn(OrderBy.ORDER_ASC)
                        .and().byStringColumn(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        ).stream()
                .map(r -> new Pair<>(r.booleanColumn(), r.stringColumn()))
                .collect(toList());
        assertAscending(expectedAscending, sameDirectionComparator);
    }

    @Test
    @DisplayName("sort by two columns both DESC from bottom")
    public void sortByTwoColumnsBothDESCFromBottom() {
        List<Pair<Boolean, String>> expectedDescending = retrieveToList(
                allTypesTable()
                        .find().last(64).byNotDeleted()
                        .then()
                        .order().byBooleanColumn(OrderBy.ORDER_DESC)
                        .and().byStringColumn(OrderBy.ORDER_DESC)
                        .then()
                        .get()
        ).stream()
                .map(r -> new Pair<>(r.booleanColumn(), r.stringColumn()))
                .collect(toList());
        assertDescending(expectedDescending, sameDirectionComparator);
    }

    @Test
    @DisplayName("sort by two columns first ASC second DESC from bottom")
    public void sortByTwoColumnsFirstASCSecondDESCFromBottom() {
        List<Pair<Boolean, String>> expectedAscending = retrieveToList(
                allTypesTable()
                        .find().last(64).byNotDeleted()
                        .then()
                        .order().byBooleanColumn(OrderBy.ORDER_ASC)
                        .and().byStringColumn(OrderBy.ORDER_DESC)
                        .then()
                        .get()
        ).stream()
                .map(r -> new Pair<>(r.booleanColumn(), r.stringColumn()))
                .collect(toList());
        assertAscending(expectedAscending, firstASCSecondDescComparator);
    }

    @Test
    @DisplayName("sort by two columns first DESC second ASC from bottom")
    public void sortByTwoColumnsFirstDESCSecondASCFromBottom() {
        List<Pair<Boolean, String>> expectedAscending = retrieveToList(
                allTypesTable()
                        .find().last(64).byNotDeleted()
                        .then()
                        .order().byBooleanColumn(OrderBy.ORDER_DESC)
                        .and().byStringColumn(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        ).stream()
                .map(r -> new Pair<>(r.booleanColumn(), r.stringColumn()))
                .collect(toList());
        assertAscending(expectedAscending, firstDescSecondAscComparator);
    }

    // FIND by single column (id)

    @Test
    @DisplayName("finding by exact id")
    public void findColumnByExactId() {
        int id = ThreadLocalRandom.current().nextInt(0, NUM_RECORDS);
        assertEquals(savedRecords.get(id).getRecord(), recordWithId(id + 1));
    }

    @Test
    @DisplayName("finding by id between clopen range [)")
    public void findColumnWithIdBetweenClopenRangeLowerInclusive() {
        Pair<Long, Long> idRange = createRandomRange(1L, NUM_RECORDS);

        List<AllTypesTable.Record> returnedList = retrieveToList(
                allTypesTable().find()
                        .byIdBetweenInclusive(idRange.first).and(idRange.second)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> filteredSavedRecords = savedRecords.stream()
                .filter(asr -> asr.getResult().inserted().id >= idRange.first && asr.getResult().inserted().id < idRange.second)
                .map(asr -> asr.getRecord())
                .collect(toList());

        assertListEquals(filteredSavedRecords, returnedList);
    }

    @Test
    @DisplayName("finding by id between open range ()")
    public void findColumnWithIdBetweenOpenRange() {
        Pair<Long, Long> idRange = createRandomRange(1L, NUM_RECORDS);

        List<AllTypesTable.Record> returnedList = retrieveToList(
                allTypesTable().find()
                        .byIdBetween(idRange.first).and(idRange.second)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> filteredSavedRecords = savedRecords.stream()
                .filter(asr -> asr.getResult().inserted().id > idRange.first && asr.getResult().inserted().id < idRange.second)
                .map(asr -> asr.getRecord())
                .collect(toList());

        assertListEquals(filteredSavedRecords, returnedList);
    }

    @Test
    @DisplayName("finding by id between closed range []")
    public void findColumnWithIdBetweenClosedRange() {
        Pair<Long, Long> idRange = createRandomRange(1L, NUM_RECORDS);

        List<AllTypesTable.Record> returnedList = retrieveToList(
                allTypesTable().find()
                        .byIdBetweenInclusive(idRange.first).andInclusive(idRange.second)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> filteredSavedRecords = savedRecords.stream()
                .filter(asr -> asr.getResult().inserted().id >= idRange.first && asr.getResult().inserted().id <= idRange.second)
                .map(asr -> asr.getRecord())
                .collect(toList());

        assertListEquals(filteredSavedRecords, returnedList);
    }

    @Test
    @DisplayName("finding by id between clopen range (]")
    public void findColumnWithIdBetweenClopenRangeUpperInclusive() {
        Pair<Long, Long> idRange = createRandomRange(1L, NUM_RECORDS);

        List<AllTypesTable.Record> returnedList = retrieveToList(
                allTypesTable().find()
                        .byIdBetween(idRange.first).andInclusive(idRange.second)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> filteredSavedRecords = savedRecords.stream()
                .filter(asr -> asr.getResult().inserted().id > idRange.first && asr.getResult().inserted().id <= idRange.second)
                .map(asr -> asr.getRecord())
                .collect(toList());

        assertListEquals(filteredSavedRecords, returnedList);
    }

    @Test
    @DisplayName("finding by id >=")
    public void findColumnWithIdGE() {
        final long lowerInclusiveBound = ThreadLocalRandom.current().nextLong(0, NUM_RECORDS) + 1;

        List<AllTypesTable.Record> returnedList = retrieveToList(
                allTypesTable().find()
                        .byIdGreaterThanInclusive(lowerInclusiveBound)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> filteredSavedRecords = savedRecords.stream()
                .filter(asr -> asr.getResult().inserted().id >= lowerInclusiveBound)
                .map(asr -> asr.getRecord())
                .collect(toList());

        assertListEquals(filteredSavedRecords, returnedList);
    }

    @Test
    @DisplayName("finding by id >")
    public void findColumnWithIdGT() {
        final long exclusiveLowerBound = ThreadLocalRandom.current().nextLong(0, NUM_RECORDS) + 1;

        List<AllTypesTable.Record> returnedList = retrieveToList(
                allTypesTable().find()
                        .byIdGreaterThan(exclusiveLowerBound)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> filteredSavedRecords = savedRecords.stream()
                .filter(asr -> asr.getResult().inserted().id > exclusiveLowerBound)
                .map(asr -> asr.getRecord())
                .collect(toList());

        assertListEquals(filteredSavedRecords, returnedList);
    }

    @Test
    @DisplayName("finding by id <=")
    public void findColumnWithIdLE() {
        final long inclusiveUpperBound = ThreadLocalRandom.current().nextLong(0, NUM_RECORDS) + 1;

        List<AllTypesTable.Record> returnedList = retrieveToList(
                allTypesTable().find()
                        .byIdLessThanInclusive(inclusiveUpperBound)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> filteredSavedRecords = savedRecords.stream()
                .filter(asr -> asr.getResult().inserted().id <= inclusiveUpperBound)
                .map(asr -> asr.getRecord())
                .collect(toList());

        assertListEquals(filteredSavedRecords, returnedList);
    }

    @Test
    @DisplayName("finding by id <")
    public void findColumnWithIdLT() {
        final long exclusiveUpperBound = ThreadLocalRandom.current().nextLong(0, NUM_RECORDS) + 1;

        List<AllTypesTable.Record> returnedList = retrieveToList(
                allTypesTable().find()
                        .byIdLessThan(exclusiveUpperBound)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> filteredSavedRecords = savedRecords.stream()
                .filter(asr -> asr.getResult().inserted().id < exclusiveUpperBound)
                .map(asr -> asr.getRecord())
                .collect(toList());

        assertListEquals(filteredSavedRecords, returnedList);
    }

    @Test
    @DisplayName("finding by id NOT")
    public void findColumnWithIdNOT() {
        final long exclusion = ThreadLocalRandom.current().nextLong(0, NUM_RECORDS) + 1;

        List<AllTypesTable.Record> returnedList = retrieveToList(
                allTypesTable().find()
                        .byIdNot(exclusion)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> filteredSavedRecords = savedRecords.stream()
                .filter(asr -> asr.getResult().inserted().id != exclusion)
                .map(asr -> asr.getRecord())
                .collect(toList());

        assertListEquals(filteredSavedRecords, returnedList);
    }

    @Test
    @DisplayName("finding by id from several match criteria")
    public void findColumnWithIdInExactMatchCriteria() {
        long id1 = ThreadLocalRandom.current().nextLong(0, NUM_RECORDS) + 1;
        long id2 = ThreadLocalRandom.current().nextLong(0, NUM_RECORDS) + 1;
        long id3 = ThreadLocalRandom.current().nextLong(0, NUM_RECORDS) + 1;
        long id4 = ThreadLocalRandom.current().nextLong(0, NUM_RECORDS) + 1;
        long id5 = ThreadLocalRandom.current().nextLong(0, NUM_RECORDS) + 1;
        Set<Long> allowedIds = new HashSet<>(Arrays.asList(id1, id2, id3, id4, id5));

        List<AllTypesTable.Record> returnedList = retrieveToList(
                allTypesTable().find()
                        .byId(id1, id2, id3, id4, id5)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> filteredSavedRecords = savedRecords.stream()
                .filter(asr -> allowedIds.contains(asr.getResult().inserted().id))
                .map(asr -> asr.getRecord())
                .collect(toList());

        assertListEquals(filteredSavedRecords, returnedList);
    }

    @Test
    @DisplayName("finding by id from several match criteria using OR")
    public void findColumnWithIdInExactMatchCriteriaUsingOR() {
        long id1 = ThreadLocalRandom.current().nextLong(0, NUM_RECORDS) + 1;
        long id2 = ThreadLocalRandom.current().nextLong(0, NUM_RECORDS) + 1;
        long id3 = ThreadLocalRandom.current().nextLong(0, NUM_RECORDS) + 1;
        long id4 = ThreadLocalRandom.current().nextLong(0, NUM_RECORDS) + 1;
        long id5 = ThreadLocalRandom.current().nextLong(0, NUM_RECORDS) + 1;
        Set<Long> allowedIds = new HashSet<>(Arrays.asList(id1, id2, id3, id4, id5));

        List<AllTypesTable.Record> returnedList = retrieveToList(
                allTypesTable()
                        .find().byId(id1)
                        .or().byId(id2)
                        .or().byId(id3)
                        .or().byId(id4)
                        .or().byId(id5)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> filteredSavedRecords = savedRecords.stream()
                .filter(asr -> allowedIds.contains(asr.getResult().inserted().id))
                .map(asr -> asr.getRecord())
                .collect(toList());

        assertListEquals(filteredSavedRecords, returnedList);
    }

    // TODO: find by string, int, date, blob, double, and float
    // TODO: find by multiple parameters

    private Pair<Long, Long> createRandomRange(long inclusiveLowerBound, long inclusiveUpperBound) {
        long id1 = ThreadLocalRandom.current().nextLong(inclusiveLowerBound, inclusiveUpperBound) + 1;
        long id2 = ThreadLocalRandom.current().nextLong(inclusiveLowerBound, inclusiveUpperBound) + 1;
        while (id1 == id2) {
            id2 = ThreadLocalRandom.current().nextLong(inclusiveLowerBound, inclusiveUpperBound) + 1;
        }
        return new Pair<>(Math.min(id1, id2), Math.max(id1, id2));
    }
}
