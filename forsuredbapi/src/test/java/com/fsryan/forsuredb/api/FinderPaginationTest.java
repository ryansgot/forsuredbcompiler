package com.fsryan.forsuredb.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public abstract class FinderPaginationTest extends FinderTest.ForFinder {

    private final int fromTop;
    private final int offsetFromTop;
    private final int fromBottom;
    private final int offsetFromBottom;
    private final int expectedLimit;
    private final int expectedOffset;
    private final boolean expectedFromBottom;

    public FinderPaginationTest(int fromTop, int offsetFromTop, int fromBottom, int offsetFromBottom, int expectedLimit, int expectedOffset, boolean expectedFromBottom) {
        this.fromTop = fromTop;
        this.offsetFromTop = offsetFromTop;
        this.fromBottom = fromBottom;
        this.offsetFromBottom = offsetFromBottom;
        this.expectedLimit = expectedLimit;
        this.expectedOffset = expectedOffset;
        this.expectedFromBottom = expectedFromBottom;
    }

    @Before
    public void setUpFinder() {
        finderUnderTest.first(fromTop, offsetFromTop).last(fromBottom, offsetFromBottom);
    }

    @Test
    public void shouldHaveExpectedLimit() {
        assertEquals(expectedLimit, finderUnderTest.selection().limits().count());
    }

    @Test
    public void shouldHaveExpectedOffset() {
        assertEquals(expectedOffset, finderUnderTest.selection().limits().offset());
    }

    @Test
    public void shouldHaveExpectedFromBottom() {
        assertEquals(expectedFromBottom, finderUnderTest.selection().limits().isBottom());
    }

    @RunWith(Parameterized.class)
    public static class LimitsTest extends FinderPaginationTest {

        public LimitsTest(int fromTop, int offsetFromTop, int fromBottom, int offsetFromBottom, int expectedLimit, int expectedOffset, boolean expectedFromBottom) {
            super(fromTop, offsetFromTop, fromBottom, offsetFromBottom, expectedLimit, expectedOffset, expectedFromBottom);
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {-1, 0, 0, 0, 0, 0, false},     // 00: negative number for first results in zero count
                    {0, 0, -1, 0, 0, 0, false},     // 01: negative number for last results in zero count
                    {-1, 0, -1, 0, 0, 0, false},    // 02: negative number for both first and last results in zero count
                    {1, 0, 0, 0, 1, 0, false},      // 03: positive number for first results in that number offset--last false
                    {0, 0, 1, 0, 1, 0, true},       // 04: positive number for last results in that number offset--formBottom true
                    {0, 1, 0, 0, 0, 0, false},      // 05: positive offset without first results in zero offset
                    {0, 0, 0, 1, 0, 0, false},      // 06: positive offset without last results in zero offset
                    {20, 10, 0, 0, 20, 10, false},  // 07: non-one numbers should be accurate first and offset
                    {0, 0, 20, 10, 20, 10, true},   // 08: non-one numbers should be accurate last and offset
            });
        }
    }

    @RunWith(Parameterized.class)
    public static class Incorporate extends FinderPaginationTest {

        private Finder toIncorporate;

        private final int fromTopToIncorporate;
        private final int offsetFromTopToIncorporate;
        private final int fromBottomToIncorporate;
        private final int offsetFromBottomToIncorporate;

        public Incorporate(int fromTop, int offsetFromTop, int fromBottom, int offsetFromBottom,
                           int fromTopToIncorporate, int offsetFromTopToIncorporate, int fromBottomToIncorporate, int offsetFromBottomToIncorporate,
                           int expectedLimit, int expectedOffset, boolean expectedFromBottom) {
            super(fromTop, offsetFromTop, fromBottom, offsetFromBottom, expectedLimit, expectedOffset, expectedFromBottom);
            this.fromTopToIncorporate = fromTopToIncorporate;
            this.offsetFromTopToIncorporate = offsetFromTopToIncorporate;
            this.fromBottomToIncorporate = fromBottomToIncorporate;
            this.offsetFromBottomToIncorporate = offsetFromBottomToIncorporate;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    // The test cases where the finder to incorporate has all zeroes
                    {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, false},     // 00: negative number for first results in zero count
                    {0, 0, -1, 0, 0, 0, 0, 0, 0, 0, false},     // 01: negative number for last results in zero count
                    {-1, 0, -1, 0, 0, 0, 0, 0, 0, 0, false},    // 02: negative number for both first and last results in zero count
                    {1, 0, 0, 0, 0, 0, 0, 0, 1, 0, false},      // 03: positive number for first results in that number offset--last false
                    {0, 0, 1, 0, 0, 0, 0, 0, 1, 0, true},       // 04: positive number for last results in that number offset--formBottom true
                    {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, false},      // 05: positive offset without first results in zero offset
                    {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, false},      // 06: positive offset without last results in zero offset
                    {20, 10, 0, 0, 0, 0, 0, 0, 20, 10, false},  // 07: non-one numbers should be accurate first and offset
                    {0, 0, 20, 10, 0, 0, 0, 0, 20, 10, true},   // 08: non-one numbers should be accurate last and offset

                    // The reverse cases--where the parent finder has all zeroes
                    {0, 0, 0, 0, -1, 0, 0, 0, 0, 0, false},     // 09: negative number for first results in zero count
                    {0, 0, 0, 0, 0, 0, -1, 0, 0, 0, false},     // 10: negative number for last results in zero count
                    {0, 0, 0, 0, -1, 0, -1, 0, 0, 0, false},    // 11: negative number for both first and last results in zero count
                    {0, 0, 0, 0, 1, 0, 0, 0, 1, 0, false},      // 12: positive number for first results in that number offset--last false
                    {0, 0, 0, 0, 0, 0, 1, 0, 1, 0, true},       // 13: positive number for last results in that number offset--formBottom true
                    {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, false},      // 14: positive offset without first results in zero offset
                    {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, false},      // 15: positive offset without last results in zero offset
                    {0, 0, 0, 0, 20, 10, 0, 0, 20, 10, false},  // 16: non-one numbers should be accurate first and offset
                    {0, 0, 0, 0, 0, 0, 20, 10, 20, 10, true},   // 17: non-one numbers should be accurate last and offset

                    // same offset and count
                    {3, 9, 0, 0, 3, 9, 0, 0, 3, 9, false},      // 18: same offset and first should not throw
                    {0, 0, 4, 5, 0, 0, 4, 5, 4, 5, true},       // 19: same offset and last should not throw

            });
        }

        @Before
        public void setUpFinderToIncorporateAndIncorporate() {
            toIncorporate = new Finder(mockResolver) {}
                    .first(fromTopToIncorporate, offsetFromTopToIncorporate)
                    .last(fromBottomToIncorporate, offsetFromBottomToIncorporate);
            finderUnderTest.incorporate(toIncorporate);
        }
    }

    public static class ExceptionCases extends FinderTest.ForFinder {

        @Test(expected = IllegalStateException.class)
        public void shouldThrowWhenCallingFromTopThenFromBottomWithPositiveIntegers() {
            new Finder(mockResolver) {}.first(1).last(1);
        }

        @Test(expected = IllegalStateException.class)
        public void shouldThrowWhenCallingBottomThenTopPositiveIntegers() {
            new Finder(mockResolver) {}.last(1).first(1);
        }

        @Test(expected = IllegalStateException.class)
        public void shouldThrowWhenIncorporatedFinderSpecifiesFromBottomAndTParentFinderFromTop() {
            Finder parent = new Finder(mockResolver) {}.last(1);
            Finder toIncorporate = new Finder(mockResolver) {}.first(1);
            parent.incorporate(toIncorporate);
        }

        @Test(expected = IllegalStateException.class)
        public void shouldThrowWhenIncorporatedFinderSpecifiesFromTopAndTParentFinderFromBottom() {
            Finder parent = new Finder(mockResolver) {}.first(1);
            Finder toIncorporate = new Finder(mockResolver) {}.last(1);
            parent.incorporate(toIncorporate);
        }

        @Test(expected = IllegalStateException.class)
        public void shouldThrowWhenIncorporatedFinderSpecifiesDifferentPositiveFromTop() {
            Finder parent = new Finder(mockResolver) {}.first(1);
            Finder toIncorporate = new Finder(mockResolver) {}.first(2);
            parent.incorporate(toIncorporate);
        }

        @Test(expected = IllegalStateException.class)
        public void shouldThrowWhenIncorporatedFinderSpecifiesDifferentPositiveFromBottom() {
            Finder parent = new Finder(mockResolver) {}.last(1);
            Finder toIncorporate = new Finder(mockResolver) {}.last(2);
            parent.incorporate(toIncorporate);
        }

        @Test(expected = IllegalStateException.class)
        public void shouldThrowWhenIncorporatedFinderSpecifiesDifferentPositiveOffset() {
            Finder parent = new Finder(mockResolver) {}.last(1, 5);
            Finder toIncorporate = new Finder(mockResolver) {}.last(1, 4);
            parent.incorporate(toIncorporate);
        }
    }
}
