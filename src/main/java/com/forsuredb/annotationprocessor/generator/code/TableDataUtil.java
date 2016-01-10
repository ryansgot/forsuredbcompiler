package com.forsuredb.annotationprocessor.generator.code;

import com.forsuredb.annotationprocessor.info.ColumnInfo;
import com.forsuredb.annotationprocessor.info.TableInfo;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TableDataUtil {

    public static List<ColumnInfo> columnsSortedByName(TableInfo table) {
        return columnsSortedByName(table == null ? null : table.getColumns());
    }

    public static List<ColumnInfo> columnsSortedByName(Collection<ColumnInfo> unsortedColumns) {
        if (unsortedColumns == null) {
            return Collections.EMPTY_LIST;
        }

        List<ColumnInfo> columns = Lists.newArrayList(unsortedColumns);
        Collections.sort(columns, new Comparator<ColumnInfo>() {
            @Override
            public int compare(ColumnInfo c1, ColumnInfo c2) {
                return c1.getColumnName().compareToIgnoreCase(c2.getColumnName());
            }
        });
        return columns;
    }
}
