package com.fsyran.forsuredb.integrationtest;

import com.fsryan.forsuredb.queryable.StatementBinder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class SqlMasterAssertions {

    public static void tableExists(Connection c, String tableName) throws SQLException {
        ResultSet rs = null;
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM SQLITE_MASTER WHERE type = ? AND tbl_name = ?")) {
            StatementBinder.bindObject(1, ps,"table");
            StatementBinder.bindObject(2, ps, tableName);
            rs = ps.executeQuery();
            assertTrue(rs.next());
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    public static void nonUniqueIndexExists(Connection c, String tableName, String columnName) throws SQLException {
        indexExists(c, tableName, columnName, false);
    }

    public static void uniqueIndexExists(Connection c, String tableName, String columnName) throws SQLException {
        indexExists(c, tableName, columnName, true);
    }

    public static void indexExists(Connection c, String tableName, String columnName, boolean unique) throws SQLException {
        ResultSet rs = null;
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM SQLITE_MASTER WHERE type = ? AND tbl_name = ? AND name = ?")) {
            StatementBinder.bindObject(1, ps,"index");
            StatementBinder.bindObject(2, ps, tableName);
            StatementBinder.bindObject(3, ps, tableName + "_" + columnName);

            rs = ps.executeQuery();
            assertTrue(rs.next());

            final String sql = rs.getString("sql");
            if (unique) {
                assertTrue(sql.startsWith("CREATE UNIQUE INDEX"));
            } else {
                assertTrue(sql.startsWith("CREATE INDEX"));
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }
}
