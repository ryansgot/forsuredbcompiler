package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.info.TableInfo;

import javax.annotation.Nonnull;
import java.util.*;

public class TableInfoUtil {

    /**
     * <p>Sorts tables given their foreign keys in an attempted
     * directed-acyclic manner in terms of their transitive dependencies. If the
     * map defines a graph with cycles, the table with the fewest number of hops
     * to reach the other is sorted ahead of the other. If both have the same number
     * of hops, then the output order is undefined.
     *
     * <p>The main purpose of this tool is to help you perform static data insertion
     * in the correct order. If you have a cyclic graph, then you should just turn off
     * foreign key constraints (if your DBMS allows you to do so) prior to performing
     * the static data insertion.
     *
     * @param schema a map from table name to {@link TableInfo}
     * @return a best-effort sorted list of tables where, as best as this algorithm
     * can do, tables with foreign keys will appear after the tables they reference.
     */
    public static List<TableInfo> bestEffortDAGSort(final Map<String, TableInfo> schema) {
        List<TableInfo> ret = new ArrayList<>(schema.values());
        Comparator<TableInfo> schemaAwareComparator = new Comparator<TableInfo>() {
            @SuppressWarnings("UseCompareMethod")   // <-- not supported on some versions of Android that are supported
            @Override
            public int compare(TableInfo t1, TableInfo t2) {
                int t1ToT2HopCount = findHopCount(t1, t2, 0);
                if (t1ToT2HopCount < 1) {
                    return -1;
                }

                int t2ToT1HopCount = findHopCount(t2, t1, 0);
                return t1ToT2HopCount < t2ToT1HopCount ? -1 : t1ToT2HopCount == t2ToT1HopCount ? 0 : 1;
            }

            private int findHopCount(@Nonnull TableInfo t1, @Nonnull TableInfo t2, int totalHops) {
                if (t1.foreignKeys() == null
                        || t1.foreignKeys().isEmpty()
                        || t1.tableName().equals(t2.tableName())) { // <-- break self-references
                    return 0;
                }

                int minDepth = 0;
                for (TableForeignKeyInfo tfki1 : t1.foreignKeys()) {
                    String foreignTableName = tfki1.foreignTableName();
                    if (t2.tableName().equals(foreignTableName)) {
                        return totalHops + 1;   // <-- there is a direct dependency t1 -> t2
                    }
                    int depth = findHopCount(schema.get(foreignTableName), t2, totalHops + 1);
                    // minDepth = 0 means that a transitive dependency has not yet been found.
                    minDepth = minDepth == 0 ? depth : Math.min(depth, minDepth);
                }

                // when minDepth is still zero, that means there is no transitive dependency
                return minDepth == 0 ? 0 : minDepth + totalHops;
            }
        };

        Collections.sort(ret, schemaAwareComparator);
        return ret;
    }
}
