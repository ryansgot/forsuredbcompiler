package com.fsryan.forsuredb;

import com.fsryan.forsuredb.api.FSTableCreator;
import com.fsryan.forsuredb.api.RecordContainer;
import com.fsryan.forsuredb.api.TableInfoUtil;
import com.fsryan.forsuredb.api.sqlgeneration.Sql;
import com.fsryan.forsuredb.api.staticdata.StaticDataRetrieverFactory;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.MigrationSet;
import com.fsryan.forsuredb.resources.Resources;
import com.fsryan.forsuredb.serialization.FSDbInfoSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.fsryan.forsuredb.queryable.StatementBinder.bindObjects;

public class FSDBHelper extends AbstractDBOpener {

    private static FSDBHelper instance;

    // a special DBVersionUpdater intended to be a reference implementation and a simple
    // way to use forsoredbjdbc with SQLite
    private static final DBConfigurer sqliteDBConfigurer = new DBConfigurer() {
        @Override
        public int discoverVersion(Connection db) throws SQLException {
            try (PreparedStatement ps = db.prepareStatement("PRAGMA user_version;")) {
                try (ResultSet r = ps.executeQuery()) {
                    return r.getInt(1); // <-- user_version
                }
            }
        }

        @Override
        public boolean setVersion(Connection db, int version) throws SQLException {
            try (PreparedStatement ps = db.prepareStatement("PRAGMA user_version = " + version +" ;")) {
                return ps.execute();
            }
        }

        @Override
        public boolean mustEnableForeignKeys() {
            return true;
        }

        @Override
        public void enableForeignKeys(Connection db) throws SQLException {
            try (PreparedStatement ps = db.prepareStatement("PRAGMA foreign_keys=ON;")) {
                ps.execute();
            }
        }
    };

    private final List<FSTableCreator> tables;
    private final List<MigrationSet> migrationSets;
    private final FSDbInfoSerializer dbInfoSerializer;
    private final boolean debugMode;

    private FSDBHelper(String jdbcUrl,
                       Properties connectionProps,
                       DBConfigurer dbConfigurer,
                       List<FSTableCreator> tables,
                       List<MigrationSet> migrationSets,
                       FSDbInfoSerializer dbInfoSerializer,
                       boolean debugMode) {
        super(jdbcUrl, connectionProps, dbConfigurer, identifyDbVersion(migrationSets));
        this.tables = tables;
        Collections.sort(tables);
        this.migrationSets = migrationSets;
        this.dbInfoSerializer = dbInfoSerializer;
        this.debugMode = debugMode;
    }

    /**
     * <p>Use in thethe production version of your app. It has debug mode set to false. If you want
     * debugMode on, then call
     * {@link #initDebug(String, Properties, DBConfigurer, List, FSDbInfoSerializer)}.
     * @param jdbcUrl The jdbc url of the database (eg: jdbc:sqlite:memory)
     * @param connectionProps any additional properties required to connect to the database
     * @param dbVersionUpdater an object capable of discovering and updating a database version
     * @param tables The information for creating tables
     * @param dbInfoSerializer the {@link FSDbInfoSerializer} that your project will use to deserialize
     *                         db schema and migration info
     * @see #initDebugSQLite(String, Properties, List, FSDbInfoSerializer) if you're using SQLite and
     * want to initialize in debug mode
     * @see #initDebug(String, Properties, DBConfigurer, List, FSDbInfoSerializer)
     */
    public static synchronized void init(@Nonnull String jdbcUrl,
                                         @Nullable Properties connectionProps,
                                         @Nonnull DBConfigurer dbVersionUpdater,
                                         @Nonnull List<FSTableCreator> tables,
                                         @Nonnull FSDbInfoSerializer dbInfoSerializer) {
        if (instance != null) {
            return;
        }
        List<MigrationSet> migrationSets = new Migrator(dbInfoSerializer).getMigrationSets();
        Collections.sort(migrationSets);
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
                                               @Nullable Properties connectionProps,
                                               @Nonnull List<FSTableCreator> tables,
                                               @Nonnull FSDbInfoSerializer dbInfoSerializer) {
        init(jdbcUrl, connectionProps, sqliteDBConfigurer, tables, dbInfoSerializer);
    }

    /**
     * <p>Use in thethe debug version of your app. It has debug mode set to true. If you want
     * debugMode off, then call {@link #init(String, Properties, DBConfigurer, List, FSDbInfoSerializer)}.
     * @param jdbcUrl The jdbc url of the database (eg: jdbc:sqlite:memory)
     * @param connectionProps any additional properties required to connect to the database
     * @param dbVersionUpdater
     * @param tables The information for creating tables
     * @param dbInfoSerializer the {@link FSDbInfoSerializer} that your project will use to deserialize
     *                         db schema and migration info
     * @see #init(String, Properties, DBConfigurer, List, FSDbInfoSerializer)
     */
    public static synchronized void initDebug(@Nonnull String jdbcUrl,
                                              @Nullable Properties connectionProps,
                                              @Nonnull DBConfigurer dbVersionUpdater,
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
                                                    @Nullable Properties connectionProps,
                                                    @Nonnull List<FSTableCreator> tables,
                                                    @Nonnull FSDbInfoSerializer dbInfoSerializer) {
        initDebug(jdbcUrl, connectionProps, sqliteDBConfigurer, tables, dbInfoSerializer);
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
    }

