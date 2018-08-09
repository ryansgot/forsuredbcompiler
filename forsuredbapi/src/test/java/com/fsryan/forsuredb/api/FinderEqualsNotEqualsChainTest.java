package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.api.sqlgeneration.DBMSIntegrator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.junit.Assert.assertEquals;

public abstract class FinderEqualsNotEqualsChainTest<F extends Finder, R extends Resolver> extends FinderTest<F, R> {

    static final Date d1 = new Date();
    static final Date d2 = new Date();

    private final List<Finder.WhereElement> expectedEqualsOrElements;
    private final List<Finder.WhereElement> expectedNotEqualsAndElements;
    private final List<Object> expectedReplacements;

    FinderEqualsNotEqualsChainTest(Class<R> resolverClass,
                                   List<Finder.WhereElement> expectedEqualsOrElements,
                                   List<Finder.WhereElement> expectedNotEqualsAndElements,
                                   List<Object> expectedReplacements) {
        super(resolverClass);
        this.expectedEqualsOrElements = expectedEqualsOrElements;
        this.expectedNotEqualsAndElements = expectedNotEqualsAndElements;
        this.expectedReplacements = expectedReplacements;
        assertValidExpectations();
    }

    @Test
    public void shouldCorrectlyAddEqualsObjectsToFinder() {
        callEqualMethod(finderUnderTest);
        assertListEqual(expectedEqualsOrElements, accessWhereElements());
        assertListEqual(expectedReplacements, accessReplacementsList());
    }

    @Test
    public void shouldCorrectlyAddNotEqualsObjectsToFinder() {
        callNotEqualMethod(finderUnderTest);
        assertListEqual(expectedNotEqualsAndElements, accessWhereElements());
        assertListEqual(expectedReplacements, accessReplacementsList());
    }

    protected abstract void callEqualMethod(F finderUnderTest);
    protected abstract void callNotEqualMethod(F finderUnderTest);

    private static <T> void assertListEqual(List<T> expected, List<T> actual) {
        for (int i = 0; i < expected.size(); i++) {
            assertEquals("unequal at index " + i, expected.get(i), actual.get(i));
        }
        assertEquals(expected.size(), actual.size());
    }

    @RunWith(Parameterized.class)
    public static class IdChain extends ForFinder {

        private final long firstId;
        private final long[] subsequentIds;

        public IdChain(long firstId,
                       Object[] subsequentIds,
                       List<Finder.WhereElement> expectedEqualsOrElements,
                       List<Finder.WhereElement> expectedNotEqualsAndElements,
                       List<Object> expectedReplacements) {
            super(expectedEqualsOrElements, expectedNotEqualsAndElements, expectedReplacements);
            this.firstId = firstId;
            if (subsequentIds == null) {
                this.subsequentIds = null;
            } else {
                this.subsequentIds = new long[subsequentIds.length];
                for (int i = 0; i < subsequentIds.length; i++) {
                    this.subsequentIds[i] = (Long) subsequentIds[i];
                }
            }
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return testInput("_id", 5L, 6L, 7L, 8L, 9L);
        }

        @Override
        protected void callEqualMethod(Finder finderUnderTest) {
            finderUnderTest.byId(firstId, subsequentIds);
        }

        @Override
        protected void callNotEqualMethod(Finder finderUnderTest) {
            finderUnderTest.byIdNot(firstId, subsequentIds);
        }
    }

    @RunWith(Parameterized.class)
    public static class CreatedChain extends ForFinder {

        private final Date firstDate;
        private final Date[] subsequentDates;

        public CreatedChain(Date firstDate,
                            Object[] subsequentDates,
                            List<Finder.WhereElement> expectedEqualsOrElements,
                            List<Finder.WhereElement> expectedNotEqualsAndElements,
                            List<Object> expectedReplacements) {
            super(expectedEqualsOrElements, expectedNotEqualsAndElements, expectedReplacements);
            this.firstDate = firstDate;
            this.subsequentDates = subsequentDates == null
                    ? null
                    : Arrays.copyOf(subsequentDates, subsequentDates.length, Date[].class);
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return testInput("created", d1, d2);
        }

        @Override
        protected void callEqualMethod(Finder finderUnderTest) {
            finderUnderTest.byCreatedOn(firstDate, subsequentDates);
        }

