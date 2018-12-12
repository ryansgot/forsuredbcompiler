package com.fsryan.forsuredb.info;

import javax.annotation.Nonnull;
import java.util.*;

import static com.fsryan.forsuredb.test.tools.CollectionUtil.setOf;

public abstract class TableInfoUtil {

    private static final Set<String> defaultColumnNames = setOf("created", "deleted", "modified", "_id");

    /**
     * <p>{@link TableInfo} in tests typically don't pertain to actual classes.
     * Use this method to ensure consistency such that you only have to worry
     * about the table name in the test rather than the table's fully qualified
     * java identifier.
     * @param tableName The name of the table
     * @return the default fully-qualified name for that table
     */
    @Nonnull
    public static String tableFQClassName(@Nonnull String tableName) {
        return tableName + ".qclass.name";
    }

    /**
     * <p>
     * @param tables the tables to put into the {@link Map}
     * @return A {@link Map}&lt;{@link String}, {@link TableInfo}&gt; of all
     * tables passed in using the default keying mechanism
     */
    public static Map<String, TableInfo> tableMapOf(TableInfo... tables) {
        return tableMapOf(false, tables);
    }

    /**
     * <p>Except in case of legacy tests, you should use
     * {@link #tableMapOf(TableInfo...)}, which will use the qualified class
     * name as the key
     * @param tables the tables to add to the map
     * @return a {@link Map}&lt;{@link String}, {@link TableInfo}&gt; with
     * table name keys
     */
    @Deprecated
    public static Map<String, TableInfo> tableMapWithTableNameKeys(TableInfo... tables) {
        return tableMapOf(true, tables);
    }

    public static Set<String> defaultColumnsPlus(String... additionalColumns) {
        Set<String> ret = new HashSet<>(defaultColumnNames);
        ret.addAll(Arrays.asList(additionalColumns));
        return ret;
    }

    /**
     * <p>
     * @param useTableNameAsKey whether to use the table name as the key
     * @param tables the tables to put into the {@link Map}
     * @return A {@link Map}&lt;{@link String}, {@link TableInfo}&gt; of all
     * tables passed in using the default keying mechanism
     */
    private static Map<String, TableInfo> tableMapOf(boolean useTableNameAsKey, TableInfo... tables) {
        Map<String, TableInfo> retMap = new HashMap<>();
        for (TableInfo table : tables) {
            final String qClassName = useTableNameAsKey ? table.tableName() : table.qualifiedClassName();
            retMap.put(qClassName, table);
        }
        return retMap;
    }
}
