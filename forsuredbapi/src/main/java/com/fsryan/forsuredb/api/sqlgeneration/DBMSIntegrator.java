package com.fsryan.forsuredb.api.sqlgeneration;

import com.fsryan.forsuredb.api.migration.MigrationSet;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *     In order for the framework to correctly instantiate your DBMSIntegrator, it must hae a public
 *     no-arg constructor.
 * </p>
 */
public interface DBMSIntegrator {

    /**
     * <p>
     *     Given a {@link MigrationSet}, this method should return a list of raw SQL queries that migrate
     *     the database accordingly.
     * </p>
     * @param migrationSet the {@link MigrationSet} for which SQL should be generated.
     * @return An ordered List of raw SQL to run to migrate the database
     */
    List<String> generateMigrationSql(MigrationSet migrationSet);

    /**
     * <p>
     *     Creates the required SQL for a raw SQL insertion query. Typically, this will be used for static
     *     data insertion queries and not for queries that your app normally performs using a
     *     {@link com.fsryan.forsuredb.api.FSSaveApi} extension.
     * </p>
     * <p>
     *     Some columns in your implementation should be ignored. For example, columns _id, modified, and created
     *     should not be modified. If the columnValueMap is empty, then you should return whatever SQL corresponds to
     *     a command to do nothing.
     * </p>
     * @param tableName the name of the table to which the row should be inserted
     * @param columnValueMap map of columns to the values to be inserted
     * @return the raw SQL query for insertion of a record in the table.
     */
    String newSingleRowInsertionSql(String tableName, Map<String, String> columnValueMap);
}