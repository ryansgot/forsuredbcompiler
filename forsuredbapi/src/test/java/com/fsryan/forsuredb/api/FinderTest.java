package com.fsryan.forsuredb.api;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.List;

public abstract class FinderTest {

    @Mock
    protected Resolver mockResolver;

    protected Finder finderUnderTest;

    @Before
    public void setUpResolver() {
        MockitoAnnotations.initMocks(this);
        finderUnderTest = new Finder(mockResolver) {};
    }

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
}
