package com.fsryan.forsuredb;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class AbstractDBOpener {

    private Connection db;
    private boolean mIsInitializing;

    @Nonnull private final String jdbcUrl;
    @Nonnull private final Properties connectionProps;
    @Nonnull
    protected final DBConfigurer dbConfigurer;
    private final int newVersion;

    /**
     * <p>
     * @param jdbcUrl
     * @param connectionProps
     * @param versionUpdater
     * @param newVersion
     */
    public AbstractDBOpener(@Nonnull String jdbcUrl,
                            @Nullable Properties connectionProps,
                            @Nonnull DBConfigurer versionUpdater,
                            int newVersion) {
        if (newVersion < 1) {
            throw new IllegalArgumentException("Version must be >= 1, was " + newVersion);
        }
        this.jdbcUrl = jdbcUrl;
        this.connectionProps = connectionProps == null ? new Properties() : connectionProps;
        this.dbConfigurer = versionUpdater;
        this.newVersion = newVersion;
    }

    Connection getDatabaseLocked(boolean writable) throws SQLException {
        if (db != null) {
            try {
                if (db.isClosed()) {
                    db = null;
                } else if (!writable || !db.isReadOnly()) {
                    // The database is already open for business.
                    return db;
                }
            } catch (SQLException sqle) {
                throw new RuntimeException(sqle);
            }
        }

        if (mIsInitializing) {
            throw new IllegalStateException("getDatabase called recursively");
        }

        Connection tempDb = this.db;
        try {
            mIsInitializing = true;
            if (tempDb != null) {
                if (writable && tempDb.isReadOnly()) {
                    tempDb.setReadOnly(false);
                }
            } else if (jdbcUrl == null) {
                throw new IllegalStateException("Cannot open database connection without jdbcUrl");
            } else {

                try {
                    tempDb = DriverManager.getConnection(jdbcUrl, connectionProps);
                    // TODO: determine whether this is necessary
                    // reset file permissions to 660
//                    setFilePermissionsForDb(db);
                } catch (SQLException sqle) {
                    if (writable) {
                        throw sqle;
                    }

                    // TODO: some kind of logging showing that we're trying to open up read-only connection
                    tempDb = DriverManager.getConnection(jdbcUrl, connectionPropsWithReadOnly());
                }
            }

            onConfigure(tempDb);

            final int version = dbConfigurer.discoverVersion(tempDb);
            if (version != newVersion) {
                if (tempDb.isReadOnly()) {
                    String m = String.format("Can't use read-only connection to upgrade from version %d to %d; jdbcUrl: %s", version, newVersion, jdbcUrl);
                    throw new SQLException(m);
                }
                boolean prevAutoCommit = tempDb.getAutoCommit();
                if (prevAutoCommit) {
                    tempDb.setAutoCommit(false);
                }
                try {
                    if (version == 0) {
                        onCreate(tempDb);
                    } else {
                        if (version > newVersion) {
                            onDowngrade(tempDb, version, newVersion);
                        } else {
                            onUpgrade(tempDb, version, newVersion);
                        }
                    }
                    dbConfigurer.setVersion(tempDb, newVersion);
                    tempDb.commit();
                } catch (SQLException sqle) {
                    tempDb.rollback();
                    throw sqle;
                } finally {
                    if (prevAutoCommit) {
                        tempDb.setAutoCommit(true);
                    }
                }
            }

            onOpen(tempDb);
//            if (tempDb.isReadOnly()) {
//                // TODO: logging
//            }
            this.db = tempDb;
            return tempDb;
        } finally {
            mIsInitializing = false;
            if (tempDb != null && tempDb != this.db) {
                tempDb.close();
            }
        }
    }

    /**
     * <p>Called before {@link #onCreate}, {@link #onUpgrade(Connection, int, int)},
     * {@link #onDowngrade(Connection, int, int)}, and {@link #onOpen(Connection)}.
     * Don't modify the database here--just configure the database connection
     * @param db the {@link Connection} to the database
     * @throws SQLException that could be thrown when using the {@link Connection}
     */
    public void onConfigure(Connection db) throws SQLException {
        // empty implementation--override if necessary
    }

    /**
     * <p>Called before {@link #onUpgrade(Connection, int, int)},
     * {@link #onDowngrade(Connection, int, int)}, and {@link #onOpen(Connection)}
     * @param db the {@link Connection} to the database
     * @throws SQLException that could be thrown when using the {@link Connection}
     */
    public void onCreate(Connection db) throws SQLException {
        // empty implementation--override if necessary
    }

    /**
     * <p>Called when the discovered version is <i>GREATER THAN</i> the {@link #newVersion}
     * @param db the {@link Connection} to the database
     * @param version the existing version
     * @param newVersion the version to migrate to
     * @throws SQLException that could be thrown when using the {@link Connection}
     */
    public void onDowngrade(Connection db, int version, int newVersion) throws SQLException {
        // empty implementation--override if necessary
    }

    /**
     * <p>Called when the discovered version is <i>LESS THAN</i> the {@link #newVersion}
     * @param db the {@link Connection} to the database
     * @param version the existing version
     * @param newVersion the version to migrate to
     * @throws SQLException that could be thrown when using the {@link Connection}
     */
    public void onUpgrade(Connection db, int version, int newVersion) throws SQLException {
        // empty implementation--override if necessary
    }

    /**
     * <p>Called when the database connection has been opened. You should check whether the
     * database {@link Connection#isReadOnly()} prior to performing any write operations
     * @param db the {@link Connection} to the database
     * @throws SQLException that could be thrown when using the {@link Connection}
     */
    public void onOpen(Connection db) throws SQLException {
        // empty implementation--override if necessary
    }

    private Properties connectionPropsWithReadOnly() {
        if (connectionProps.getProperty("open_mode", "0").equals("1")) {   // <-- 1 -> read only
            return connectionProps;
        }
        Properties wrapped = new Properties(connectionProps);
        wrapped.setProperty("open_mode", "1");
        return wrapped;
    }
}