        @Override
        protected void callNotEqualMethod(Finder finderUnderTest) {
            finderUnderTest.byNotCreatedOn(firstDate, subsequentDates);
        }
    }

    @RunWith(Parameterized.class)
    public static class ModifiedChain extends ForFinder {

        private final Date firstDate;
        private final Date[] subsequentDates;

        public ModifiedChain(Date firstDate,
                             Object[] subsequentDates,
                             List<Finder.WhereElement> expectedEqualsOrElements,
                             List<Finder.WhereElement> expectedNotEqualsAndElements,
                             List<Object> expectedReplacements) {
            super(expectedEqualsOrElements, expectedNotEqualsAndElements, expectedReplacements);
            this.firstDate = firstDate;
            this.subsequentDates = subsequentDates == null
                    ? null
                    : Arrays.copyOf(subsequentDates, subsequentDates.length, Date[].class);
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return testInput("modified", d1, d2);
        }

        @Override
        protected void callEqualMethod(Finder finderUnderTest) {
            finderUnderTest.byModifiedOn(firstDate, subsequentDates);
        }

        @Override
        protected void callNotEqualMethod(Finder finderUnderTest) {
            finderUnderTest.byNotModifiedOn(firstDate, subsequentDates);
        }
    }

    @RunWith(Parameterized.class)
    public static class DocStoreClassChain extends ForDocStoreFinder {

        private final Class firstClass;
        private final Class[] subsequentClasses;

        public DocStoreClassChain(String firstClass,
                                  String[] subsequentClasses,
                                  List<Finder.WhereElement> expectedEqualsOrElements,
                                  List<Finder.WhereElement> expectedNotEqualsAndElements,
                                  List<Object> expectedReplacements) throws Exception {
            super(expectedEqualsOrElements, expectedNotEqualsAndElements, expectedReplacements);
            this.firstClass = Class.forName(firstClass);
            if (subsequentClasses == null) {
                this.subsequentClasses = null;
            } else {
                this.subsequentClasses = new Class[subsequentClasses.length];
                for (int i = 0; i < subsequentClasses.length; i++) {
                    this.subsequentClasses[i] = Class.forName(subsequentClasses[i]);
                }
            }
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return testInputClasses(Long.class, Integer.class);
        }

        @Override
        protected void callEqualMethod(DocStoreFinder finderUnderTest) {
            finderUnderTest.byClass(firstClass, subsequentClasses);
        }

        @Override
        protected void callNotEqualMethod(DocStoreFinder finderUnderTest) {
            finderUnderTest.byClassNot(firstClass, subsequentClasses);
        }
    }

    @RunWith(Parameterized.class)
    public static class DocStoreClassNameChain extends ForDocStoreFinder {

        private final String firstClassName;
        private final String[] subsequentClassNames;

        public DocStoreClassNameChain(String firstClassName,
                                      Object[] subsequentClassNames,
                                      List<Finder.WhereElement> expectedEqualsOrElements,
                                      List<Finder.WhereElement> expectedNotEqualsAndElements,
                                      List<Object> expectedReplacements) throws Exception {
            super(expectedEqualsOrElements, expectedNotEqualsAndElements, expectedReplacements);
            this.firstClassName = firstClassName;
            this.subsequentClassNames = subsequentClassNames == null
                    ? null
                    : Arrays.copyOf(subsequentClassNames, subsequentClassNames.length, String[].class);
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return testInputClasses(Long.class, Integer.class);
        }

        @Override
        protected void callEqualMethod(DocStoreFinder finderUnderTest) {
            finderUnderTest.byClassName(firstClassName, subsequentClassNames);
        }

        @Override
        protected void callNotEqualMethod(DocStoreFinder finderUnderTest) {
            finderUnderTest.byClassNameNot(firstClassName, subsequentClassNames);
        }
    }

    static Iterable<Object[]> testInputClasses(Class first, Class... subsequent) {
        String firstName = first.getName();
        if (subsequent == null) {
            return testInput("class_name", firstName, null);
        }

        String[] subsequentNames = new String[subsequent.length];
        for (int i = 0; i < subsequent.length; i++) {
            subsequentNames[i] = subsequent[i].getName();
        }
        return testInput("class_name", firstName, subsequentNames);
    }

