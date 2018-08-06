package com.fsryan.forsuredb.api.sqlgeneration;

import com.fsryan.forsuredb.api.*;
import com.fsryan.forsuredb.migration.MigrationSet;
import com.fsryan.forsuredb.serialization.FSDbInfoSerializer;

import javax.annotation.Nonnull;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * <p>In order for the framework to correctly instantiate your DBMSIntegrator,
 * it must have a public, no-arg constructor.
 */
public interface DBMSIntegrator {

    /**
     * <p>Given a {@link MigrationSet}, this method should return a list of raw
     * SQL queries that migrate the database accordingly.
     * <p>Note that this method signature may remove the need for an
     * {@link FSDbInfoSerializer} to simplify the API. It's purpose is to
     * deserialize the nested data within any migration's extra information.
     * @param migrationSet the {@link MigrationSet} for which SQL should be
     *                     generated.
     * @param dbInfoSerializer the {@link FSDbInfoSerializer} capable of
     *                         deserializing extra information within
     *                         migrations
     * @return An ordered List of raw SQL to run to migrate the database
     */
    List<String> generateMigrationSql(MigrationSet migrationSet, FSDbInfoSerializer dbInfoSerializer);

    /**
     * <p>Creates the required SQL for a raw SQL insertion query. Typically,
     * this will be used for static data insertion queries and not for
     * queries that your app normally performs using a
     * {@link com.fsryan.forsuredb.api.FSSaveApi} extension.
     * <p>Some columns in your implementation should be ignored. For example,
     * columns _id, modified, and created should not be modified. If the
     * columnValueMap is empty, then you should return whatever SQL corresponds
     * to a command to do nothing.
     * @param tableName the name of the table to which the row should be inserted
     * @param columns map of columns to the values to be inserted
     * @return the raw SQL query for insertion of a record in the table.
     */
    String newSingleRowInsertionSql(String tableName, List<String> columns);

    /**
     * <p>Creates the unambiguous column name for a table and column.
     * @param tableName the name of the table
     * @param columnName the name of the column of the table
     * @return the unambiguous column name given the table and column names
     * @see #unambiguousRetrievalColumn(String, String)
     */
    String unambiguousColumn(String tableName, String columnName);

    /**
     * <p>Creates an unambiguous name for column retrieval. This can be the
     * same as {@link #unambiguousColumn(String, String)} if the
     * framework/platform you're using supports the same unambiguous notation
     * as the DBMS you're using.
     * <p>Strangely, Android/SQLiteCursor does not support the table.column
     * notation for disambiguating columns when getting data out of a Cursor.
     * Therefore, this method was added to work around that. See bug 903852
     * for more details.
     * @param tableName the name of the table
     * @param columnName the name of the column of the table
     * @return the unambiguous column name specifically for disambugating
     * column names after a join
     * @see #unambiguousColumn(String, String)
     */
    String unambiguousRetrievalColumn(String tableName, String columnName);

    /**
     * <p>Formats zero or more {@link FSOrdering} into one correctly formatted
     * string for the ORDER BY clause of a query
     * @param orderings a possibly-empty list of {@link FSOrdering}
     * @return A string that correctly combines the ORDER BY expressions
     */
    String expressOrdering(List<FSOrdering> orderings);

    /**
     * <p>Parse the date from the string. This is intended only for returns
     * from the database. On a parse error, return null.
     * @param dateStr the dateStr read from the database column
     * @return a Date representation of the date string
     */
    Date parseDate(String dateStr);

    @Nonnull
    DateFormat getDateFormat();

    SqlForPreparedStatement createQuerySql(@Nonnull String table, FSProjection projection, FSSelection selection, List<FSOrdering> orderings);

    SqlForPreparedStatement createQuerySql(@Nonnull String table, List<FSJoin> joins, List<FSProjection> projections, FSSelection selection, List<FSOrdering> orderings);

    boolean alwaysUnambiguouslyAliasColumns();

    SqlForPreparedStatement createUpdateSql(@Nonnull String table, List<String> updateColumns, FSSelection selection, List<FSOrdering> orderings);

    SqlForPreparedStatement createDeleteSql(@Nonnull String table, FSSelection selection, List<FSOrdering> orderings);

    String createWhere(@Nonnull String tableName, @Nonnull List<Finder.WhereElement> whereElements);

    Object objectForReplacement(int op, Object obj);
}
