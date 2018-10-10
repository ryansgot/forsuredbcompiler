package com.fsryan.forsuredb.info;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AutoValue
public abstract class TableIndexInfo {

    /**
     * <p>columnOrder and columnSortOrder must be the same length--a poor man's
     * ordered map
     * <p>The order you declare the columns will affect the order in which the
     * composite index will be built.
     * @param unique whether the index is a unique index
     * @param columns the columns in the order they were declared
     * @param columnSortOrders the sort orders of the indices in order to not
     *                         specify, use empty string
     * @return a new {@link TableIndexInfo}
     */
    public static TableIndexInfo create(boolean unique, @Nonnull List<String> columns, @Nonnull List<String> columnSortOrders) {
        if (columns.size() < 1) {
            throw new IllegalArgumentException("an index must be on at least one column");
        }
        if (columns.size() != columnSortOrders.size()) {
            throw new IllegalArgumentException("columns and columnSortOrders form a map. They must be the same length. columns " + columns + "; columnSortOrders: " + columnSortOrders);
        }
        return new AutoValue_TableIndexInfo(unique, columns, columnSortOrders);
    }

    /**
     * <p>Utility to merge two {@link TableIndexInfo} objects, preserving the
     * integrity of the column order and column sort orders
     * @param first the first {@link TableIndexInfo}
     * @param second  {@link TableIndexInfo} to be appended
     * @return a merged {@link TableIndexInfo}
     */
    public static TableIndexInfo merge(@Nonnull TableIndexInfo first, @Nonnull TableIndexInfo second) {
        if (first.unique() != second.unique()) {
            throw new IllegalArgumentException("Composite indices cannot be both unique and non-unique: compositing '" + first + "' and '" + second + "'");
        }
        List<String> accCols = first.columns();
        List<String> accSorts = first.columnSortOrders();
        List<String> nextCols = second.columns();
        List<String> nextSorts = second.columnSortOrders();
        List<String> mergedColumns = new ArrayList<>(accCols.size() + nextCols.size());
        List<String> mergedSorts = new ArrayList<>(accSorts.size() + nextSorts.size());
        mergedColumns.addAll(accCols);
        mergedColumns.addAll(nextCols);
        mergedSorts.addAll(accSorts);
        mergedSorts.addAll(nextSorts);
        return TableIndexInfo.create(first.unique(), mergedColumns, mergedSorts);
    }

    /**
     * @return a {@link Map} which maps column name to index sort order
     */
    @Memoized
    @Nonnull
    public Map<String, String> columnSortOrderMap() {
        List<String> columns = columns();
        List<String> sorts = columnSortOrders();
        Map<String, String> ret = new HashMap<>(columns.size());
        for (int i = 0; i < columns.size(); i++) {
            ret.put(columns.get(i), sorts.get(i));
        }
        return ret;
    }

    /**
     * @return whether this index is a unique index
     */
    public abstract boolean unique();                           // unique

    /**
     * <p>empty value means use DBMS default.
     * @return a Map of String to String where the key is the column name and
     * the value is the sort order for that column of this index.
     */
    @Nonnull
    public abstract List<String> columns();                     // columns

    /**
     * @return the sort orders of the columns of the index
     */
    @Nonnull
    abstract List<String> columnSortOrders();                   // column_sort_orders
}
