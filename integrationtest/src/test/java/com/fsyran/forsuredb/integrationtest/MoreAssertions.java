package com.fsyran.forsuredb.integrationtest;

import com.fsryan.forsuredb.api.SaveResult;
import com.fsryan.forsuredb.queryable.DirectLocator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class MoreAssertions {
    public static void assertSuccessfulInsertion(SaveResult<DirectLocator> result, String table, long id) {
        assertNotNull(result);
        assertEquals(1, result.rowsAffected());
        assertNotNull(result.inserted());
        assertEquals(id, result.inserted().id);
        assertEquals(table, result.inserted().table);
        assertNull(result.exception());
    }

    public static void assertSuccessfulUpdate(SaveResult<DirectLocator> result, int rowsAffected) {
        assertNotNull(result);
        assertEquals(rowsAffected, result.rowsAffected());
        assertNull(result.exception());
        assertNull(result.inserted());
    }
}
