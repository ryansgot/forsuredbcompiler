package com.fsyran.forsuredb.integrationtest;

import com.fsryan.forsuredb.queryable.StatementBinder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        triggerExists(c, tableName, "updated");
    }

    public static void nonUniqueIndexExists(Connection c, String tableName, String columnName) throws SQLException {
        indexExists(c, tableName, columnName, false);
    }

    public static void uniqueIndexExists(Connection c, String tableName, String columnName) throws SQLException {
        indexExists(c, tableName, columnName, true);
    }

    public static void indexExists(Connection c, String tableName, String columnName, boolean unique) throws SQLException {
        final String expectedSql = "CREATE " + (unique ? "UNIQUE " : "") + "INDEX " + tableName + '_' + columnName + " ON " + tableName + '(' + columnName + ')';
        ResultSet rs = null;
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM SQLITE_MASTER WHERE type = ? AND tbl_name = ? AND name = ?")) {
            StatementBinder.bindObject(1, ps,"index");
            StatementBinder.bindObject(2, ps, tableName);
            StatementBinder.bindObject(3, ps, tableName + "_" + columnName);

            rs = ps.executeQuery();

            assertTrue(rs.next());
            final String actualSql = rs.getString("sql");
            assertEquals(expectedSql, actualSql);
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }

    public static void triggerExists(Connection c, String tableName, String columnName) throws SQLException {
        String expectedSql = "CREATE TRIGGER " + tableName + '_' + columnName + '_' + "trigger AFTER UPDATE ON " + tableName + " BEGIN UPDATE " + tableName + " SET modified=STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW') WHERE _id=NEW._id; END";
        ResultSet rs = null;
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM SQLITE_MASTER WHERE type = ? AND tbl_name = ? AND name = ?")) {
            StatementBinder.bindObject(1, ps,"trigger");
            StatementBinder.bindObject(2, ps, tableName);
            StatementBinder.bindObject(3, ps, tableName + "_" + columnName + "_trigger");

            rs = ps.executeQuery();

            assertTrue(rs.next());
            String actualSql = rs.getString("sql");
            assertEquals(expectedSql, actualSql);
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
    }
}
