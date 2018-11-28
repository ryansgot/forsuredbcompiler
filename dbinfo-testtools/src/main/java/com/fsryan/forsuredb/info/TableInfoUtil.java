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
        Map<String, TableInfo> retMap = new HashMap<>();
        for (TableInfo table : tables) {
            // TODO: change the keying mechanism to be based upon fq class name
//            retMap.put(table.qualifiedClassName(), table);
            retMap.put(table.tableName(), table);
        }
        return retMap;
    }

    public static Set<String> defaultColumnsPlus(String... additionalColumns) {
        Set<String> ret = new HashSet<>(defaultColumnNames);
        ret.addAll(Arrays.asList(additionalColumns));
        return ret;
    }
}
