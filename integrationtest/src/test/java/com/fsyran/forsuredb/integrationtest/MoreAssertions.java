package com.fsyran.forsuredb.integrationtest;

import com.fsryan.forsuredb.FSDBHelper;
import com.fsryan.forsuredb.api.SaveResult;
import com.fsryan.forsuredb.integrationtest.singletable.AllTypesTable;
import com.fsryan.forsuredb.queryable.DirectLocator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

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

    @SuppressWarnings("unchecked")
    public static <T> void assertDescending(List<T> list) {
        assertDescending(list, (Comparator<T>) Comparator.naturalOrder());
    }

    public static <T> void assertDescending(List<T> list, Comparator<T> comparator) {
        assertAscending(list, comparator.reversed());
    }

    @SuppressWarnings("unchecked")
    public static <T> void assertAscending(List<T> list) {
        assertAscending(list, (Comparator<T>) Comparator.naturalOrder());
    }

    public static <T> void assertAscending(List<T> list, Comparator<T> comparator) {
        if (list.size() < 2) {
            return;
        }

        for (int i = 0; i < list.size() - 1; i ++) {
            final T lesser = list.get(i);
            final T greater = list.get(i + 1);
            if (comparator.compare(lesser, greater) > 0) {
                fail("expected sort " + lesser + " after " + greater);
            }
        }
    }

    public static <T> void assertListEquals(List<T> expectedList, List<T> actualList) {
        IntStream.range(0, expectedList.size())
                .forEach(i -> {
                    final T expected = expectedList.get(i);
                    final T actual = actualList.get(i);
                    if (!expected.equals(actual)) {
                        fail("idx[" + i + "]: expected = " + expected + "; but was = " + actual);
                    }
                });
        assertEquals(expectedList.size(), actualList.size());
    }
}