    static Iterable<Object[]> testInput(String column, Object first, Object... subsequent) {
        List<Finder.WhereElement> expectedEqualsOrElements = new ArrayList<>(3 + (subsequent == null ? 0 : subsequent.length) * 2);
        List<Finder.WhereElement> expectedNotEqualsAndElements = new ArrayList<>(3 + (subsequent == null ? 0 : subsequent.length) * 2);
        expectedEqualsOrElements.add(Finder.WhereElement.createGroupStart());
        expectedNotEqualsAndElements.add(Finder.WhereElement.createGroupStart());
        expectedEqualsOrElements.add(Finder.WhereElement.createCondition(column, Finder.OP_EQ, first));
        expectedNotEqualsAndElements.add(Finder.WhereElement.createCondition(column, Finder.OP_NE, first));
        for (Object o : subsequent) {
            expectedEqualsOrElements.add(Finder.WhereElement.createOr());
            expectedEqualsOrElements.add(Finder.WhereElement.createCondition(column, Finder.OP_EQ, o));
            expectedNotEqualsAndElements.add(Finder.WhereElement.createAnd());
            expectedNotEqualsAndElements.add(Finder.WhereElement.createCondition(column, Finder.OP_NE, o));
        }
        expectedEqualsOrElements.add(Finder.WhereElement.createGroupEnd());
        expectedNotEqualsAndElements.add(Finder.WhereElement.createGroupEnd());

        List<Object> expectedReplacements = new ArrayList<>(1 + (subsequent == null ? 0 : subsequent.length));
        expectedReplacements.add(first);
        expectedReplacements.addAll(Arrays.asList(subsequent));

        return Arrays.asList(new Object[][] {
                {   // 00: single element
                        first,
                        null,
                        Arrays.asList(
                                Finder.WhereElement.createGroupStart(),
                                Finder.WhereElement.createCondition(column, Finder.OP_EQ, first),
                                Finder.WhereElement.createGroupEnd()
                        ),
                        Arrays.asList(
                                Finder.WhereElement.createGroupStart(),
                                Finder.WhereElement.createCondition(column, Finder.OP_NE, first),
                                Finder.WhereElement.createGroupEnd()
                        ),
                        Collections.singletonList(first)
                },
                {   // 01: multiple elements
                        first,
                        subsequent,
                        expectedEqualsOrElements,
                        expectedNotEqualsAndElements,
                        expectedReplacements
                }
        });
    }

    private void assertValidExpectations() {
        assertEquals(expectedEqualsOrElements.size(), expectedNotEqualsAndElements.size());
        int countEqualsConditions = 0;
        for (Finder.WhereElement whereElement : expectedEqualsOrElements) {
            if (whereElement.type() == Finder.WhereElement.TYPE_CONDITION) {
                countEqualsConditions++;
            }
        }
        int countNotEqualsConditions = 0;
        for (Finder.WhereElement whereElement : expectedNotEqualsAndElements) {
            if (whereElement.type() == Finder.WhereElement.TYPE_CONDITION) {
                countNotEqualsConditions++;
            }
        }
        assertEquals(countEqualsConditions, countNotEqualsConditions);
        assertEquals(countEqualsConditions, expectedReplacements.size());
    }

    protected static abstract class ForFinder extends FinderEqualsNotEqualsChainTest<Finder, Resolver> {

        protected ForFinder(List<Finder.WhereElement> expectedEqualsOrElements, List<Finder.WhereElement> expectedNotEqualsAndElements, List<Object> expectedReplacements) {
            super(Resolver.class, expectedEqualsOrElements, expectedNotEqualsAndElements, expectedReplacements);
        }

        @Override
        protected Finder createFinder(DBMSIntegrator mockDbmsIntegrator, Resolver mockResolver) {
            return new Finder(mockDbmsIntegrator, mockResolver) {};
        }
    }

    protected static abstract class ForDocStoreFinder extends FinderEqualsNotEqualsChainTest<DocStoreFinder, DocStoreResolver> {

        protected ForDocStoreFinder(List<Finder.WhereElement> expectedEqualsOrElements, List<Finder.WhereElement> expectedNotEqualsAndElements, List<Object> expectedReplacements) {
            super(DocStoreResolver.class, expectedEqualsOrElements, expectedNotEqualsAndElements, expectedReplacements);
        }

        @Override
        protected DocStoreFinder createFinder(DBMSIntegrator mockDbmsIntegrator, DocStoreResolver mockResolver) {
            return new DocStoreFinder(mockDbmsIntegrator, mockResolver) {};
        }
    }
}
