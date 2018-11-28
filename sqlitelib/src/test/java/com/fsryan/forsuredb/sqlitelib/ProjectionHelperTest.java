package com.fsryan.forsuredb.sqlitelib;

import com.fsryan.forsuredb.api.FSProjection;
import com.fsryan.forsuredb.api.sqlgeneration.DBMSIntegrator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

// TODO: better testing of the ProjectionHelper class. You are currently testing this indirectly by other tests
@RunWith(Parameterized.class)
public class ProjectionHelperTest {

    @Mock DBMSIntegrator mockSqlGenerator;

    private final List<FSProjection> inputProjections;
    private final String[] expected;

    ProjectionHelper projectionHelperUnderTest;

    public ProjectionHelperTest(List<FSProjection> inputProjections, String[] expected) {
        this.inputProjections = inputProjections;
        this.expected = expected;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {   // 00: handles nulls
                        null,
                        null
                },
                {   // 01: emptyMigrationSet input results in null output
                        Collections.<FSProjection>emptyList(),
                        null
                },
                {   // 02: COUNT projection results in COUNT(*)
                        Collections.singletonList(FSProjection.COUNT),
                        new String[] {"COUNT(*)"}
                }
        });
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(mockSqlGenerator.alwaysUnambiguouslyAliasColumns()).thenReturn(true);
        when(mockSqlGenerator.unambiguousRetrievalColumn(anyString(), anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArgument(0) + "_" + invocation.getArgument(1);
            }
        });

        projectionHelperUnderTest = new ProjectionHelper(mockSqlGenerator);
    }

    @Test
    public void shouldCorrectlyFormatProjectionInputAsList() {
        String[] actual = projectionHelperUnderTest.formatProjection(inputProjections);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void shouldCorrectlyFormatProjectionWithVarargsArray() {
        List<FSProjection> inputProjectionsCopy = inputProjections == null ? null : new ArrayList<>(inputProjections);
        FSProjection initial = inputProjectionsCopy == null || inputProjectionsCopy.isEmpty() ? null : inputProjectionsCopy.remove(0);
        FSProjection[] subsequent = inputProjectionsCopy == null || inputProjectionsCopy.isEmpty() ? new FSProjection[0] : inputProjectionsCopy.toArray(new FSProjection[0]);

        String[] actual = projectionHelperUnderTest.formatProjection(initial, subsequent);
        assertArrayEquals(expected, actual);
    }
}
