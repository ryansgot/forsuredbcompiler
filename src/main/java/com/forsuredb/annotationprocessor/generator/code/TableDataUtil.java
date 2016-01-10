package com.forsuredb.annotationprocessor.generator.code;

import com.forsuredb.annotationprocessor.TableContext;
import com.forsuredb.annotationprocessor.info.ColumnInfo;
import com.forsuredb.annotationprocessor.info.TableInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/*package*/ class TableDataUtil {

    public static List<ColumnInfo> columnsSortedByName(TableInfo table, ColumnInfo... excludedColumns) {
        return columnsSortedByName(table == null ? null : table.getColumns(), excludedColumns);
    }

    public static List<ColumnInfo> columnsSortedByName(Collection<ColumnInfo> unsortedColumns, ColumnInfo... excludedColumns) {
        return sortedByName(unsortedColumns, new Comparator<ColumnInfo>() {
            @Override
            public int compare(ColumnInfo t1, ColumnInfo t2) {
                return t1.getColumnName().compareToIgnoreCase(t2.getColumnName());
            }
        }, excludedColumns);
    }

    public static List<TableInfo> tablesSortedByName(TableContext tableContext, TableInfo... excludedTables) {
        return tablesSortedByName(tableContext == null ? null : tableContext.allTables(), excludedTables);
    }

    public static List<TableInfo> tablesSortedByName(Collection<TableInfo> unsortedTables, TableInfo... excludedTables) {
        return sortedByName(unsortedTables, new Comparator<TableInfo>() {
            @Override
            public int compare(TableInfo t1, TableInfo t2) {
                return t1.getTableName().compareToIgnoreCase(t2.getTableName());
            }
        }, excludedTables);
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
