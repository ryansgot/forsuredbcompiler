package com.fsryan.forsuredb.sqlitelib;

import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.sqlitelib.diff.MigrationUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <p>Generate SQL that will check whether the object (table, trigger, column,
 * foreign key, index, etc.) exists in the current schema. It does this by
 * running the appropriate query against the schema and checking the count of
 * rows returned. If you're checking for the existance of an object, then the
 * output result set will be as follows:
 * <table>
 *   <th>exists</th>
 *   <tr><td>true/false</td></tr>
 * </table>
 * When the output is false, the test will fail. See
 * {@link #makeAssertion(Connection, String)} for the message you'll receive.
 *
 * <p>Note that this must be run with SQLite 3.16.0 or greater when checking
 * for columns and foreign keys because it uses
 * {@code SELECT * pragma_table_info('tablename')} syntax in order to perform
 * schema verifications. This kind of pragma query returns an error on older
 * versions of SQLite because it will not be able to find that table.
 *
 * <p>Assertions on indices is forthcoming.
 */
public abstract class SqliteMasterAssertions {

    /**
     * @param tableName the table name to look for
     * @return assertion SQL to run to ensure that this table exists
     */
    @Nonnull
    public static String forTableExists(@Nonnull String tableName) {
        return forTable(tableName, true);
    }

    /**
     * @param tableName the table name to look for
     * @return assertion SQL to run to ensure that this table does not exist
     */
    @Nonnull
    public static String forTableNotExists(@Nonnull String tableName) {
        return forTable(tableName, false);
    }

    /**
     * @param tableName the table name to look for
     * @param exists whether you want to assert the tagble exists (true) or
     *               does not exist (false)
     * @return assertion SQL to run to ensure that this table either exists or
     * does not exist
     */
    @Nonnull
    public static String forTable(@Nonnull String tableName, boolean exists) {
        return String.format(
                "SELECT CASE COUNT(*) WHEN %d THEN 'true' ELSE 'false' END AS '%s' FROM sqlite_master WHERE type = 'table' AND name = '%s';",
                exists ? 1 : 0,
                exists ? "exists" : "not_exists",
                tableName
        );
    }

    /**
     * @param tableName the name of the table that has the trigger
     * @param triggerName the name of the trigger
     * @return assertion SQL to run to ensure that this trigger exists
     */
    @Nonnull
    public static String forTriggerExists(@Nonnull String tableName, @Nonnull String triggerName) {
        return forTrigger(tableName, triggerName, true);
    }

    /**
     * @param tableName the name of the table that has the trigger
     * @param triggerName the name of the trigger
     * @return assertion SQL to run to ensure that this trigger does not exist
     */
    @Nonnull
    public static String forTriggerNotExists(@Nonnull String tableName, @Nonnull String triggerName) {
        return forTrigger(tableName, triggerName, false);
    }

    /**
     * @param tableName the name of the table that has the trigger
     * @param triggerName the name of the trigger
     * @param exists whether you want to assert the trigger exists (true) or
     *               does not exist (false)
     * @return assertion SQL to run to ensure that this trigger either exists
     * or does not exist
     */
    @Nonnull
    public static String forTrigger(@Nonnull String tableName, @Nonnull String triggerName, boolean exists) {
        return String.format(
                "SELECT CASE COUNT(*) WHEN %d THEN 'true' ELSE 'false' END AS '%s' FROM sqlite_master WHERE type = 'trigger' AND name = '%s' AND tbl_name = '%s';",
                exists ? 1 : 0,
                exists ? "exists" : "not_exists",
                triggerName,
                tableName
        );
    }

    /**
     * <p>TODO: check for nonnull
     * @param tableName the name of the table containing the column
     * @param colName the name of the column
     * @param sqlTypeName the SQL type name of the column
     * @param primaryKey the primary key columns of the table
     * @return assertion SQL to run to ensure that this column exists
     */
    @Nonnull
    public static String forColumnExists(@Nonnull String tableName, @Nonnull String colName, @Nonnull String sqlTypeName, @Nullable String defaultValue, boolean primaryKey) {
        return forColumn(tableName, colName, sqlTypeName, defaultValue, primaryKey, true);
    }

    /**
     * <p>TODO: check for nonnull
     * @param tableName the name of the table containing the column
     * @param colName the name of the column
     * @param sqlTypeName the SQL type name of the column
     * @param primaryKey the primary key columns of the table
     * @return assertion SQL to run to ensure that this column does not exist
     */
    @Nonnull
    public static String forColumnNotExists(@Nonnull String tableName, @Nonnull String colName, @Nonnull String sqlTypeName, @Nullable String defaultValue, boolean primaryKey) {
        return forColumn(tableName, colName, sqlTypeName, defaultValue, primaryKey, false);
    }

    /**
     * <p>TODO: check for nonnull
     * @param tableName the name of the table containing the column
     * @param colName the name of the column
     * @param sqlTypeName the SQL type name of the column
     * @param primaryKey the primary key columns of the table
     * @param defaultValue the default value of the column or null if none
     * @param exists whether you want to assert the column exists (true) or
     *               does not exist (false)
     * @return assertion SQL to run to ensure that this column either exists or
     * does not exist
     */
    @Nonnull
    public static String forColumn(@Nonnull String tableName, @Nonnull String colName, @Nonnull String sqlTypeName, @Nullable String defaultValue, boolean primaryKey, boolean exists) {
        return String.format(
                "SELECT CASE COUNT(*) WHEN %d THEN 'true' ELSE 'false' END AS '%s' FROM pragma_table_info('%s') WHERE type = '%s' AND name = '%s' AND %s AND pk %s 0;",
                exists ? 1 : 0,
                exists ? "exists" : "not_exists",
                tableName,
                sqlTypeName,
                colName,
                defaultValue == null ? "\"dflt_value\" IS NULL" : "\"dflt_value\" = " + defaultValue,
                primaryKey ? ">" : "="
        );
    }

    /**
     * <p>Generate SQL to ensure that a foreign key does exists.
     * @param tableName The name of the table that has the foreign key
     *                  reference
     * @param parentTableName the name of the table being referred to
     * @param localColName the name of the local column
     * @param parentColName the name of the column being referred to
     * @param onUpdate the update action--if empty, will be &quot;NO
     *                 ACTION&quot;
     * @param onDelete the delete action--if empty, will be &quot;NO
     *                 ACTION&quot;
     * @return assertion SQL to run to ensure that this foreign key exists
     */
    @Nonnull
    public static String forForeignKeyExists(@Nonnull String tableName, @Nonnull String parentTableName, @Nonnull String localColName, @Nonnull String parentColName, @Nonnull String onUpdate, @Nonnull String onDelete) {
        return forForeignKey(tableName, parentTableName, localColName, parentColName, onUpdate, onDelete, true);
    }

    /**
     * <p>Generate SQL to ensure that a foreign key does not exist.
     * @param tableName The name of the table that has the foreign key
     *                  reference
     * @param parentTableName the name of the table being referred to
     * @param localColName the name of the local column
     * @param parentColName the name of the column being referred to
     * @param onUpdate the update action--if empty, will be &quot;NO
     *                 ACTION&quot;
     * @param onDelete the delete action--if empty, will be &quot;NO
     *                 ACTION&quot;
     * @return assertion SQL to run to ensure that this foreign key does not
     * exist
     */
    @Nonnull
    public static String forForeignKeyNotExists(@Nonnull String tableName, @Nonnull String parentTableName, @Nonnull String localColName, @Nonnull String parentColName, @Nonnull String onUpdate, @Nonnull String onDelete) {
        return forForeignKey(tableName, parentTableName, localColName, parentColName, onUpdate, onDelete, false);
    }

    /**
     * <p>Generate SQL to ensure that a foreign key exists or does not exist.
     * @param tableName The name of the table that has the foreign key
     *                  reference
     * @param parentTableName the name of the table being referred to
     * @param localColName the name of the local column
     * @param parentColName the name of the column being referred to
     * @param onUpdate the update action--if empty, will be &quot;NO
     *                 ACTION&quot;
     * @param onDelete the delete action--if empty, will be &quot;NO
     *                 ACTION&quot;
     * @param exists whether you want to assert that this foreign key exists
     *               (true) or assert that this foreign key doesn't exist
     *               (false).
     * @return assertion SQL to run to ensure that this foreign key either
     * exists or does not exist
     */
    @Nonnull
    public static String forForeignKey(@Nonnull String tableName, @Nonnull String parentTableName, @Nonnull String localColName, @Nonnull String parentColName, @Nonnull String onUpdate, @Nonnull String onDelete, boolean exists) {
        return String.format(
                "SELECT CASE COUNT(*) WHEN %d THEN 'true' ELSE 'false' END AS '%s' FROM pragma_foreign_key_list('%s') WHERE \"table\" = '%s' AND \"from\" = '%s' AND \"to\" = '%s' AND on_update = '%s' AND on_delete = '%s';",
                exists ? 1 : 0,
                exists ? "exists" : "not_exists",
                tableName,
                parentTableName,
                localColName,
                parentColName,
                onUpdate.isEmpty() ? "NO ACTION" : onUpdate,
                onDelete.isEmpty() ? "NO ACTION" : onDelete
        );
    }

    /**
     * <p>Intended to be a one-stop shop for checking that a full table schema
     * exists.
     * @param tables a collection of {@link TableInfo} describing the tables to
     *               look for
     * @param additionalSql Any additional assertions (will be added to end)
     * @return assertion SQL to run to ensure that all of the tables passed in
     * as well as the additional assertions pass.
     */
    public static List<String> forAllTableInfoPlus(@Nonnull Collection<TableInfo> tables, @Nonnull String... additionalSql) {
        List<String> ret = new ArrayList<>();
        for (TableInfo table : tables) {
            ret.addAll(forTableInfo(table));
        }
        ret.addAll(Arrays.asList(additionalSql));
        return ret;
    }

    /**
     * <p>Not only will the table be checked, but also the columns and foreign
     * keys will be checked.
     * @param table the {@link TableInfo} describing the table to look for
     * @return assertion SQL to run to ensure that this table, its columns and
     * its foreign keys exist
     */
    public static List<String> forTableInfo(TableInfo table) {
        List<String> ret = new ArrayList<>();
        Set<String> pk = table.getPrimaryKey();
        ret.add(SqliteMasterAssertions.forTableExists(table.tableName()));
        ret.add(SqliteMasterAssertions.forTriggerExists(table.tableName(), table.tableName() + "_modified_trigger"));
        for (ColumnInfo column : table.getColumns()) {
            ret.add(forColumnInfo(table.tableName(), pk, column));
        }
        Set<TableForeignKeyInfo> foreignKeys = table.foreignKeys();
        for (TableForeignKeyInfo tfki : foreignKeys == null ? Collections.<TableForeignKeyInfo>emptySet() : foreignKeys) {
            ret.addAll(forTableForeignKeyInfo(table.tableName(), tfki));
        }
        return ret;
    }

    /**
     * @param tableName The name of the table
     * @param pk The set of columns that is this table's primary key
     * @param column the {@link ColumnInfo} describing the column to look for
     * @return assertion SQL to run to ensure that this column exists
     */
    public static String forColumnInfo(String tableName, Set<String> pk, ColumnInfo column) {
        String dfltVal = column.defaultValue();
        if (column.hasDefaultValue()) {
            dfltVal = Date.class.getName().equals(column.qualifiedType()) && "CURRENT_TIMESTAMP".equals(dfltVal)
                    ? '"' + SqlGenerator.CURRENT_UTC_TIME + '"'
                    : String.class.getName().equals(column.getQualifiedType())
                    ? "\"'" + dfltVal.replaceAll("'", "''") + "'\""
                    : "'" + dfltVal.replaceAll("'", "''") + "'";
        }
        return SqliteMasterAssertions.forColumnExists(
                tableName,
                column.getColumnName(),
                MigrationUtil.sqlTypeOf(column.getQualifiedType()),
                dfltVal,
                pk.contains(column.getColumnName())
        );
    }

    /**
     * <p>Generates assertion SQL to validate that the table has a foreign key
     * that matches the {@link TableForeignKeyInfo} passed in.
     * @param tableName the name of the table with the foreign key
     * @param tfki the {@link TableForeignKeyInfo} describing the foreign key
     *             to look for
     * @return assertion SQL to run to ensure that this foreign key exists
     */
    @Nonnull
    public static List<String> forTableForeignKeyInfo(@Nonnull String tableName, @Nonnull TableForeignKeyInfo tfki) {
        List<String> ret = new ArrayList<>(4);
        for (Map.Entry<String, String> localToForeignColumn : tfki.localToForeignColumnMap().entrySet()) {
            ret.add(forForeignKeyExists(
                    tableName,
                    tfki.foreignTableName(),
                    localToForeignColumn.getKey(),
                    localToForeignColumn.getValue(),
                    tfki.updateChangeAction(),
                    tfki.deleteChangeAction()
            ));
        }
        return ret;
    }

    /**
     * <p>Make all assertions specified in the sqlList parameter
     * @param conn the database {@link Connection}
     * @param sqlList the list of assertion SQL to run
     * @throws SQLException if the connection throws an exception when used
     * @see SqliteMasterAssertions for a description for the sql that can be
     * passed in to this method.
     */
    public static void makeAllAssertions(@Nonnull Connection conn, @Nonnull List<String> sqlList) throws SQLException {
        for (String sql : sqlList) {
            makeAssertion(conn, sql);
        }
    }

    /**
     * <p>Make an assertion specified by the sql passed in.
     * @param conn the database {@link Connection}
     * @param sql the assertion SQL to run
     * @throws SQLException if the connection throws an exception when used
     * @see SqliteMasterAssertions for a description for the sql that can be
     * passed in to this method.
     */
    public static void makeAssertion(@Nonnull Connection conn, @Nonnull String sql) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            ResultSet result = statement.executeQuery();
            assertTrue(result.next(), "sql: " + sql + "; failed to return a result");
            assertTrue(Boolean.parseBoolean(result.getString(1)), "sql: " + sql + "; expected to exist but did not");
        }
    }
}
