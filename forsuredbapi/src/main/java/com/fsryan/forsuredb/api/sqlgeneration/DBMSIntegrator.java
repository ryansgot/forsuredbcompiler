package com.fsryan.forsuredb.api.sqlgeneration;

import com.fsryan.forsuredb.api.migration.MigrationSet;

import java.util.Date;
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

    /**
     * <p>
     *     Creates the unambiguous column name for a table and column.
     * </p>
     * @param tableName the name of the table
     * @param columnName the name of the column of the table
     * @return the unambiguous column name given the table and column names
     * @see #unambiguousRetrievalColumn(String, String)
     */
    String unambiguousColumn(String tableName, String columnName);

    /**
     * <p>
     *     Creates an unambiguous name for column retrieval. This can be the same as
     *     {@link #unambiguousColumn(String, String)} if the framework/platform you're using supports the same
     *     unambiguous notation as the DBMS you're using.
     * </p>
     * <p>
     *     Strangely, Android/SQLiteCursor does not support the table.column notation for disambiguating columns
     *     when getting datat out of a Cursor. Therefore, this method was added to work around that. See bug
     *     903852 for more details.
     * </p>
     * @param tableName the name of the table
     * @param columnName the name of the column of the table
     * @return the unambiguous column name specifically for disambugating column names after a join
     * @see #unambiguousColumn(String, String)
     */
    String unambiguousRetrievalColumn(String tableName, String columnName);

    /**
     * <p>
     *     Creates an ascending ORDER BY expression. This should use the unambiguous column name because it
     *     could be used in a join wherein there are columns with the same name.
     * </p>
     * @param tableName the name of the table
     * @param columnName the name of the column of the table
     * @return the unambiguous ORDER BY expression
     * @see #orderByDesc(String, String)
     */
    String orderByAsc(String tableName, String columnName);

    /**
     * <p>
     *     Creates a descending ORDER BY expression. This should use the unambiguous column name because it
     *     could be used in a join wherein there are columns with the same name.
     * </p>
     * @param tableName the name of the table
     * @param columnName the name of the column of the table
     * @return the unambiguous ORDER BY expression
     * @see #orderByAsc(String, String)
     */
    String orderByDesc(String tableName, String columnName);

    /**
     * <p>
     *     Formats zero or more ORDER BY expressions into one correctly formatted string for the ORDER BY
     *     clause of a query
     * </p>
     * @param orderByList a possibly-empty list of ORDER BY expressions
     * @return A string that correctly combines the ORDER BY expressions
     */
    String combineOrderByExpressions(List<String> orderByList);

    /**
     * <p>
     *     Creates a where operation without resolving the comparison value--this will be resolved later
     *     as a part of the {@link com.fsryan.forsuredb.api.FSSelection} that is sent to the
     *     {@link com.fsryan.forsuredb.api.FSQueryable} in order to run the query.
     * </p>
     * <p>
     *     Possible values are:
     *     <ul>
     *         <li>{@link com.fsryan.forsuredb.api.Finder#OP_LT}</li>
     *         <li>{@link com.fsryan.forsuredb.api.Finder#OP_LE}</li>
     *         <li>{@link com.fsryan.forsuredb.api.Finder#OP_NE}</li>
     *         <li>{@link com.fsryan.forsuredb.api.Finder#OP_EQ}</li>
     *         <li>{@link com.fsryan.forsuredb.api.Finder#OP_GE}</li>
     *         <li>{@link com.fsryan.forsuredb.api.Finder#OP_GT}</li>
     *         <li>{@link com.fsryan.forsuredb.api.Finder#OP_LIKE}</li>
     *     </ul>
     *     In the case that one of these values is not input, you should output an empty string.
     * </p>
     * @param tableName the name of the table
     * @param column the name of the column of the table
     * @param operator the operator to use
     * @return A partial WHERE clause that ends with the operator
     * @see com.fsryan.forsuredb.api.Finder
     */
    String whereOperation(String tableName, String column, int operator);

    /**
     * <p>
     *     Format the date in the way the DBMS cares to store it
     * </p>
     * @param date the date to format
     * @return A string representation of the date
     */
    String formatDate(Date date);

    /**
     * @return The wildcard symbol/keyword
     */
    String wildcardKeyword();

    /**
     * @return the AND symbol/keyword
     */
    String and();

    /**
     * @return the OR symbol/keyword
     */
    String or();
}
