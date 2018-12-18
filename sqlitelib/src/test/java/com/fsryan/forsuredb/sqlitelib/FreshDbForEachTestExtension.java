package com.fsryan.forsuredb.sqlitelib;

import org.junit.jupiter.api.extension.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Savepoint;

/**
 * <p>Creates a reusable in-memory database that rolls back after each test of
 * a class. Furthermore, it supplies the {@link Connection} to each test that
 * needs to apply SQL.
 */
public class FreshDbForEachTestExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private Connection conn;

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        conn.prepareStatement("PRAGMA foreign_keys = ON;").execute();
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (conn != null) {
            conn.close();
            conn = null;
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == Connection.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return conn;
    }
}
