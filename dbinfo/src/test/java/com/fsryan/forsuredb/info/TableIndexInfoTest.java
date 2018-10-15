package com.fsryan.forsuredb.info;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static com.fsryan.forsuredb.test.assertions.AssertCollection.assertMapEquals;
import static com.fsryan.forsuredb.test.tools.CollectionUtil.mapOf;
import static org.junit.Assert.assertEquals;

public abstract class TableIndexInfoTest {

    public static class InstantiationExceptionCases {

        @Test(expected = IllegalArgumentException.class)
        public void shouldThrowWhenColumnsListEmpty() {
            TableIndexInfo.create(true, Collections.<String>emptyList(), Collections.<String>emptyList());
        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldThrowWhenColumnsListDifferentSizeThanColumnSortsList() {
            TableIndexInfo.create(true, Collections.singletonList("col"), Collections.<String>emptyList());
        }
    }

    @RunWith(Parameterized.class)
    public static class ColumnSortOrderMap {

        private final String desc;
        private final TableIndexInfo infoUnderTest;
        private final Map<String, String> expected;

        public ColumnSortOrderMap(String desc, TableIndexInfo infoUnderTest, Map<String, String> expected) {
            this.desc = desc;
            this.infoUnderTest = infoUnderTest;
            this.expected = expected;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {
                            "00: the column sort order map should be created correctly for single column index",
                            TableIndexInfo.create(
                                    true,
                                    Collections.singletonList("c1"),
                                    Collections.singletonList("")
                            ),
                            mapOf("c1", "")
                    },
                    {
                            "01: the column sort order map should be created correctly for composite index",
                            TableIndexInfo.create(
                                    true,
                                    Arrays.asList("c1", "c2", "c3"),
                                    Arrays.asList("", "ASC", "DESC")
                            ),
                            mapOf(
                                    "c1", "",
                                    "c2", "ASC",
                                    "c3", "DESC"
                            )
                    }
            });
        }

        @Test
        public void shouldCorrectlyDetermineColumnSortOrderMap() {
            assertMapEquals(desc, expected, infoUnderTest.columnSortOrderMap());
        }
    }

    @RunWith(Parameterized.class)
    public static class Merge {

        private final TableIndexInfo first;
        private final TableIndexInfo second;
        private final TableIndexInfo expected;

        public Merge(TableIndexInfo first, TableIndexInfo second, TableIndexInfo expected) {
            this.first = first;
            this.second = second;
            this.expected = expected;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {   // 00: unique should end in unique index with correctly merged columns
                            TableIndexInfo.create(
                                    true,
                                    Collections.singletonList("c1"),
                                    Collections.singletonList("")
                            ),
                            TableIndexInfo.create(
                                    true,
                                    Collections.singletonList("c2"),
                                    Collections.singletonList("ASC")
                            ),
                            TableIndexInfo.create(
                                    true,
                                    Arrays.asList("c1", "c2"),
                                    Arrays.asList("", "ASC")
                            )
                    },
                    {   // 01: non-unique should end in non-unique index with correctly merged columns
                            TableIndexInfo.create(
                                    false,
                                    Collections.singletonList("c1"),
                                    Collections.singletonList("")
                            ),
                            TableIndexInfo.create(
                                    false,
                                    Collections.singletonList("c2"),
                                    Collections.singletonList("ASC")
                            ),
                            TableIndexInfo.create(
                                    false,
                                    Arrays.asList("c1", "c2"),
                                    Arrays.asList("", "ASC")
                            )
                    },
                    {   // 02: multiple column merged with multiple column
                            TableIndexInfo.create(
                                    false,
                                    Arrays.asList("c1", "c3"),
                                    Arrays.asList("ASC", "DESC")
                            ),
                            TableIndexInfo.create(
                                    false,
                                    Arrays.asList("c2", "c4"),
                                    Arrays.asList("DESC", "")
                            ),
                            TableIndexInfo.create(
                                    false,
                                    Arrays.asList("c1", "c3", "c2", "c4"),
                                    Arrays.asList("ASC", "DESC", "DESC", "")
                            )
                    }
            });
        }

        @Test
        public void shouldCorrectlyDetermineColumnSortOrderMap() {
            assertEquals(expected, TableIndexInfo.merge(first, second));
        }
    }

    public static class MergeExceptionCase {
        @Test(expected = IllegalArgumentException.class)
        public void shouldThrowWhenMergingUniqueAndNonUnique() {
            TableIndexInfo unique = TableIndexInfo.create(true, Collections.singletonList("c1"), Collections.singletonList(""));
            TableIndexInfo nonUnique = TableIndexInfo.create(false, Collections.singletonList("c2"), Collections.singletonList(""));
            TableIndexInfo.merge(unique, nonUnique);
        }
    }
}
