package com.fsryan.forsuredb;

import java.sql.Connection;
import java.sql.SQLException;

public interface DBConfigurer {
    int discoverVersion(Connection db) throws SQLException;
    boolean setVersion(Connection db, int version) throws SQLException;
    boolean mustEnableForeignKeys();
    void enableForeignKeys(Connection db) throws SQLException;
}
