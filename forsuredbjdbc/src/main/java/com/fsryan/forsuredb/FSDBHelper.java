package com.fsryan.forsuredb;

import com.fsryan.forsuredb.api.FSTableCreator;
import com.fsryan.forsuredb.api.sqlgeneration.Sql;
import com.fsryan.forsuredb.migration.MigrationSet;
import com.fsryan.forsuredb.serialization.FSDbInfoSerializer;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class FSDBHelper extends AbstractDBOpener {

    private static FSDBHelper instance;

    // a special DBVersionUpdater intended to be a reference implementation and a simple
    // way to use forsoredbjdbc with SQLite
    private static final DBVersionUpdater sqliteDbVersionUpdater = new DBVersionUpdater() {
        @Override
        public int discoverVersion(Connection db) throws SQLException {
            ResultSet r = db.prepareStatement("PRAGMA user_version;").executeQuery();
            return r.getInt(1); // <-- user_version
        }

        @Override
        public boolean setVersion(Connection db, int version) throws SQLException {
            return db.prepareStatement(String.format("PRAGMA user_version = %d;", version)).execute();
        }
    };

    private final List<FSTableCreator> tables;
    private final List<MigrationSet> migrationSets;
    private final FSDbInfoSerializer dbInfoSerializer;
    private final boolean debugMode;

    private FSDBHelper(String jdbcUrl,
                       Properties connectionProps,
                       DBVersionUpdater dbVersionUpdater,
                       List<FSTableCreator> tables,
                       List<MigrationSet> migrationSets,
                       FSDbInfoSerializer dbInfoSerializer,
                       boolean debugMode) {
        super(jdbcUrl, connectionProps, dbVersionUpdater, identifyDbVersion(migrationSets));
        this.tables = tables;
        this.migrationSets = migrationSets;
        this.dbInfoSerializer = dbInfoSerializer;
        this.debugMode = debugMode;
    }

    /**
     * <p>Use in thethe production version of your app. It has debug mode set to false. If you want
     * debugMode on, then call
     * {@link #initDebug(String, Properties, DBVersionUpdater, List, FSDbInfoSerializer)}.
     * @param jdbcUrl The jdbc url of the database (eg: jdbc:sqlite:memory)
     * @param connectionProps any additional properties required to connect to the database
     * @param dbVersionUpdater an object capable of discovering and updating a database version
     * @param tables The information for creating tables
     * @param dbInfoSerializer the {@link FSDbInfoSerializer} that your project will use to deserialize
     *                         db schema and migration info
     * @see #initDebugSQLite(String, Properties, List, FSDbInfoSerializer) if you're using SQLite and
     * want to initialize in debug mode
     * @see #initDebug(String, Properties, DBVersionUpdater, List, FSDbInfoSerializer)
     */
    public static synchronized void init(@Nonnull String jdbcUrl,
                                         @Nonnull Properties connectionProps,
                                         @Nonnull DBVersionUpdater dbVersionUpdater,
                                         @Nonnull List<FSTableCreator> tables,
                                         @Nonnull FSDbInfoSerializer dbInfoSerializer) {
        if (instance != null) {
            return;
        }
        List<MigrationSet> migrationSets = new Migrator(dbInfoSerializer).getMigrationSets();
        instance = new FSDBHelper(
                jdbcUrl,
                connectionProps,
                dbVersionUpdater,
                tables,
                migrationSets,
                dbInfoSerializer,
                false
        );
    }

    /**
     * <p>Use in thethe production version of your app. It has debug mode set to false. If you want
     * debugMode on, then call
     * {@link #initDebugSQLite(String, Properties, List, FSDbInfoSerializer)}.
     * @param jdbcUrl The jdbc url of the database (eg: jdbc:sqlite:memory)
     * @param connectionProps any additional properties required to connect to the database
     * @param tables The information for creating tables
     * @param dbInfoSerializer the {@link FSDbInfoSerializer} that your project will use to deserialize
     *                         db schema and migration info
     * @see #initDebugSQLite(String, Properties, List, FSDbInfoSerializer)
     */
    public static synchronized void initSQLite(@Nonnull String jdbcUrl,
                                               @Nonnull Properties connectionProps,
                                               @Nonnull List<FSTableCreator> tables,
                                               @Nonnull FSDbInfoSerializer dbInfoSerializer) {
        init(jdbcUrl, connectionProps, sqliteDbVersionUpdater, tables, dbInfoSerializer);
    }

    /**
     * <p>Use in thethe debug version of your app. It has debug mode set to true. If you want
     * debugMode off, then call {@link #init(String, Properties, DBVersionUpdater, List, FSDbInfoSerializer)}.
     * @param jdbcUrl The jdbc url of the database (eg: jdbc:sqlite:memory)
     * @param connectionProps any additional properties required to connect to the database
     * @param dbVersionUpdater
     * @param tables The information for creating tables
     * @param dbInfoSerializer the {@link FSDbInfoSerializer} that your project will use to deserialize
     *                         db schema and migration info
     * @see #init(String, Properties, DBVersionUpdater, List, FSDbInfoSerializer)
     */
    public static synchronized void initDebug(@Nonnull String jdbcUrl,
                                              @Nonnull Properties connectionProps,
                                              @Nonnull DBVersionUpdater dbVersionUpdater,
                                              @Nonnull List<FSTableCreator> tables,
                                              @Nonnull FSDbInfoSerializer dbInfoSerializer) {
        if (instance != null) {
            return;
        }
        List<MigrationSet> migrationSets = new Migrator(dbInfoSerializer).getMigrationSets();
        instance = new FSDBHelper(
                jdbcUrl,
                connectionProps,
                dbVersionUpdater,
                tables,
                migrationSets,
                dbInfoSerializer,
                true
        );
    }

    /**
     * <p>Use in thethe debug version of your app. It has debug mode set to true. If you want
     * debugMode off, then call {@link #initSQLite(String, Properties, List, FSDbInfoSerializer)}.
     * @param jdbcUrl The jdbc url of the database (eg: jdbc:sqlite:memory)
     * @param connectionProps any additional properties required to connect to the database
     * @param tables The information for creating tables
     * @param dbInfoSerializer the {@link FSDbInfoSerializer} that your project will use to deserialize
     *                         db schema and migration info
     * @see #initSQLite(String, Properties, List, FSDbInfoSerializer)
     */
    public static synchronized void initDebugSQLite(@Nonnull String jdbcUrl,
                                                    @Nonnull Properties connectionProps,
                                                    @Nonnull List<FSTableCreator> tables,
                                                    @Nonnull FSDbInfoSerializer dbInfoSerializer) {
        initDebug(jdbcUrl, connectionProps, sqliteDbVersionUpdater, tables, dbInfoSerializer);
    }

    public static synchronized FSDBHelper inst() {
        if (instance == null) {
            throw new IllegalStateException("Must call FSDBHelper.initSQLite prior to getting instance");
        }
        return instance;
    }

    @Override
    public void onCreate(Connection db) {
        applyMigrations(db, 0);

        Collections.sort(tables);
        for (FSTableCreator table : tables) {
            executeSqlList(db, new StaticDataSQL(table).getInsertionSQL(), "inserting static data: ");
        }
    }

    @Override
    public void onUpgrade(Connection db, int oldVersion, int newVersion) {
        applyMigrations(db, oldVersion);
    }

    @Override
    public void onOpen(Connection db) throws SQLException {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.prepareStatement("PRAGMA foreign_keys=ON;").execute();
        }
    }

    public Connection getWritableDatabase() throws SQLException {
        synchronized (this) {
            return getDatabaseLocked(true);
        }
    }

    public Connection getReadableDatabase() throws SQLException {
        synchronized (this) {
            return getDatabaseLocked(false);
        }
    }

    public boolean inDebugMode() {
        return debugMode;
    }

    /**
     * @param migrationSets The {@link List} of
     * {@link com.fsryan.forsuredb.migration.MigrationSet MigrationSet}
     * @return either 1 or the largest dbVersion in the migrationSets list
     */
    private static int identifyDbVersion(List<MigrationSet> migrationSets) {
        if (migrationSets == null || migrationSets.size() == 0) {
            return 1;
        }

        int version = 1;
        for (MigrationSet migrationSet : migrationSets) {
            version = migrationSet.dbVersion() > version ? migrationSet.dbVersion() : version;
        }
        return version;
    }

    private void applyMigrations(Connection db, int previousVersion) {
        for (MigrationSet migrationSet : migrationSets) {
            if (previousVersion >= migrationSet.dbVersion()) {
                continue;
            }
            final List<String> sqlScript = Sql.generator().generateMigrationSql(migrationSet, dbInfoSerializer);
            executeSqlList(db, sqlScript, "performing migration sql: ");
        }
    }

    private void executeSqlList(Connection db, List<String> sqlScript, String logPrefix) {
        // TODO: batch this up and make it work with a prepared statement
        try {
            if (debugMode) {
                for (String insertionSqlString : sqlScript) {
                    System.out.println(String.format("[forsuredb] %s -- %s", logPrefix, insertionSqlString));
                    db.prepareStatement(insertionSqlString).execute();
                }
            } else {
                for (String insertionSqlString : sqlScript) {
                    db.prepareStatement(insertionSqlString).execute();
                }
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace(); // TODO: determine what to do
        }
    }
}
