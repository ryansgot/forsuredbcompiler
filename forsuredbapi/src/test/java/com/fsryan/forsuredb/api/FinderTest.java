package com.fsryan.forsuredb.api;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public abstract class FinderTest {

    @Mock
    protected Resolver mockResolver;

    @Before
    public void setUpResolver() {
        MockitoAnnotations.initMocks(this);
    }
}
