package com.fsyran.forsuredb.integrationtest.singletable;

import com.fsryan.forsuredb.FSDBHelper;
import com.fsryan.forsuredb.api.OrderBy;
import com.fsryan.forsuredb.integrationtest.singletable.AllTypesTable;
import com.fsyran.forsuredb.integrationtest.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.fsryan.forsuredb.integrationtest.ForSure.allTypesTable;
import static com.fsyran.forsuredb.integrationtest.MoreAssertions.assertAscending;
import static com.fsyran.forsuredb.integrationtest.MoreAssertions.assertDescending;
import static com.fsyran.forsuredb.integrationtest.MoreAssertions.assertListEquals;
import static com.fsyran.forsuredb.integrationtest.TestUtil.*;
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
    public void shouldCorrectlySortRetrievalByDateASC() {
        List<Date> expectedAscending = retrieveToList(allTypesTable().order().byDateColumn(OrderBy.ORDER_ASC).then().get())
                .stream()
                .map(AllTypesTable.Record::dateColumn)
                .collect(toList());
        assertAscending(expectedAscending);
    }

    @Test
    @DisplayName("sort retrieval by Date DESC")
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
        Pair<Long, Long> range = randomIdRange();

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
        Pair<Long, Long> range = randomIdRange();

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
        Pair<Long, Long> range = randomIdRange();

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
        Pair<Long, Long> range = randomIdRange();

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

    // FIND by single long_wrapper_column

    @Test
    @DisplayName("finding by exact long_wrapper_column")
    public void shouldFindRecordsByExactLongWrapperColumn() {
        final Long exactMatch = randomSavedRecord().longWrapperColumn();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byLongWrapperColumn(exactMatch)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asp -> longWrapperColOf(asp).equals(exactMatch));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by long_wrapper_column between clopen range [)")
    public void shouldFindRecordsByLongWrapperColumnBetweenClopenLowerInclusive() {
        Pair<Long, Long> range = randomRange(TestUtil::randomLong);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byLongWrapperColumnBetweenInclusive(range.first).and(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(longWrapperColBetween(range, true, false));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by long_wrapper_column between open range ()")
    public void shouldFindRecordWithLongWrapperColumnBetweenOpenRange() {
        Pair<Long, Long> range = randomRange(TestUtil::randomLong);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byLongWrapperColumnBetween(range.first).and(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(longWrapperColBetween(range, false, false));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by long_wrapper_column between closed range []")
    public void shouldFindRecordWithLongWrapperColumnBetweenClosedRange() {
        Pair<Long, Long> range = randomRange(TestUtil::randomLong);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byLongWrapperColumnBetweenInclusive(range.first).andInclusive(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(longWrapperColBetween(range, true, true));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by long_wrapper_column between clopen range (]")
    public void shouldFindRecordWithLongWrapperColumnBetweenClopenRangeUpperInclusive() {
        Pair<Long, Long> range = randomRange(TestUtil::randomLong);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byLongWrapperColumnBetween(range.first).andInclusive(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(longWrapperColBetween(range, false, true));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by long_wrapper_column >=")
    public void shouldFindRecordWithLongWrapperColumnGE() {
        final Long lowerInclusiveBound = ThreadLocalRandom.current().nextLong();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byLongWrapperColumnGreaterThanInclusive(lowerInclusiveBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> longWrapperColOf(asr) >= lowerInclusiveBound);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by long_wrapper_column >")
    public void shouldFindRecordWithLongWrapperColumnGT() {
        final Long exclusiveLowerBound = ThreadLocalRandom.current().nextLong();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byLongWrapperColumnGreaterThan(exclusiveLowerBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> filteredSavedRecords = savedRecordsWhere(asr -> longWrapperColOf(asr) > exclusiveLowerBound);

        assertListEquals(filteredSavedRecords, actual);
    }

    @Test
    @DisplayName("finding by long_wrapper_column <=")
    public void shouldFindRecordWithLongWrapperColumnLE() {
        final Long inclusiveUpperBound = ThreadLocalRandom.current().nextLong();

        List<AllTypesTable.Record> returnedList = retrieveToList(
                allTypesTable().find()
                        .byLongWrapperColumnLessThanInclusive(inclusiveUpperBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> longWrapperColOf(asr) <= inclusiveUpperBound);

        assertListEquals(expected, returnedList);
    }

    @Test
    @DisplayName("finding by long_wrapper_column <")
    public void shouldFindRecordWithLongWrapperColumnLT() {
        final Long exclusiveUpperBound = ThreadLocalRandom.current().nextLong();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byLongWrapperColumnLessThan(exclusiveUpperBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> longWrapperColOf(asr) < exclusiveUpperBound);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by long_wrapper_column NOT")
    public void shouldFindRecordWithLongWrapperColumnNOT() {
        final Long exclusion = randomSavedRecord().longWrapperColumn();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byLongWrapperColumnNot(exclusion)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> longWrapperColOf(asr) != exclusion);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by long_wrapper_column from several match criteria")
    public void shouldFindRecordWithLongWrapperColumnInExactMatchCriteria() {
        Long match1 = randomSavedRecord().longWrapperColumn();
        Long match2 = randomSavedRecord().longWrapperColumn();
        Long match3 = randomSavedRecord().longWrapperColumn();
        Long match4 = randomSavedRecord().longWrapperColumn();
        Long match5 = randomSavedRecord().longWrapperColumn();
        Set<Long> matches = new HashSet<>(Arrays.asList(match1, match2, match3, match4, match5));

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find().byLongWrapperColumn(match1, match2, match3, match4, match5)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> matches.contains(longWrapperColOf(asr)));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by long_wrapper_column from several match criteria using OR")
    public void shouldFindRecordWithLongWrapperColumnInExactMatchCriteriaUsingOR() {
        Long match1 = randomSavedRecord().longWrapperColumn();
        Long match2 = randomSavedRecord().longWrapperColumn();
        Long match3 = randomSavedRecord().longWrapperColumn();
        Long match4 = randomSavedRecord().longWrapperColumn();
        Long match5 = randomSavedRecord().longWrapperColumn();
        Set<Long> matches = new HashSet<>(Arrays.asList(match1, match2, match3, match4, match5));

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byLongWrapperColumn(match1)
                        .or().byLongWrapperColumn(match2)
                        .or().byLongWrapperColumn(match3)
                        .or().byLongWrapperColumn(match4)
                        .or().byLongWrapperColumn(match5)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> matches.contains(longWrapperColOf(asr)));

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
        Pair<String, String> range = randomStoredStringColumnRange();

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
        Pair<String, String> range = randomStoredStringColumnRange();

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
        Pair<String, String> range = randomStoredStringColumnRange();

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
        Pair<String, String> range = randomStoredStringColumnRange();

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

    // FIND by single int_column

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
        Pair<Integer, Integer> range = randomRange(TestUtil::randomInt);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIntColumnBetweenInclusive(range.first).and(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(intColBetween(range, true, false));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by int_column between open range ()")
    public void shouldFindRecordWithIntColumnBetweenOpenRange() {
        Pair<Integer, Integer> range = randomRange(TestUtil::randomInt);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIntColumnBetween(range.first).and(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(intColBetween(range, false, false));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by int_column between closed range []")
    public void shouldFindRecordWithIntColumnBetweenClosedRange() {
        Pair<Integer, Integer> range = randomRange(TestUtil::randomInt);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIntColumnBetweenInclusive(range.first).andInclusive(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(intColBetween(range, true, true));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by int_column between clopen range (]")
    public void shouldFindRecordWithIntColumnBetweenClopenRangeUpperInclusive() {
        Pair<Integer, Integer> range = randomRange(TestUtil::randomInt);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIntColumnBetween(range.first).andInclusive(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(intColBetween(range, false, true));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by int_column >=")
    public void shouldFindRecordWithIntColumnGE() {
        final int lowerInclusiveBound = randomInt();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIntColumnGreaterThanInclusive(lowerInclusiveBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> intColOf(asr) >= lowerInclusiveBound);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by int_column >")
    public void shouldFindRecordWithIntColumnGT() {
        final int exclusiveLowerBound = randomInt();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIntColumnGreaterThan(exclusiveLowerBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> filteredSavedRecords = savedRecordsWhere(asr -> intColOf(asr) > exclusiveLowerBound);

        assertListEquals(filteredSavedRecords, actual);
    }

    @Test
    @DisplayName("finding by int_column <=")
    public void shouldFindRecordWithIntColumnLE() {
        final int inclusiveUpperBound = randomInt();

        List<AllTypesTable.Record> returnedList = retrieveToList(
                allTypesTable().find()
                        .byIntColumnLessThanInclusive(inclusiveUpperBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> intColOf(asr) <= inclusiveUpperBound);

        assertListEquals(expected, returnedList);
    }

    @Test
    @DisplayName("finding by int_column <")
    public void shouldFindRecordWithIntColumnLT() {
        final int exclusiveUpperBound = randomInt();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIntColumnLessThan(exclusiveUpperBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
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
                        .order().byId(OrderBy.ORDER_ASC)
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
                        .order().byId(OrderBy.ORDER_ASC)
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
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> matches.contains(intColOf(asr)));

        assertListEquals(expected, actual);
    }

    // FIND by single integer_wrapper_column

    @Test
    @DisplayName("finding by exact integer_wrapper_column")
    public void shouldFindRecordsByExactIntegerWrapperColumn() {
        final Integer exactMatch = randomSavedRecord().integerWrapperColumn();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byIntegerWrapperColumn(exactMatch)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asp -> integerWrapperColOf(asp).equals(exactMatch));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by integer_wrapper_column between clopen range [)")
    public void shouldFindRecordsByIntegerWrapperColumnBetweenClopenLowerInclusive() {
        Pair<Integer, Integer> range = randomRange(TestUtil::randomInt);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIntegerWrapperColumnBetweenInclusive(range.first).and(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(integerWrapperColBetween(range, true, false));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by integer_wrapper_column between open range ()")
    public void shouldFindRecordWithIntegerWrapperColumnBetweenOpenRange() {
        Pair<Integer, Integer> range = randomRange(TestUtil::randomInt);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIntegerWrapperColumnBetween(range.first).and(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(integerWrapperColBetween(range, false, false));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by integer_wrapper_column between closed range []")
    public void shouldFindRecordWithIntegerWrapperColumnBetweenClosedRange() {
        Pair<Integer, Integer> range = randomRange(TestUtil::randomInt);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIntegerWrapperColumnBetweenInclusive(range.first).andInclusive(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(integerWrapperColBetween(range, true, true));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by integer_wrapper_column between clopen range (]")
    public void shouldFindRecordWithIntegerWrapperColumnBetweenClopenRangeUpperInclusive() {
        Pair<Integer, Integer> range = randomRange(TestUtil::randomInt);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIntegerWrapperColumnBetween(range.first).andInclusive(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(integerWrapperColBetween(range, false, true));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by integer_wrapper_column >=")
    public void shouldFindRecordWithIntegerWrapperColumnGE() {
        final Integer lowerInclusiveBound = randomInt();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIntegerWrapperColumnGreaterThanInclusive(lowerInclusiveBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> integerWrapperColOf(asr).compareTo(lowerInclusiveBound) >= 0);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by integer_wrapper_column >")
    public void shouldFindRecordWithIntegerWrapperColumnGT() {
        final Integer exclusiveLowerBound = randomInt();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIntegerWrapperColumnGreaterThan(exclusiveLowerBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> filteredSavedRecords = savedRecordsWhere(asr -> integerWrapperColOf(asr).compareTo(exclusiveLowerBound) > 0);

        assertListEquals(filteredSavedRecords, actual);
    }

    @Test
    @DisplayName("finding by integer_wrapper_column <=")
    public void shouldFindRecordWithIntegerWrapperColumnLE() {
        final Integer inclusiveUpperBound = randomInt();

        List<AllTypesTable.Record> returnedList = retrieveToList(
                allTypesTable().find()
                        .byIntegerWrapperColumnLessThanInclusive(inclusiveUpperBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> integerWrapperColOf(asr).compareTo(inclusiveUpperBound) <= 0);

        assertListEquals(expected, returnedList);
    }

    @Test
    @DisplayName("finding by integer_wrapper_column <")
    public void shouldFindRecordWithIntegerWrapperColumnLT() {
        final Integer exclusiveUpperBound = randomInt();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIntegerWrapperColumnLessThan(exclusiveUpperBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> integerWrapperColOf(asr).compareTo(exclusiveUpperBound) < 0);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by integer_wrapper_column NOT")
    public void shouldFindRecordWithIntegerWrapperColumnNOT() {
        final Integer exclusion = randomSavedRecord().integerWrapperColumn();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byIntegerWrapperColumnNot(exclusion)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> !integerWrapperColOf(asr).equals(exclusion));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by integer_wrapper_column from several match criteria")
    public void shouldFindRecordWithIntegerWrapperColumnInExactMatchCriteria() {
        Integer match1 = randomSavedRecord().integerWrapperColumn();
        Integer match2 = randomSavedRecord().integerWrapperColumn();
        Integer match3 = randomSavedRecord().integerWrapperColumn();
        Integer match4 = randomSavedRecord().integerWrapperColumn();
        Integer match5 = randomSavedRecord().integerWrapperColumn();
        Set<Integer> matches = new HashSet<>(Arrays.asList(match1, match2, match3, match4, match5));

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find().byIntegerWrapperColumn(match1, match2, match3, match4, match5)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> matches.contains(integerWrapperColOf(asr)));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by integer_wrapper_column from several match criteria using OR")
    public void shouldFindRecordWithIntegerWrapperColumnInExactMatchCriteriaUsingOR() {
        Integer match1 = randomSavedRecord().integerWrapperColumn();
        Integer match2 = randomSavedRecord().integerWrapperColumn();
        Integer match3 = randomSavedRecord().integerWrapperColumn();
        Integer match4 = randomSavedRecord().integerWrapperColumn();
        Integer match5 = randomSavedRecord().integerWrapperColumn();
        Set<Integer> matches = new HashSet<>(Arrays.asList(match1, match2, match3, match4, match5));

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byIntegerWrapperColumn(match1)
                            .or().byIntegerWrapperColumn(match2)
                            .or().byIntegerWrapperColumn(match3)
                            .or().byIntegerWrapperColumn(match4)
                            .or().byIntegerWrapperColumn(match5)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> matches.contains(integerWrapperColOf(asr)));

        assertListEquals(expected, actual);
    }

    // FIND by single float_column

    @Test
    @DisplayName("finding by exact float_column")
    @Disabled("floating point equivalence working out differently than expected after being serialized")    // <-- TODO: fix by using fuzz-factor for floating point values
    public void shouldFindRecordsByExactFloatColumn() {
        final float floatColumn = randomSavedRecord().floatColumn();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byFloatColumn(floatColumn)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asp -> floatColOf(asp) == floatColumn);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by float_column between clopen range [)")
    public void shouldFindRecordsByFloatColumnBetweenClopenLowerInclusive() {
        Pair<Float, Float> range = randomRange(TestUtil::randomFloat);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byFloatColumnBetweenInclusive(range.first).and(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(floatColBetween(range, true, false));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by float_column between open range ()")
    public void shouldFindRecordWithFloatColumnBetweenOpenRange() {
        Pair<Float, Float> range = randomRange(TestUtil::randomFloat);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byFloatColumnBetween(range.first).and(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(floatColBetween(range, false, false));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by float_column between closed range []")
    public void shouldFindRecordWithFloatColumnBetweenClosedRange() {
        Pair<Float, Float> range = randomRange(TestUtil::randomFloat);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byFloatColumnBetweenInclusive(range.first).andInclusive(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(floatColBetween(range, true, true));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by float_column between clopen range (]")
    public void shouldFindRecordWithFloatColumnBetweenClopenRangeUpperInclusive() {
        Pair<Float, Float> range = randomRange(TestUtil::randomFloat);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byFloatColumnBetween(range.first).andInclusive(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(floatColBetween(range, false, true));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by float_column >=")
    public void shouldFindRecordWithFloatColumnGE() {
        final float lowerInclusiveBound = randomFloat();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byFloatColumnGreaterThanInclusive(lowerInclusiveBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> floatColOf(asr) >= lowerInclusiveBound);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by float_column >")
    public void shouldFindRecordWithFloatColumnGT() {
        final float exclusiveLowerBound = randomFloat();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byFloatColumnGreaterThan(exclusiveLowerBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> filteredSavedRecords = savedRecordsWhere(asr -> floatColOf(asr) > exclusiveLowerBound);

        assertListEquals(filteredSavedRecords, actual);
    }

    @Test
    @DisplayName("finding by float_column <=")
    public void shouldFindRecordWithFloatColumnLE() {
        final float inclusiveUpperBound = randomFloat();

        List<AllTypesTable.Record> returnedList = retrieveToList(
                allTypesTable().find()
                        .byFloatColumnLessThanInclusive(inclusiveUpperBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> floatColOf(asr) <= inclusiveUpperBound);

        assertListEquals(expected, returnedList);
    }

    @Test
    @DisplayName("finding by float_column <")
    public void shouldFindRecordWithFloatColumnLT() {
        final float exclusiveUpperBound = randomFloat();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byFloatColumnLessThan(exclusiveUpperBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> floatColOf(asr) < exclusiveUpperBound);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by float_column NOT")
    @Disabled("floating point equivalence working out differently than expected after being serialized")    // <-- TODO: fix by using fuzz-factor for floating point values
    public void shouldFindRecordWithFloatColumnNOT() {
        final float exclusion = randomSavedRecord().floatColumn();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byFloatColumnNot(exclusion)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> floatColOf(asr) != exclusion);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by float_column from several match criteria")
    @Disabled("floating point equivalence working out differently than expected after being serialized")    // <-- TODO: fix by using fuzz-factor for floating point values
    public void shouldFindRecordWithFloatColumnInExactMatchCriteria() {
        float match1 = randomSavedRecord().floatColumn();
        float match2 = randomSavedRecord().floatColumn();
        float match3 = randomSavedRecord().floatColumn();
        float match4 = randomSavedRecord().floatColumn();
        float match5 = randomSavedRecord().floatColumn();
        Set<Float> matches = new HashSet<>(Arrays.asList(match1, match2, match3, match4, match5));

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byFloatColumn(match1, match2, match3, match4, match5)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> matches.contains(floatColOf(asr)));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by float_column from several match criteria using OR")
    @Disabled("floating point equivalence working out differently than expected after being serialized")    // <-- TODO: fix by using fuzz-factor for floating point values
    public void shouldFindRecordWithFloatColumnInExactMatchCriteriaUsingOR() {
        float match1 = randomSavedRecord().floatColumn();
        float match2 = randomSavedRecord().floatColumn();
        float match3 = randomSavedRecord().floatColumn();
        float match4 = randomSavedRecord().floatColumn();
        float match5 = randomSavedRecord().floatColumn();
        Set<Float> matches = new HashSet<>(Arrays.asList(match1, match2, match3, match4, match5));

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byFloatColumn(match1)
                        .or().byFloatColumn(match2)
                        .or().byFloatColumn(match3)
                        .or().byFloatColumn(match4)
                        .or().byFloatColumn(match5)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> matches.contains(floatColOf(asr)));

        assertListEquals(expected, actual);
    }


    // FIND by single float_wrapper_column

    @Test
    @DisplayName("finding by exact float_wrapper_column")
    @Disabled("floating point equivalence working out differently than expected after being serialized")    // <-- TODO: fix by using fuzz-factor for floating point values
    public void shouldFindRecordsByExactFloatWrapperColumn() {
        final Float exactMatch = randomSavedRecord().floatWrapperColumn();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byFloatWrapperColumn(exactMatch)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asp -> floatWrapperColOf(asp) == exactMatch);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by float_wrapper_column between clopen range [)")
    public void shouldFindRecordsByFloatWrapperColumnBetweenClopenLowerInclusive() {
        Pair<Float, Float> range = randomRange(TestUtil::randomFloat);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byFloatWrapperColumnBetweenInclusive(range.first).and(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(floatWrapperColBetween(range, true, false));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by float_wrapper_column between open range ()")
    public void shouldFindRecordWithFloatWrapperColumnBetweenOpenRange() {
        Pair<Float, Float> range = randomRange(TestUtil::randomFloat);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byFloatWrapperColumnBetween(range.first).and(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(floatWrapperColBetween(range, false, false));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by float_wrapper_column between closed range []")
    public void shouldFindRecordWithFloatWrapperColumnBetweenClosedRange() {
        Pair<Float, Float> range = randomRange(TestUtil::randomFloat);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byFloatWrapperColumnBetweenInclusive(range.first).andInclusive(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(floatWrapperColBetween(range, true, true));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by float_wrapper_column between clopen range (]")
    public void shouldFindRecordWithFloatWrapperColumnBetweenClopenRangeUpperInclusive() {
        Pair<Float, Float> range = randomRange(TestUtil::randomFloat);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byFloatWrapperColumnBetween(range.first).andInclusive(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(floatWrapperColBetween(range, false, true));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by float_wrapper_column >=")
    public void shouldFindRecordWithFloatWrapperColumnGE() {
        final Float lowerInclusiveBound = randomFloat();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byFloatWrapperColumnGreaterThanInclusive(lowerInclusiveBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> floatWrapperColOf(asr).compareTo(lowerInclusiveBound) >= 0);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by float_wrapper_column >")
    public void shouldFindRecordWithFloatWrapperColumnGT() {
        final Float exclusiveLowerBound = randomFloat();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byFloatWrapperColumnGreaterThan(exclusiveLowerBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> filteredSavedRecords = savedRecordsWhere(asr -> floatWrapperColOf(asr).compareTo(exclusiveLowerBound) > 0);

        assertListEquals(filteredSavedRecords, actual);
    }

    @Test
    @DisplayName("finding by float_wrapper_column <=")
    public void shouldFindRecordWithFloatWrapperColumnLE() {
        final Float inclusiveUpperBound = randomFloat();

        List<AllTypesTable.Record> returnedList = retrieveToList(
                allTypesTable().find()
                        .byFloatWrapperColumnLessThanInclusive(inclusiveUpperBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> floatWrapperColOf(asr).compareTo(inclusiveUpperBound) <= 0);

        assertListEquals(expected, returnedList);
    }

    @Test
    @DisplayName("finding by float_wrapper_column <")
    public void shouldFindRecordWithFloatWrapperColumnLT() {
        final Float exclusiveUpperBound = randomFloat();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byFloatWrapperColumnLessThan(exclusiveUpperBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> floatWrapperColOf(asr).compareTo(exclusiveUpperBound) < 0);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by float_wrapper_column NOT")
    @Disabled("floating point equivalence working out differently than expected after being serialized")    // <-- TODO: fix by using fuzz-factor for floating point values
    public void shouldFindRecordWithFloatWrapperColumnNOT() {
        final Float exclusion = randomSavedRecord().floatWrapperColumn();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byFloatWrapperColumnNot(exclusion)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> !floatWrapperColOf(asr).equals(exclusion));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by float_wrapper_column from several match criteria")
    @Disabled("floating point equivalence working out differently than expected after being serialized")    // <-- TODO: fix by using fuzz-factor for floating point values
    public void shouldFindRecordWithFloatWrapperColumnInExactMatchCriteria() {
        Float match1 = randomSavedRecord().floatWrapperColumn();
        Float match2 = randomSavedRecord().floatWrapperColumn();
        Float match3 = randomSavedRecord().floatWrapperColumn();
        Float match4 = randomSavedRecord().floatWrapperColumn();
        Float match5 = randomSavedRecord().floatWrapperColumn();
        Set<Float> matches = new HashSet<>(Arrays.asList(match1, match2, match3, match4, match5));

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byFloatWrapperColumn(match1, match2, match3, match4, match5)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> matches.contains(floatWrapperColOf(asr)));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by float_wrapper_column from several match criteria using OR")
    @Disabled("floating point equivalence working out differently than expected after being serialized")    // <-- TODO: fix by using fuzz-factor for floating point values
    public void shouldFindRecordWithFloatWrapperColumnInExactMatchCriteriaUsingOR() {
        Float match1 = randomSavedRecord().floatWrapperColumn();
        Float match2 = randomSavedRecord().floatWrapperColumn();
        Float match3 = randomSavedRecord().floatWrapperColumn();
        Float match4 = randomSavedRecord().floatWrapperColumn();
        Float match5 = randomSavedRecord().floatWrapperColumn();
        Set<Float> matches = new HashSet<>(Arrays.asList(match1, match2, match3, match4, match5));

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byFloatWrapperColumn(match1)
                            .or().byFloatWrapperColumn(match2)
                            .or().byFloatWrapperColumn(match3)
                            .or().byFloatWrapperColumn(match4)
                            .or().byFloatWrapperColumn(match5)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> matches.contains(floatWrapperColOf(asr)));

        assertListEquals(expected, actual);
    }

    // FIND by single double_column

    @Test
    @DisplayName("finding by exact double_column")
    @Disabled("floating point equivalence working out differently than expected after being serialized")    // <-- TODO: fix by using fuzz-factor for floating point values
    public void shouldFindRecordsByExactDoubleColumn() {
        final double exactMatch = randomSavedRecord().doubleColumn();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byDoubleColumn(exactMatch)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asp -> doubleColOf(asp) == exactMatch);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by double_column between clopen range [)")
    public void shouldFindRecordsByDoubleColumnBetweenClopenLowerInclusive() {
        Pair<Double, Double> range = randomRange(TestUtil::randomDouble);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byDoubleColumnBetweenInclusive(range.first).and(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(doubleColBetween(range, true, false));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by double_column between open range ()")
    public void shouldFindRecordWithDoubleColumnBetweenOpenRange() {
        Pair<Double, Double> range = randomRange(TestUtil::randomDouble);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byDoubleColumnBetween(range.first).and(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(doubleColBetween(range, false, false));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by double_column between closed range []")
    public void shouldFindRecordWithDoubleColumnBetweenClosedRange() {
        Pair<Double, Double> range = randomRange(TestUtil::randomDouble);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byDoubleColumnBetweenInclusive(range.first).andInclusive(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(doubleColBetween(range, true, true));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by double_column between clopen range (]")
    public void shouldFindRecordWithDoubleColumnBetweenClopenRangeUpperInclusive() {
        Pair<Double, Double> range = randomRange(TestUtil::randomDouble);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byDoubleColumnBetween(range.first).andInclusive(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(doubleColBetween(range, false, true));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by double_column >=")
    public void shouldFindRecordWithDoubleColumnGE() {
        final double lowerInclusiveBound = randomDouble();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byDoubleColumnGreaterThanInclusive(lowerInclusiveBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> doubleColOf(asr) >= lowerInclusiveBound);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by double_column >")
    public void shouldFindRecordWithDoubleColumnGT() {
        final double exclusiveLowerBound = randomDouble();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byDoubleColumnGreaterThan(exclusiveLowerBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> filteredSavedRecords = savedRecordsWhere(asr -> doubleColOf(asr) > exclusiveLowerBound);

        assertListEquals(filteredSavedRecords, actual);
    }

    @Test
    @DisplayName("finding by double_column <=")
    public void shouldFindRecordWithDoubleColumnLE() {
        final double inclusiveUpperBound = randomDouble();

        List<AllTypesTable.Record> returnedList = retrieveToList(
                allTypesTable().find()
                        .byDoubleColumnLessThanInclusive(inclusiveUpperBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> doubleColOf(asr) <= inclusiveUpperBound);

        assertListEquals(expected, returnedList);
    }

    @Test
    @DisplayName("finding by double_column <")
    public void shouldFindRecordWithDoubleColumnLT() {
        final double exclusiveUpperBound = randomDouble();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byDoubleColumnLessThan(exclusiveUpperBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> doubleColOf(asr) < exclusiveUpperBound);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by double_column NOT")
    @Disabled("floating point equivalence working out differently than expected after being serialized")    // <-- TODO: fix by using fuzz-factor for floating point values
    public void shouldFindRecordWithDoubleColumnNOT() {
        final double exclusion = randomSavedRecord().doubleColumn();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byDoubleColumnNot(exclusion)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> doubleColOf(asr) != exclusion);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by double_column from several match criteria")
    @Disabled("floating point equivalence working out differently than expected after being serialized")    // <-- TODO: fix by using fuzz-factor for floating point values
    public void shouldFindRecordWithDoubleColumnInExactMatchCriteria() {
        double match1 = randomSavedRecord().doubleColumn();
        double match2 = randomSavedRecord().doubleColumn();
        double match3 = randomSavedRecord().doubleColumn();
        double match4 = randomSavedRecord().doubleColumn();
        double match5 = randomSavedRecord().doubleColumn();
        Set<Double> matches = new HashSet<>(Arrays.asList(match1, match2, match3, match4, match5));

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byDoubleColumn(match1, match2, match3, match4, match5)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> matches.contains(doubleColOf(asr)));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by double_column from several match criteria using OR")
    @Disabled("floating point equivalence working out differently than expected after being serialized")    // <-- TODO: fix by using fuzz-factor for floating point values
    public void shouldFindRecordWithDoubleColumnInExactMatchCriteriaUsingOR() {
        double match1 = randomSavedRecord().doubleColumn();
        double match2 = randomSavedRecord().doubleColumn();
        double match3 = randomSavedRecord().doubleColumn();
        double match4 = randomSavedRecord().doubleColumn();
        double match5 = randomSavedRecord().doubleColumn();
        Set<Double> matches = new HashSet<>(Arrays.asList(match1, match2, match3, match4, match5));

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byDoubleColumn(match1)
                            .or().byDoubleColumn(match2)
                            .or().byDoubleColumn(match3)
                            .or().byDoubleColumn(match4)
                            .or().byDoubleColumn(match5)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> matches.contains(doubleColOf(asr)));

        assertListEquals(expected, actual);
    }

    // FIND by single double_wrapper_column

    @Test
    @DisplayName("finding by exact double_wrapper_column")
    @Disabled("floating point equivalence working out differently than expected after being serialized")    // <-- TODO: fix by using fuzz-factor for floating point values
    public void shouldFindRecordsByExactDoubleWrapperColumn() {
        final Double exactMatch = randomSavedRecord().doubleWrapperColumn();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byDoubleWrapperColumn(exactMatch)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asp -> doubleWrapperColOf(asp).equals(exactMatch));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by double_wrapper_column between clopen range [)")
    public void shouldFindRecordsByDoubleWrapperColumnBetweenClopenLowerInclusive() {
        Pair<Double, Double> range = randomRange(TestUtil::randomDouble);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byDoubleWrapperColumnBetweenInclusive(range.first).and(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(doubleWrapperColBetween(range, true, false));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by double_wrapper_column between open range ()")
    public void shouldFindRecordWithDoubleWrapperColumnBetweenOpenRange() {
        Pair<Double, Double> range = randomRange(TestUtil::randomDouble);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byDoubleWrapperColumnBetween(range.first).and(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(doubleWrapperColBetween(range, false, false));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by double_wrapper_column between closed range []")
    public void shouldFindRecordWithDoubleWrapperColumnBetweenClosedRange() {
        Pair<Double, Double> range = randomRange(TestUtil::randomDouble);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byDoubleWrapperColumnBetweenInclusive(range.first).andInclusive(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(doubleWrapperColBetween(range, true, true));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by double_wrapper_column between clopen range (]")
    public void shouldFindRecordWithDoubleWrapperColumnBetweenClopenRangeUpperInclusive() {
        Pair<Double, Double> range = randomRange(TestUtil::randomDouble);

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byDoubleWrapperColumnBetween(range.first).andInclusive(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(doubleWrapperColBetween(range, false, true));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by double_wrapper_column >=")
    public void shouldFindRecordWithDoubleWrapperColumnGE() {
        final Double lowerInclusiveBound = randomDouble();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byDoubleWrapperColumnGreaterThanInclusive(lowerInclusiveBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> doubleWrapperColOf(asr).compareTo(lowerInclusiveBound) >= 0);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by double_wrapper_column >")
    public void shouldFindRecordWithDoubleWrapperColumnGT() {
        final Double exclusiveLowerBound = randomDouble();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byDoubleWrapperColumnGreaterThan(exclusiveLowerBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> filteredSavedRecords = savedRecordsWhere(asr -> doubleWrapperColOf(asr).compareTo(exclusiveLowerBound) > 0);

        assertListEquals(filteredSavedRecords, actual);
    }

    @Test
    @DisplayName("finding by double_wrapper_column <=")
    public void shouldFindRecordWithDoubleWrapperColumnLE() {
        final Double inclusiveUpperBound = randomDouble();

        List<AllTypesTable.Record> returnedList = retrieveToList(
                allTypesTable().find()
                        .byDoubleWrapperColumnLessThanInclusive(inclusiveUpperBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> doubleWrapperColOf(asr).compareTo(inclusiveUpperBound) <= 0);

        assertListEquals(expected, returnedList);
    }

    @Test
    @DisplayName("finding by double_wrapper_column <")
    public void shouldFindRecordWithDoubleWrapperColumnLT() {
        final Double exclusiveUpperBound = randomDouble();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byDoubleWrapperColumnLessThan(exclusiveUpperBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> doubleWrapperColOf(asr).compareTo(exclusiveUpperBound) < 0);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by double_wrapper_column NOT")
    @Disabled("floating point equivalence working out differently than expected after being serialized")    // <-- TODO: fix by using fuzz-factor for floating point values
    public void shouldFindRecordWithDoubleWrapperColumnNOT() {
        final Double exclusion = randomSavedRecord().doubleWrapperColumn();

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byDoubleWrapperColumnNot(exclusion)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> !doubleWrapperColOf(asr).equals(exclusion));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by double_wrapper_column from several match criteria")
    @Disabled("floating point equivalence working out differently than expected after being serialized")    // <-- TODO: fix by using fuzz-factor for floating point values
    public void shouldFindRecordWithDoubleWrapperColumnInExactMatchCriteria() {
        Double match1 = randomSavedRecord().doubleWrapperColumn();
        Double match2 = randomSavedRecord().doubleWrapperColumn();
        Double match3 = randomSavedRecord().doubleWrapperColumn();
        Double match4 = randomSavedRecord().doubleWrapperColumn();
        Double match5 = randomSavedRecord().doubleWrapperColumn();
        Set<Double> matches = new HashSet<>(Arrays.asList(match1, match2, match3, match4, match5));

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable().find()
                        .byDoubleWrapperColumn(match1, match2, match3, match4, match5)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> matches.contains(doubleWrapperColOf(asr)));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by double_wrapper_column from several match criteria using OR")
    @Disabled("floating point equivalence working out differently than expected after being serialized")    // <-- TODO: fix by using fuzz-factor for floating point values
    public void shouldFindRecordWithDoubleWrapperColumnInExactMatchCriteriaUsingOR() {
        Double match1 = randomSavedRecord().doubleWrapperColumn();
        Double match2 = randomSavedRecord().doubleWrapperColumn();
        Double match3 = randomSavedRecord().doubleWrapperColumn();
        Double match4 = randomSavedRecord().doubleWrapperColumn();
        Double match5 = randomSavedRecord().doubleWrapperColumn();
        Set<Double> matches = new HashSet<>(Arrays.asList(match1, match2, match3, match4, match5));

        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byDoubleWrapperColumn(match1)
                        .or().byDoubleWrapperColumn(match2)
                        .or().byDoubleWrapperColumn(match3)
                        .or().byDoubleWrapperColumn(match4)
                        .or().byDoubleWrapperColumn(match5)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> matches.contains(doubleWrapperColOf(asr)));

        assertListEquals(expected, actual);
    }

    // FIND by single boolean_column

    @Test
    @DisplayName("finding by boolean_column true")
    public void shouldFindRecordsByBooleanColumnTrue() {
        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byBooleanColumn()
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(AllTypesTableTestUtil::booleanColOf);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by exact boolean_column false")
    public void shouldFindRecordsByBooleanColumnFalse() {
        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byNotBooleanColumn()
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asp -> !booleanColOf(asp));

        assertListEquals(expected, actual);
    }

    // FIND by single boolean_wrapper_column

    @Test
    @DisplayName("finding by boolean_wrapper_column true")
    public void shouldFindRecordsByBooleanWrapperColumnTrue() {
        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byBooleanWrapperColumn()
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(AllTypesTableTestUtil::booleanWrapperColOf);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by exact boolean_wrapper_column false")
    public void shouldFindRecordsByBooleanWrapperColumnFalse() {
        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byNotBooleanWrapperColumn()
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asp -> !booleanWrapperColOf(asp));

        assertListEquals(expected, actual);
    }

    // FIND by single date_column

    @Test
    @DisplayName("finding by date_column exact match")
    public void shouldFindRecordsByOnDateColumn() {
        Date exactMatch = randomSavedRecord().dateColumn();
        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byDateColumnOn(exactMatch)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> dateColOf(asr).equals(exactMatch));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by date_column NOT")
    public void shouldFindRecordsByNotOnDateColumn() {
        Date exclusion = randomSavedRecord().dateColumn();
        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byNotDateColumnOn(exclusion)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> !dateColOf(asr).equals(exclusion));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by date_column BEFORE")
    public void shouldFindRecordsByDateColumnBefore() {
        Date upperExclusiveBound = randomSavedRecord().dateColumn();
        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byDateColumnBefore(upperExclusiveBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> dateColOf(asr).compareTo(upperExclusiveBound) < 0);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by date_column AFTER")
    public void shouldFindRecordsByDateColumnAfter() {
        Date lowerExclusiveBound = randomSavedRecord().dateColumn();
        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byDateColumnAfter(lowerExclusiveBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> dateColOf(asr).compareTo(lowerExclusiveBound) > 0);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by date_column BEFORE or ON")
    public void shouldFindRecordsByDateColumnBeforeOrOn() {
        Date upperInclusiveBound = randomSavedRecord().dateColumn();
        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byDateColumnBeforeInclusive(upperInclusiveBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> dateColOf(asr).compareTo(upperInclusiveBound) <= 0);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by date_column AFTER or ON")
    public void shouldFindRecordsByDateColumnAfterOrOn() {
        Date lowerInclusiveBound = randomSavedRecord().dateColumn();
        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byDateColumnAfterInclusive(lowerInclusiveBound)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(asr -> dateColOf(asr).compareTo(lowerInclusiveBound) >= 0);

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by date_column in open range")
    public void shouldFindRecordsByDateColumnInOpenRange() {
        Pair<Date, Date> range = randomRange(TestUtil::randomDate);
        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byDateColumnBetween(range.first).and(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(dateColBetween(range, false, false));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by date_column in clopen range [)")
    public void shouldFindRecordsByDateColumnInClopenRangeLowerInclusive() {
        Pair<Date, Date> range = randomRange(TestUtil::randomDate);
        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byDateColumnBetweenInclusive(range.first).and(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(dateColBetween(range, true, false));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by date_column in clopen range (]")
    public void shouldFindRecordsByDateColumnInClopenRangeUpperInclusive() {
        Pair<Date, Date> range = randomRange(TestUtil::randomDate);
        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byDateColumnBetween(range.first).andInclusive(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(dateColBetween(range, false, true));

        assertListEquals(expected, actual);
    }

    @Test
    @DisplayName("finding by date_column in closed range")
    public void shouldFindRecordsByDateColumnInClosedRange() {
        Pair<Date, Date> range = randomRange(TestUtil::randomDate);
        List<AllTypesTable.Record> actual = retrieveToList(
                allTypesTable()
                        .find().byDateColumnBetweenInclusive(range.first).andInclusive(range.second)
                        .then()
                        .order().byId(OrderBy.ORDER_ASC)
                        .then()
                        .get()
        );
        List<AllTypesTable.Record> expected = savedRecordsWhere(dateColBetween(range, true, true));

        assertListEquals(expected, actual);
    }

    // TODO: find by blob, BigDecimal, BigInteger
    // TODO: find by multiple parameters with a single AND
    // TODO: find by multiple parameters with multiple ANDs
    // TODO: find by multiple parameters with a single OR
    // TODO: find by mutliple parameters with multiple ORs
    // TODO: find by multiple parameters with an OR and AND
    // TODO: find by multiple parameters with an AND and OR
    // TODO: find by multiple parameters with multiple AND and ORs

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

    private static Pair<String, String> randomStoredStringColumnRange() {
        return randomRange(() -> randomSavedRecord().stringColumn());
    }

    private static Pair<Long, Long> randomIdRange() {
        return randomRange(RetrievalTests::randomStoredRecordId);
    }

    private static Predicate<AttemptedSavePair<AllTypesTable.Record>> stringColBetween(Pair<String, String> range, boolean lowerInclusive, boolean upperInclusive) {
        return isBetween(range, lowerInclusive, upperInclusive, AllTypesTableTestUtil::stringColOf);
    }

    private static Predicate<AttemptedSavePair<AllTypesTable.Record>> intColBetween(Pair<Integer, Integer> range, boolean lowerInclusive, boolean upperInclusive) {
        return isBetween(range, lowerInclusive, upperInclusive, AllTypesTableTestUtil::intColOf);
    }

    private static Predicate<AttemptedSavePair<AllTypesTable.Record>> integerWrapperColBetween(Pair<Integer, Integer> range, boolean lowerInclusive, boolean upperInclusive) {
        return isBetween(range, lowerInclusive, upperInclusive, AllTypesTableTestUtil::integerWrapperColOf);
    }

    private static Predicate<AttemptedSavePair<AllTypesTable.Record>> idBetween(Pair<Long, Long> range, boolean lowerInclusive, boolean upperInclusive) {
        return isBetween(range, lowerInclusive, upperInclusive, AllTypesTableTestUtil::idOf);
    }

    private static Predicate<AttemptedSavePair<AllTypesTable.Record>> longWrapperColBetween(Pair<Long, Long> range, boolean lowerInclusive, boolean upperInclusive) {
        return isBetween(range, lowerInclusive, upperInclusive, AllTypesTableTestUtil::longWrapperColOf);
    }

    private static Predicate<AttemptedSavePair<AllTypesTable.Record>> floatColBetween(Pair<Float, Float> range, boolean lowerInclusive, boolean upperInclusive) {
        return isBetween(range, lowerInclusive, upperInclusive, AllTypesTableTestUtil::floatColOf);
    }

    private static Predicate<AttemptedSavePair<AllTypesTable.Record>> floatWrapperColBetween(Pair<Float, Float> range, boolean lowerInclusive, boolean upperInclusive) {
        return isBetween(range, lowerInclusive, upperInclusive, AllTypesTableTestUtil::floatWrapperColOf);
    }

    private static Predicate<AttemptedSavePair<AllTypesTable.Record>> doubleColBetween(Pair<Double, Double> range, boolean lowerInclusive, boolean upperInclusive) {
        return isBetween(range, lowerInclusive, upperInclusive, AllTypesTableTestUtil::doubleColOf);
    }

    private static Predicate<AttemptedSavePair<AllTypesTable.Record>> doubleWrapperColBetween(Pair<Double, Double> range, boolean lowerInclusive, boolean upperInclusive) {
        return isBetween(range, lowerInclusive, upperInclusive, AllTypesTableTestUtil::doubleWrapperColOf);
    }

    private static Predicate<AttemptedSavePair<AllTypesTable.Record>> dateColBetween(Pair<Date, Date> range, boolean lowerInclusive, boolean upperInclusive) {
        return isBetween(range, lowerInclusive, upperInclusive, AllTypesTableTestUtil::dateColOf);
    }
}
