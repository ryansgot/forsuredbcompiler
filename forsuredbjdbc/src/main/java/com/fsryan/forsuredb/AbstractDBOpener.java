package com.fsryan.forsuredb;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class AbstractDBOpener {

    private Connection db;
    private boolean mIsInitializing;

    @Nonnull private final String jdbcUrl;
    @Nonnull private final Properties connectionProps;
    private final int newVersion;
    private final int minimumSupportedVersion;

    public AbstractDBOpener(@Nonnull String jdbcUrl, @Nullable Properties connectionProps, int newVersion) {
        this(jdbcUrl, connectionProps, newVersion, 0);
    }

    /**
     * <p>accepts an integer minimumSupportedVersion as a convenience for upgrading very old
     * versions of this database that are no longer supported. If a database with older version that
     * minimumSupportedVersion is found, it is simply deleted and a new database is created with the
     * given name and version
     *
     * @param jdbcUrl the jdbc connection url used to open a connection
     * @param newVersion the required version of the database
     * @param minimumSupportedVersion the minimum version that is supported to be upgraded to
     *            {@code newVersion} via {@link #onUpgrade}. If the current database version is lower
     *            than this, database is simply deleted and recreated with the version passed in
     *            {@code version}. {@link #onBeforeDelete} is called before deleting the database
     *            when this happens. This is 0 by default.
     * @see #onBeforeDelete(Connection)
     * @see #AbstractDBOpener(String, Properties, int)
     * @see #onUpgrade(Connection, int, int)
     */
    public AbstractDBOpener(@Nonnull String jdbcUrl, @Nullable Properties connectionProps, int newVersion, int minimumSupportedVersion) {
        if (newVersion < 1) {
            throw new IllegalArgumentException("Version must be >= 1, was " + newVersion);
        }
        this.jdbcUrl = jdbcUrl;
        this.connectionProps = connectionProps == null ? new Properties() : connectionProps;
        this.newVersion = newVersion;
        this.minimumSupportedVersion = Math.max(0, minimumSupportedVersion);
//        mOpenParamsBuilder.addOpenFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
    }

    protected Connection getDatabaseLocked(boolean writable) {
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
                    // Keep pre-O-MR1 behavior by resetting file permissions to 660
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

            final int version = getVersion(tempDb);
            if (version != newVersion) {
                if (tempDb.isReadOnly()) {
                    String m = String.format("Can't use read-only connection to upgrade from version %d to %d; jdbcUrl: %s", version, newVersion, jdbcUrl);
                    throw new SQLException(m);
                }
                if (version > 0 && version < minimumSupportedVersion) {
                    // TODO: here, I think you want to do some inspection of the jdbcUrl to determine whether the
                    // TODO: database is in-memory or an actual file on the device.
                    // TODO: if in memory, you should be able to, through introspection, drop all databases.
                    // TODO: if a file on the device, then you should be able to find the filename and delete the file
//                    File databaseFile = new File(db.getPath());
//                    onBeforeDelete(db);
//                    db.close();
//                    if (SQLiteDatabase.deleteDatabase(databaseFile)) {
//                        mIsInitializing = false;
//                        return getDatabaseLocked(writable);
//                    } else {
//                        throw new IllegalStateException("Unable to delete obsolete database "
//                                + mName + " with version " + version);
//                    }
                } else {
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
                        setVersion(tempDb, newVersion);
                    } finally {
                        tempDb.commit();
                        if (prevAutoCommit) {
                            tempDb.setAutoCommit(true);
                        }
                    }
                }
            }

            onOpen(tempDb);
            if (tempDb.isReadOnly()) {
                // TODO: logging
            }
            this.db = tempDb;
            return tempDb;
        } catch (SQLException sqle) {
            throw new RuntimeException(sqle);
        } finally {
            mIsInitializing = false;
            if (tempDb != null && tempDb != this.db) {
                try {
                    tempDb.close();
                } catch (SQLException sqle) {
                    // TODO: determine what to do here
                }
            }
        }
    }

    public void onBeforeDelete(Connection db) {
        // empty implementation--override if necessary
    }

    /**
     * <p>Called before {@link #onCreate}, {@link #onUpgrade}, {@link #onDowngrade}, or {@link #onOpen}
     * are called. Don't modify the database here--just configure the database connection
     * @param db the {@link Connection} to the database
     */
    public void onConfigure(Connection db) {
        // empty implementation--override if necessary
    }

    public void onCreate(Connection db) {
        // empty implementation--override if necessary
    }

    public void onDowngrade(Connection db, int version, int newVersion) {
        // empty implementation--override if necessary
    }

    public void onUpgrade(Connection db, int version, int newVersion) {
        // empty implementation--override if necessary
    }

    public void onOpen(Connection db) {
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

    private static void setVersion(Connection db, int version) throws SQLException {
        db.prepareStatement(String.format("PRAGMA user_version = %d;", version)).execute();
    }

    private static int getVersion(@Nonnull Connection db) throws SQLException {
        ResultSet r = db.prepareStatement("PRAGMA user_version;").executeQuery();
        return r.getInt(1); // <-- user_version
    }
}
