package com.fsryan.forsuredb;

import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.api.FSLogger;
import com.fsryan.forsuredb.api.FSTableCreator;
import com.fsryan.forsuredb.api.sqlgeneration.Sql;
import com.fsryan.forsuredb.api.staticdata.StaticDataRetrieverFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/*package*/ class StaticDataSQL {

    private final Class<? extends FSGetApi> apiClass;
    private final String tableName;
    private final String staticDataAsset;
    private final String staticDataRecordName;
    private final FSLogger log;

    /**
     * <p>A convenience for the constructor which takes the four parameters
     * @param tc {@link FSTableCreator} containing all of the table information
     */
    public StaticDataSQL(FSTableCreator tc) {
        this(tc.getTableApiClass(), tc.getTableName(), tc.getStaticDataAsset(), tc.getStaticDataRecordName());
    }

    public StaticDataSQL(Class<? extends FSGetApi> apiClass, String tableName, String staticDataAsset, String staticDataRecordName) {
        this.apiClass = apiClass;
        this.tableName = tableName;
        this.staticDataAsset = staticDataAsset;
        this.staticDataRecordName = staticDataRecordName;
        this.log = new FSLogger.DefaultFSLogger();
    }

    public List<String> getInsertionSQL() {
        if (!canCreateStaticDataInsertionQueries()) {
            log.e("Cannot create static data insertion queries for apiClass: %s", apiClass.getSimpleName());
            return Collections.emptyList();
        }

        List<String> insertionQueries = new ArrayList<>();
        for (Map<String, String> rawRecord : getRawRecords()) {
            insertionQueries.add(Sql.generator().newSingleRowInsertionSql(tableName, rawRecord));
        }

        return insertionQueries;
    }

    private List<Map<String, String>> getRawRecords() {
        InputStream xmlStream = null;
        try {
            xmlStream = ClassLoader.getSystemClassLoader().getResourceAsStream(staticDataAsset);
            if (xmlStream == null) {
                throw new IllegalStateException("There must be a resource in the default package called: " + staticDataAsset);
            }
            return new StaticDataRetrieverFactory(log).fromStream(xmlStream).getRawRecords(staticDataRecordName);
        } finally {
            if (xmlStream != null) {
                try {
                    xmlStream.close();
                } catch (IOException e) {}
            }
        }
    }

    private boolean canCreateStaticDataInsertionQueries() {
        return tableName != null && !tableName.isEmpty()
                && staticDataAsset != null && !staticDataAsset.isEmpty()
                && staticDataRecordName != null && !staticDataRecordName.isEmpty();
    }
}