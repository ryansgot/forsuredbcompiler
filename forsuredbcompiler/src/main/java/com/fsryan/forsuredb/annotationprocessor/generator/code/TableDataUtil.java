package com.fsryan.forsuredb.annotationprocessor.generator.code;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TableDataUtil {

    public static List<ColumnInfo> columnsSortedByName(TableInfo table, ColumnInfo... excludedColumns) {
        return columnsSortedByName(table == null ? null : table.getColumns(), excludedColumns);
    }

    public static List<ColumnInfo> columnsSortedByName(Collection<ColumnInfo> unsortedColumns, ColumnInfo... excludedColumns) {
        return sortedByName(unsortedColumns, (t1, t2) -> t1.getColumnName().compareToIgnoreCase(t2.getColumnName()), excludedColumns);
    }

    public static List<TableInfo> tablesSortedByName(TableContext tableContext, TableInfo... excludedTables) {
        return tablesSortedByName(tableContext == null ? null : tableContext.allTables(), excludedTables);
    }

    public static List<TableInfo> tablesSortedByName(Collection<TableInfo> unsortedTables, TableInfo... excludedTables) {
        return sortedByName(unsortedTables, (t1, t2) -> t1.tableName().compareToIgnoreCase(t2.tableName()), excludedTables);
    }

    private static <T> List<T> sortedByName(Collection<T> unsorted, Comparator<T> comparator, T... exclusions) {
        if (unsorted == null) {
            return Collections.emptyList();
        }

        List<T> retList = removeExclusionsFrom(unsorted, comparator, exclusions);
        Collections.sort(retList, comparator);

        return retList;
    }

    private static <T> List<T> removeExclusionsFrom(Collection<T> collectionOfT, Comparator<T> comparator, T... exclusions) {
        List<T> retList = new ArrayList<>();

        for (T t : collectionOfT) {
            boolean excluded = false;
            for (T excludedT : exclusions) {
                if (comparator.compare(t, excludedT) == 0) {
                    excluded = true;
                    break;
                }
            }
            if (!excluded) {
                retList.add(t);
            }
        }

        return retList;
    }
}
