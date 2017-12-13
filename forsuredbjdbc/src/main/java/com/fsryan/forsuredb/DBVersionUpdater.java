package com.fsryan.forsuredb;

import java.sql.Connection;
import java.sql.SQLException;

public interface DBVersionUpdater {
    int discoverVersion(Connection db) throws SQLException;
    boolean setVersion(Connection db, int version) throws SQLException;
}
