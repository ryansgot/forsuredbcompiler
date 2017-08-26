package com.fsryan.forsuredb.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public abstract class FinderPaginationTest extends FinderTest {

    private final int fromTop;
    private final int offsetFromTop;
    private final int fromBottom;
    private final int offsetFromBottom;
    private final int expectedLimit;
    private final int expectedOffset;
    private final boolean expectedFromBottom;

    protected Finder finderUnderTest;

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
        finderUnderTest = new Finder(mockResolver) {}
                .top(fromTop, offsetFromTop)
                .bottom(fromBottom, offsetFromBottom);
    }

    @Test
    public void shouldHaveExpectedLimit() {
        assertEquals(expectedLimit, finderUnderTest.selection().retrieverLimits().limit());
    }

    @Test
    public void shouldHaveExpectedOffset() {
        assertEquals(expectedOffset, finderUnderTest.selection().retrieverLimits().offset());
    }

    @Test
    public void shouldHaveExpectedFromBottom() {
        assertEquals(expectedFromBottom, finderUnderTest.selection().retrieverLimits().fromBottom());
    }

    @RunWith(Parameterized.class)
    public static class RetrieverLimitsTest extends FinderPaginationTest {

        public RetrieverLimitsTest(int fromTop, int offsetFromTop, int fromBottom, int offsetFromBottom, int expectedLimit, int expectedOffset, boolean expectedFromBottom) {
            super(fromTop, offsetFromTop, fromBottom, offsetFromBottom, expectedLimit, expectedOffset, expectedFromBottom);
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {-1, 0, 0, 0, 0, 0, false},     // 00: negative number for top results in zero limit
                    {0, 0, -1, 0, 0, 0, false},     // 01: negative number for bottom results in zero limit
                    {-1, 0, -1, 0, 0, 0, false},    // 02: negative number for both top and bottom results in zero limit
                    {1, 0, 0, 0, 1, 0, false},      // 03: positive number for top results in that number offset--bottom false
                    {0, 0, 1, 0, 1, 0, true},       // 04: positive number for bottom results in that number offset--formBottom true
                    {0, 1, 0, 0, 0, 0, false},      // 05: positive offset without top results in zero offset
                    {0, 0, 0, 1, 0, 0, false},      // 06: positive offset without bottom results in zero offset
                    {20, 10, 0, 0, 20, 10, false},  // 07: non-one numbers should be accurate top and offset
                    {0, 0, 20, 10, 20, 10, true},   // 08: non-one numbers should be accurate bottom and offset
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
                    {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, false},     // 00: negative number for top results in zero limit
                    {0, 0, -1, 0, 0, 0, 0, 0, 0, 0, false},     // 01: negative number for bottom results in zero limit
                    {-1, 0, -1, 0, 0, 0, 0, 0, 0, 0, false},    // 02: negative number for both top and bottom results in zero limit
                    {1, 0, 0, 0, 0, 0, 0, 0, 1, 0, false},      // 03: positive number for top results in that number offset--bottom false
                    {0, 0, 1, 0, 0, 0, 0, 0, 1, 0, true},       // 04: positive number for bottom results in that number offset--formBottom true
                    {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, false},      // 05: positive offset without top results in zero offset
                    {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, false},      // 06: positive offset without bottom results in zero offset
                    {20, 10, 0, 0, 0, 0, 0, 0, 20, 10, false},  // 07: non-one numbers should be accurate top and offset
                    {0, 0, 20, 10, 0, 0, 0, 0, 20, 10, true},   // 08: non-one numbers should be accurate bottom and offset

                    // The reverse cases--where the parent finder has all zeroes
                    {0, 0, 0, 0, -1, 0, 0, 0, 0, 0, false},     // 09: negative number for top results in zero limit
                    {0, 0, 0, 0, 0, 0, -1, 0, 0, 0, false},     // 10: negative number for bottom results in zero limit
                    {0, 0, 0, 0, -1, 0, -1, 0, 0, 0, false},    // 11: negative number for both top and bottom results in zero limit
                    {0, 0, 0, 0, 1, 0, 0, 0, 1, 0, false},      // 12: positive number for top results in that number offset--bottom false
                    {0, 0, 0, 0, 0, 0, 1, 0, 1, 0, true},       // 13: positive number for bottom results in that number offset--formBottom true
                    {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, false},      // 14: positive offset without top results in zero offset
                    {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, false},      // 15: positive offset without bottom results in zero offset
                    {0, 0, 0, 0, 20, 10, 0, 0, 20, 10, false},  // 16: non-one numbers should be accurate top and offset
                    {0, 0, 0, 0, 0, 0, 20, 10, 20, 10, true},   // 17: non-one numbers should be accurate bottom and offset

                    // The reverse cases--where the parent finder has all zeroes
                    {3, 9, 0, 0, 3, 9, 0, 0, 3, 9, false},      // 18: same offset and top should not throw
                    {0, 0, 4, 5, 0, 0, 4, 5, 4, 5, true},       // 19: same offset and bottom should not throw

            });
        }

        @Before
        public void setUpFinderToIncorporateAndIncorporate() {
            toIncorporate = new Finder(mockResolver) {}
                    .top(fromTopToIncorporate, offsetFromTopToIncorporate)
                    .bottom(fromBottomToIncorporate, offsetFromBottomToIncorporate);
            finderUnderTest.incorporate(toIncorporate);
        }
    }

    public static class ExceptionCases extends FinderTest {

        @Test(expected = IllegalStateException.class)
        public void shouldThrowWhenCallingFromTopThenFromBottomWithPositiveIntegers() {
            new Finder(mockResolver) {}.top(1).bottom(1);
        }

        @Test(expected = IllegalStateException.class)
        public void shouldThrowWhenCallingBottomThenTopPositiveIntegers() {
            new Finder(mockResolver) {}.bottom(1).top(1);
        }

        @Test(expected = IllegalStateException.class)
        public void shouldThrowWhenIncorporatedFinderSpecifiesFromBottomAndTParentFinderFromTop() {
            Finder parent = new Finder(mockResolver) {}.bottom(1);
            Finder toIncorporate = new Finder(mockResolver) {}.top(1);
            parent.incorporate(toIncorporate);
        }

        @Test(expected = IllegalStateException.class)
        public void shouldThrowWhenIncorporatedFinderSpecifiesFromTopAndTParentFinderFromBottom() {
            Finder parent = new Finder(mockResolver) {}.top(1);
            Finder toIncorporate = new Finder(mockResolver) {}.bottom(1);
            parent.incorporate(toIncorporate);
        }

        @Test(expected = IllegalStateException.class)
        public void shouldThrowWhenIncorporatedFinderSpecifiesDifferentPositiveFromTop() {
            Finder parent = new Finder(mockResolver) {}.top(1);
            Finder toIncorporate = new Finder(mockResolver) {}.top(2);
            parent.incorporate(toIncorporate);
        }

        @Test(expected = IllegalStateException.class)
        public void shouldThrowWhenIncorporatedFinderSpecifiesDifferentPositiveFromBottom() {
            Finder parent = new Finder(mockResolver) {}.bottom(1);
            Finder toIncorporate = new Finder(mockResolver) {}.bottom(2);
            parent.incorporate(toIncorporate);
        }

        @Test(expected = IllegalStateException.class)
        public void shouldThrowWhenIncorporatedFinderSpecifiesDifferentPositiveOffset() {
            Finder parent = new Finder(mockResolver) {}.bottom(1, 5);
            Finder toIncorporate = new Finder(mockResolver) {}.bottom(1, 4);
            parent.incorporate(toIncorporate);
        }
    }
}
