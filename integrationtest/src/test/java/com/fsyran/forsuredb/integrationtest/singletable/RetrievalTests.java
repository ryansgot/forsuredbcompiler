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
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

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

    // FIND by single column (id) long

    @Test
    @DisplayName("finding by exact id")
    public void shouldFindRecordByExactId() {
        int idx = ThreadLocalRandom.current().nextInt(0, NUM_RECORDS);
        assertEquals(randomSavedRecordByIdx(idx), recordWithId(idx + 1));
    }

    @Test
    @DisplayName("finding by id between clopen range [)")
    public void shouldFindRecordWithIdBetweenClopenRangeLowerInclusive() {
        Pair<Long, Long> range = createRandomIdRange();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIdBetweenInclusive(range.first).and(range.second)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(idBetween(range, true, false));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by id between open range ()")
    public void shouldFindRecordWithIdBetweenOpenRange() {
        Pair<Long, Long> range = createRandomIdRange();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIdBetween(range.first).and(range.second)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(idBetween(range, false, false));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by id between closed range []")
    public void shouldFindRecordWithIdBetweenClosedRange() {
        Pair<Long, Long> range = createRandomIdRange();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIdBetweenInclusive(range.first).andInclusive(range.second)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(idBetween(range, true, true));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by id between clopen range (]")
    public void shouldFindRecordWithIdBetweenClopenRangeUpperInclusive() {
        Pair<Long, Long> range = createRandomIdRange();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIdBetween(range.first).andInclusive(range.second)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(idBetween(range, false, true));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by id >=")
    public void shouldFindRecordWithIdGE() {
        final long lowerInclusiveBound = randomStoredRecordId();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIdGreaterThanInclusive(lowerInclusiveBound)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> idOf(asr) >= lowerInclusiveBound);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by id >")
    public void shouldFindRecordWithIdGT() {
        final long exclusiveLowerBound = randomStoredRecordId();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIdGreaterThan(exclusiveLowerBound)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> filteredSavedRecords = savedRecordsWhere(asr -> idOf(asr) > exclusiveLowerBound);

        assertListEquals(filteredSavedRecords, actual);
    }

    @Test
    @DisplayName("finding by id <=")
    public void shouldFindRecordWithIdLE() {
        final long inclusiveUpperBound = randomStoredRecordId();

        List<AllTypesTable.Record> returnedList = retrieveToList(
                allTypesTable().find()
                        .byIdLessThanInclusive(inclusiveUpperBound)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> idOf(asr) <= inclusiveUpperBound);

        assertListEquals(expected, returnedList);
    }

    @Test
    @DisplayName("finding by id <")
    public void shouldFindRecordWithIdLT() {
        final long exclusiveUpperBound = randomStoredRecordId();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIdLessThan(exclusiveUpperBound)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> idOf(asr) < exclusiveUpperBound);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by id NOT")
    public void shouldFindRecordWithIdNOT() {
        final long exclusion = randomStoredRecordId();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIdNot(exclusion)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> idOf(asr) != exclusion);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by id from several match criteria")
    public void shouldFindRecordWithIdInExactMatchCriteria() {
        long id1 = randomStoredRecordId();
        long id2 = randomStoredRecordId();
        long id3 = randomStoredRecordId();
        long id4 = randomStoredRecordId();
        long id5 = randomStoredRecordId();
        Set<Long> allowedIds = new HashSet<>(Arrays.asList(id1, id2, id3, id4, id5));

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byId(id1, id2, id3, id4, id5)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> allowedIds.contains(idOf(asr)));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by id from several match criteria using OR")
    public void shouldFindRecordWithIdInExactMatchCriteriaUsingOR() {
        long id1 = randomStoredRecordId();
        long id2 = randomStoredRecordId();
        long id3 = randomStoredRecordId();
        long id4 = randomStoredRecordId();
        long id5 = randomStoredRecordId();
        Set<Long> allowedIds = new HashSet<>(Arrays.asList(id1, id2, id3, id4, id5));

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byId(id1)
                            .or().byId(id2)
                            .or().byId(id3)
                            .or().byId(id4)
                            .or().byId(id5)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> allowedIds.contains(idOf(asr)));

        assertListEquals(expected, actual);
    }

    // finding by string_column

    @Test
    @DisplayName("finding string_column exact match single string")
    public void shouldFindRecordWithExactStringMatchSingleString() {
        final String match = randomSavedRecord().stringColumn();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byStringColumn(match)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> match.equals(asr.getAttemptedRecord().stringColumn()));

        assertListEquals(expected, actual);
        assertEquals(1, actual.size());   // <-- string_column is unique
    }

    @Test
    @DisplayName("finding string_column NOT equal to match")
    public void shouldFindRecordsWithNotMatchString() {
        final String match = randomSavedRecord().stringColumn();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byStringColumnNot(match)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> !match.equals(asr.getAttemptedRecord().stringColumn()));

        assertListEquals(expected, actual);
        assertEquals(NUM_RECORDS - 1, actual.size());   // <-- string_column is unique
    }

    @Test
    @DisplayName("finding string_column exact match multiple strings OR")
    public void shouldFindRecordsWithMultipleStringMatchCriteria() {
        String match1 = randomSavedRecord().stringColumn();
        String match2 = randomSavedRecord().stringColumn();
        String match3 = randomSavedRecord().stringColumn();
        String match4 = randomSavedRecord().stringColumn();
        String match5 = randomSavedRecord().stringColumn();

        final Set<String> matches = new HashSet<>(Arrays.asList(match1, match2, match3, match4, match5));

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byStringColumn(match1)
                            .or().byStringColumn(match2)
                            .or().byStringColumn(match3)
                            .or().byStringColumn(match4)
                            .or().byStringColumn(match5)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)    // <-- if we wanted to just judge inclusion, then we could, but this ensures equal order
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> matches.contains(asr.getAttemptedRecord().stringColumn()));

        assertListEquals(expected, actual);
        assertEquals(matches.size(), actual.size());    // <-- string_column is unique
    }

    @Test
    @DisplayName("finding string_column exact match multiple strings")
    public void shouldFindRecordsWithMultipleStringMatchConditions() {
        String match1 = randomSavedRecord().stringColumn();
        String match2 = randomSavedRecord().stringColumn();
        String match3 = randomSavedRecord().stringColumn();
        String match4 = randomSavedRecord().stringColumn();
        String match5 = randomSavedRecord().stringColumn();

        final Set<String> matches = new HashSet<>(Arrays.asList(match1, match2, match3, match4, match5));

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byStringColumn(match1, match2, match3, match4, match5)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)    // <-- if we wanted to just judge inclusion, then we could, but this ensures equal order
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> matches.contains(asr.getAttemptedRecord().stringColumn()));

        assertListEquals(expected, actual);
        assertEquals(matches.size(), actual.size());    // <-- string_column is unique
    }

    @Test
    @DisplayName("finding string_column Less Than")
    public void shouldFindRecordsStringLT() {
        final String ltString = randomSavedRecord().stringColumn();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byStringColumnLessThan(ltString)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)    // <-- if we wanted to just judge inclusion, then we could, but this ensures equal order
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> stringColOf(asr).compareTo(ltString) < 0);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding string_column Greater Than")
    public void shouldFindRecordsStringGT() {
        final String gtString = randomSavedRecord().stringColumn();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byStringColumnGreaterThan(gtString)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)    // <-- if we wanted to just judge inclusion, then we could, but this ensures equal order
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> stringColOf(asr).compareTo(gtString) > 0);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding string_column Less Than OR Equal To")
    public void shouldFindRecordsStringLE() {
        final String leString = randomSavedRecord().stringColumn();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byStringColumnLessThanInclusive(leString)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)    // <-- if we wanted to just judge inclusion, then we could, but this ensures equal order
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> stringColOf(asr).compareTo(leString) <= 0);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding string_column Greater Than OR Equal To")
    public void shouldFindRecordsStringGE() {
        final String geString = randomSavedRecord().stringColumn();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byStringColumnGreaterThanInclusive(geString)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)    // <-- if we wanted to just judge inclusion, then we could, but this ensures equal order
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> stringColOf(asr).compareTo(geString) >= 0);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding string_column in non-inclusive range")
    public void shouldFindRecordsStringInNonInclusiveRange() {
        Pair<String, String> range = createRandomStoredStringColumnRange();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byStringColumnBetween(range.first).and(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)    // <-- if we wanted to just judge inclusion, then we could, but this ensures equal order
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(stringColBetween(range, false, false));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding string_column in clopen [) range")
    public void shouldFindRecordsStringInClopenLowerInclusiveRange() {
        Pair<String, String> range = createRandomStoredStringColumnRange();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byStringColumnBetweenInclusive(range.first).and(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)    // <-- if we wanted to just judge inclusion, then we could, but this ensures equal order
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(stringColBetween(range, true, false));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding string_column in clopen (] range")
    public void shouldFindRecordsStringInClopenHigherInclusiveRange() {
        Pair<String, String> range = createRandomStoredStringColumnRange();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byStringColumnBetween(range.first).andInclusive(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)    // <-- if we wanted to just judge inclusion, then we could, but this ensures equal order
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(stringColBetween(range, false, true));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding string_column in [] range")
    public void shouldFindRecordsStringInClosedRange() {
        Pair<String, String> range = createRandomStoredStringColumnRange();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byStringColumnBetweenInclusive(range.first).andInclusive(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)    // <-- if we wanted to just judge inclusion, then we could, but this ensures equal order
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(stringColBetween(range, true, true));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding string_column LIKE")
    @Disabled("fails due to not surrounding like string with single-quotes")    // TODO: fix this in sqlitelib and write unit test
    public void shouldFindRecordsStringLIKE() {
        final String likeStr = "a";
        final Pattern p = Pattern.compile(likeStr);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byStringColumnLike(likeStr)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)    // <-- if we wanted to just judge inclusion, then we could, but this ensures equal order
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asp -> p.matcher(stringColOf(asp)).matches());

        assertListEquals(expected, actual);
    }

    // FIND by single int column

    @Test
    @DisplayName("finding by exact int_column")
    public void shouldFindRecordsByExactIntColumn() {
        final int intColumn = randomSavedRecord().intColumn();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byIntColumn(intColumn)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asp -> intColOf(asp) == intColumn);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by int_column between clopen range [)")
    public void shouldFindRecordsByIntColumnBetweenClopenLowerInclusive() {
        Pair<Integer, Integer> range = createRandomIntRange();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIntColumnBetweenInclusive(range.first).and(range.second)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(intColBetween(range, true, false));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by int_column between open range ()")
    public void shouldFindRecordWithIntColumnBetweenOpenRange() {
        Pair<Integer, Integer> range = createRandomIntRange();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIntColumnBetween(range.first).and(range.second)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(intColBetween(range, false, false));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by int_column between closed range []")
    public void shouldFindRecordWithIntColumnBetweenClosedRange() {
        Pair<Integer, Integer> range = createRandomIntRange();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIntColumnBetweenInclusive(range.first).andInclusive(range.second)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(intColBetween(range, true, true));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by int_column between clopen range (]")
    public void shouldFindRecordWithIntColumnBetweenClopenRangeUpperInclusive() {
        Pair<Integer, Integer> range = createRandomIntRange();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIntColumnBetween(range.first).andInclusive(range.second)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(intColBetween(range, false, true));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by int_column >=")
    public void shouldFindRecordWithIntColumnGE() {
        final int lowerInclusiveBound = ThreadLocalRandom.current().nextInt();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIntColumnGreaterThanInclusive(lowerInclusiveBound)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> intColOf(asr) >= lowerInclusiveBound);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by int_column >")
    public void shouldFindRecordWithIntColumnGT() {
        final int exclusiveLowerBound = ThreadLocalRandom.current().nextInt();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIntColumnGreaterThan(exclusiveLowerBound)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> filteredSavedRecords = savedRecordsWhere(asr -> intColOf(asr) > exclusiveLowerBound);

        assertListEquals(filteredSavedRecords, actual);
    }

    @Test
    @DisplayName("finding by int_column <=")
    public void shouldFindRecordWithIntColumnLE() {
        final int inclusiveUpperBound = ThreadLocalRandom.current().nextInt();

        List<AllTypesTable.Record> returnedList = retrieveToList(
                allTypesTable().find()
                        .byIntColumnLessThanInclusive(inclusiveUpperBound)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> intColOf(asr) <= inclusiveUpperBound);

        assertListEquals(expected, returnedList);
    }

    @Test
    @DisplayName("finding by int_column <")
    public void shouldFindRecordWithIntColumnLT() {
        final int exclusiveUpperBound = ThreadLocalRandom.current().nextInt();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIntColumnLessThan(exclusiveUpperBound)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> intColOf(asr) < exclusiveUpperBound);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by int_column NOT")
    public void shouldFindRecordWithIntColumnNOT() {
        final int exclusion = randomSavedRecord().intColumn();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIntColumnNot(exclusion)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> intColOf(asr) != exclusion);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by int_column from several match criteria")
    public void shouldFindRecordWithIntColumnInExactMatchCriteria() {
        int match1 = randomSavedRecord().intColumn();
        int match2 = randomSavedRecord().intColumn();
        int match3 = randomSavedRecord().intColumn();
        int match4 = randomSavedRecord().intColumn();
        int match5 = randomSavedRecord().intColumn();
        Set<Integer> matches = new HashSet<>(Arrays.asList(match1, match2, match3, match4, match5));

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIntColumn(match1, match2, match3, match4, match5)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> matches.contains(intColOf(asr)));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by int_column from several match criteria using OR")
    public void shouldFindRecordWithIntColumnInExactMatchCriteriaUsingOR() {
        int match1 = randomSavedRecord().intColumn();
        int match2 = randomSavedRecord().intColumn();
        int match3 = randomSavedRecord().intColumn();
        int match4 = randomSavedRecord().intColumn();
        int match5 = randomSavedRecord().intColumn();
        Set<Integer> matches = new HashSet<>(Arrays.asList(match1, match2, match3, match4, match5));

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byIntColumn(match1)
                        .or().byIntColumn(match2)
                        .or().byIntColumn(match3)
                        .or().byIntColumn(match4)
                        .or().byIntColumn(match5)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> matches.contains(intColOf(asr)));

        assertListEquals(expected, actual);
    }

    // TODO: find by date, blob, double, float, wrapper classes, BigDecimal, BigInteger
    // TODO: find by multiple parameters with a single AND
    // TODO: find by multiple parameters with multiple ANDs
    // TODO: find by multiple parameters with a single OR
    // TODO: find by mutliple parameters with multiple ORs
    // TODO: find by multiple parameters with an OR and AND
    // TODO: find by multiple parameters with an AND and OR
    // TODO: find by multiple parameters with multiple AND and ORs

    static String stringColOf(AttemptedSavePair<AllTypesTable.Record> asr) {
        return asr.getAttemptedRecord().stringColumn();
    }

    private static AllTypesTable.Record randomSavedRecord() {
        return randomSavedRecordByIdx(randomSavedRecordIdx());
    }

    private static AllTypesTable.Record randomSavedRecordByIdx(int idx) {
        return savedRecords.get(idx).getAttemptedRecord();
    }

    private static int randomSavedRecordIdx() {
        return ThreadLocalRandom.current().nextInt(0, NUM_RECORDS);
    }

    private static long randomStoredRecordId() {
        return randomSavedRecordIdx() + 1;
    }

    private static List<AllTypesTable.Record> savedRecordsWhere(Predicate<AttemptedSavePair<AllTypesTable.Record>> predicate) {
        return savedRecords.stream()
                .filter(predicate)
                .map(AttemptedSavePair::getAttemptedRecord)
                .collect(toList());
    }

    private static long idOf(AttemptedSavePair<AllTypesTable.Record> asr) {
        return asr.getResult().inserted().id;
    }

    private static int intColOf(AttemptedSavePair<AllTypesTable.Record> asr) {
        return asr.getAttemptedRecord().intColumn();
    }

    private static Pair<String, String> createRandomStoredStringColumnRange() {
        return createRandomRange(() -> randomSavedRecord().stringColumn());
    }

    private static Pair<Integer, Integer> createRandomIntRange() {
        return createRandomRange(() -> ThreadLocalRandom.current().nextInt());
    }

    private static Pair<Long, Long> createRandomIdRange() {
        return createRandomRange(RetrievalTests::randomStoredRecordId);
    }

    private static <T extends Comparable<T>> Pair<T, T> createRandomRange(Supplier<T> supplier) {
        T t1 = supplier.get();
        T t2 = supplier.get();
        while (t1.equals(t2)) {
            t2 = supplier.get();
        }
        return t1.compareTo(t2) > 0 ? new Pair<>(t2, t1) : new Pair<>(t1, t2);
    }

    private static Predicate<AttemptedSavePair<AllTypesTable.Record>> stringColBetween(Pair<String, String> range, boolean lowerInclusive, boolean upperInclusive) {
        return columnBetween(range, lowerInclusive, upperInclusive, RetrievalTests::stringColOf);
    }

    private static Predicate<AttemptedSavePair<AllTypesTable.Record>> intColBetween(Pair<Integer, Integer> range, boolean lowerInclusive, boolean upperInclusive) {
        return columnBetween(range, lowerInclusive, upperInclusive, RetrievalTests::intColOf);
    }

    private static Predicate<AttemptedSavePair<AllTypesTable.Record>> idBetween(Pair<Long, Long> range, boolean lowerInclusive, boolean upperInclusive) {
        return columnBetween(range, lowerInclusive, upperInclusive, RetrievalTests::idOf);
    }

    // generic comparison with range
    private static <T extends Comparable<T>> Predicate<AttemptedSavePair<AllTypesTable.Record>> columnBetween(Pair<T, T> range, boolean lowerInclusive, boolean upperInclusive, Unpacker<T> unpacker) {
        return asp -> {
            int lowCompare = range.first.compareTo(unpacker.unpack(asp));
            if (lowCompare > 0 || (!lowerInclusive && lowCompare == 0)) {
                return false;
            }
            int highCompare = range.second.compareTo(unpacker.unpack(asp));
            return highCompare >= 0 && (upperInclusive || highCompare != 0);
        };
    }

    private interface Unpacker<T> {
        T unpack(AttemptedSavePair<AllTypesTable.Record> asr);
    }
}
