package com.fsryan.forsuredb;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class ConnectionUtil {

    /**
     * <p>Sets autocommit to false if necessary and returns whether the db
     * previously had autocommit set
     * @param db the {@link Connection} on which to turn autocommit off
     */
    public static boolean ensureNotAutoCommit(Connection db) throws SQLException {
        if (db == null) {
            return false;
        }
        boolean wasAutoCommit = db.getAutoCommit();
        if (wasAutoCommit) {
            db.setAutoCommit(false);
        }
        return wasAutoCommit;
    }
}
