package com.fsyran.forsuredb.integrationtest.singletable;

import com.fsryan.forsuredb.api.OrderBy;
import com.fsryan.forsuredb.integrationtest.singletable.AllTypesTable;
import com.fsyran.forsuredb.integrationtest.DBSetup;
import com.fsyran.forsuredb.integrationtest.ExecutionLog;
import com.fsyran.forsuredb.integrationtest.Pair;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static com.fsryan.forsuredb.integrationtest.ForSure.allTypesTable;
import static com.fsyran.forsuredb.integrationtest.MoreAssertions.assertAscending;
import static com.fsyran.forsuredb.integrationtest.MoreAssertions.assertDescending;
import static com.fsyran.forsuredb.integrationtest.TestUtil.MEMCMP_COMPARATOR;
import static com.fsyran.forsuredb.integrationtest.singletable.AllTypesTableTestUtil.*;
import static java.util.stream.Collectors.toList;

@ExtendWith({DBSetup.class, EnsureAllTypesTableEmptyBeforeClass.class, ExecutionLog.class})
public class RetrieveSortingTest {

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

    @BeforeAll
    public static void insert128RandomRecords() {
        insertRandomRecords(128, 1L);
    }

    // ONE COLUMN

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

    // TWO COLUMNS: boolean column will have a fair number of duplicates, so it's ideal to use as the first sorting criteria for the tests

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
}
