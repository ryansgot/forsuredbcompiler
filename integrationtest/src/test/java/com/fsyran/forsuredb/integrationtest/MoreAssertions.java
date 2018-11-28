package com.fsyran.forsuredb.integrationtest;

import com.fsryan.forsuredb.FSDBHelper;
import com.fsryan.forsuredb.api.SaveResult;
import com.fsryan.forsuredb.queryable.DirectLocator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

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

    public static void assertCount(String tableName, int count) throws SQLException {
        assertCount(FSDBHelper.inst().getReadableDatabase(), tableName, count);
    }

    public static void assertCount(Connection c, String tableName, int count) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM " + tableName + ';');
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertEquals(count, rs.getInt(1));
        }
    }
}