    @Override
    public void onUpgrade(Connection db, int oldVersion, int newVersion) {
        applyMigrations(db, oldVersion);
    }

    @Override
    public void onOpen(Connection db) throws SQLException {
        super.onOpen(db);
        if (!db.isReadOnly() && dbConfigurer.mustEnableForeignKeys()) {
            dbConfigurer.enableForeignKeys(db);
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
        int staticDataInsertFromVersion = 0;
        final Map<String, Map<Integer, List<RecordContainer>>> versionToStaticDataRecordContainers = new HashMap<>();
        while (migrationSets.size() > 0) {
            MigrationSet migrationSet = migrationSets.get(0);
            int version = migrationSet.dbVersion();
            if (previousVersion >= version) {
                migrationSets.remove(0);
                continue;
            }

            if (staticDataInsertFromVersion == 0) {
                staticDataInsertFromVersion = migrationSet.dbVersion();
                versionToStaticDataRecordContainers.putAll(createStaticDataRecordContainers());
            }
            migrationSets.remove(0);

            final List<String> sqlScript = Sql.generator().generateMigrationSql(migrationSet, dbInfoSerializer);
            migrateSchema(db, sqlScript, "performing migration sql: ");
            insertStaticData(db, migrationSet, versionToStaticDataRecordContainers);
        }
    }

    private void insertStaticData(Connection db, MigrationSet migrationSet, Map<String, Map<Integer, List<RecordContainer>>> versionToStaticDataRecordContainers) {
        TableInfoUtil.bestEffortDAGSort(migrationSet.targetSchema()).stream()
                .map(TableInfo::tableName)
                .filter(this::hasStaticData)
                .filter(versionToStaticDataRecordContainers::containsKey)
                .forEach(tableName -> {
                    Map<Integer, List<RecordContainer>> versionRecordMap = versionToStaticDataRecordContainers.get(tableName);
                    List<RecordContainer> records = versionRecordMap.get(migrationSet.dbVersion());
                    if (records == null) {
                        return;
                    }
                    insertStaticData(db, tableName, records);
                });
    }

    private boolean hasStaticData(String tableName) {
        return tables.stream()
                .filter(tc -> tc.getTableName().equals((tableName)))
                .anyMatch(tc -> tc.getStaticDataAsset() != null && !tc.getStaticDataAsset().isEmpty());
    }

    private Map<String, Map<Integer, List<RecordContainer>>> createStaticDataRecordContainers() {
        Map<String, Map<Integer, List<RecordContainer>>> ret = new HashMap<>();
        for (FSTableCreator tc : tables) {
            String staticDataAsset = tc.getStaticDataAsset();
            if (staticDataAsset == null || staticDataAsset.isEmpty()) {
                continue;
            }

            URL url = null;
            try {
                // TODO: this is bound to fail. Change.
                url = Resources.getResourceURLs(resourceUrl -> resourceUrl.toString().endsWith(staticDataAsset)).get(0);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            try {
                StaticDataRetrieverFactory.createFor(tc.getTableName(), migrationSets, url).retrieve(versionRecordMap -> {
                    ret.put(tc.getTableName(), versionRecordMap);
                });
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return ret;
    }

    private void migrateSchema(Connection db, List<String> sqlScript, String logPrefix) {
        for (String insertionSqlString : sqlScript) {
            if (debugMode) {
                System.out.println(String.format("[forsuredb] %s %s", logPrefix, insertionSqlString));
            }
            try (PreparedStatement ps = db.prepareStatement(insertionSqlString)) {
                ps.execute();
            } catch (SQLException sqle) {
                throw new RuntimeException(sqle);
            }
        }
    }

    private void insertStaticData(Connection db, String tableName, List<RecordContainer> records) {
        // TODO: records are inserted individually, but there is not a strong reason why they should--investigate batching instead of individual record insertion
        records.forEach(record -> {
            final List<String> columns = new ArrayList<>(record.keySet());
            String sql = Sql.generator().newSingleRowInsertionSql(tableName, columns);
            try (PreparedStatement statement = db.prepareStatement(sql)) {
                bindObjects(statement, columns, record);
                statement.executeUpdate();  // TODO: figure out what to do with the return
            } catch (SQLException sqle) {
                throw new RuntimeException(sqle);
            }
        });
    }
}
