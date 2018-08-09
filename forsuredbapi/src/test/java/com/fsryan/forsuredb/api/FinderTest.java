package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.api.sqlgeneration.DBMSIntegrator;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public abstract class FinderTest<F extends Finder, R extends Resolver> {

    protected DBMSIntegrator mockDbmsIntegrator;
    protected R mockResolver;

    protected F finderUnderTest;

    private final Class<R> resolverClass;

    FinderTest(Class<R> resolverClass) {
        this.resolverClass = resolverClass;
    }

    @Before
    public void setUpResolver() {
        mockDbmsIntegrator = mock(DBMSIntegrator.class);
        mockResolver = mock(resolverClass);
        finderUnderTest = createFinder(mockDbmsIntegrator, mockResolver);

        // returns the object input to test replacements added in correct order
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArgument(1);
            }
        }).when(mockDbmsIntegrator).objectForReplacement(anyInt(), any(Object.class));
    }

    protected abstract <T> T createFinder(DBMSIntegrator mockDbmsIntegrator, R mockResolver);

    protected List<Finder.WhereElement> accessWhereElements() {
        try {
            Field f = Finder.class.getDeclaredField("whereElements");
            f.setAccessible(true);
            return (List<Finder.WhereElement>) f.get(finderUnderTest);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected List<Object> accessReplacementsList() {
        try {
            Field f = Finder.class.getDeclaredField("replacementsList");
            f.setAccessible(true);
            return (List<Object>) f.get(finderUnderTest);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected void addWhereElement(Finder.WhereElement element) {
        accessWhereElements().add(element);
        if (element.type() == Finder.WhereElement.TYPE_CONDITION) {
            accessReplacementsList().add(element.value());
        }
    }

    protected static abstract class ForFinder extends FinderTest<Finder, Resolver> {

        protected ForFinder() {
            super(Resolver.class);
        }

        @Override
        protected Finder createFinder(DBMSIntegrator mockDbmsIntegrator, Resolver mockResolver) {
            return new Finder(mockDbmsIntegrator, mockResolver) {};
        }
    }
}
