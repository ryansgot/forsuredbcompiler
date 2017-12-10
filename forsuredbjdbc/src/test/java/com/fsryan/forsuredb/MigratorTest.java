package com.fsryan.forsuredb;

import com.fsryan.forsuredb.migration.MigrationSet;
import com.fsryan.forsuredb.serialization.FSDbInfoSerializer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MigratorTest {
    @Mock
    private FSDbInfoSerializer mockSerializer;
    @Mock
    private MigrationSet mockMigrationSet1;
    @Mock
    private MigrationSet mockMigrationSet2;

    @Before
    public void setUpMocks() {
        MockitoAnnotations.initMocks(this);
        when(mockSerializer.deserializeMigrationSet(any(InputStream.class)))
                .thenReturn(mockMigrationSet1)
                .thenReturn(mockMigrationSet2);
    }

    @Test
    public void should() {
        List<MigrationSet> actual = new Migrator(mockSerializer).getMigrationSets();
        verify(mockSerializer, times(2)).deserializeMigrationSet(any(InputStream.class));
        assertEquals(Arrays.asList(mockMigrationSet1, mockMigrationSet2), actual);
    }
}
