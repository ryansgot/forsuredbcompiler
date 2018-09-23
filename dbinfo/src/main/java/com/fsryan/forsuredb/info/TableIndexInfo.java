package com.fsryan.forsuredb.info;

import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import java.util.Map;

@AutoValue
public abstract class TableIndexInfo {

    public static TableIndexInfo create(@Nonnull Map<String, String> columnSortOrderMap, boolean unique) {
        return new AutoValue_TableIndexInfo(columnSortOrderMap, unique);
    }

    /**
     * <p>empty value means use DBMS default.
     * @return a Map of String to String where the key is the column name and
     * the value is the sort order for that column of this index.
     */
    @Nonnull public abstract Map<String, String> columnSortOrderMap();          // column_sort_order_map

    /**
     * @return whether this index is a unique index
     */
    public abstract boolean unique();                                           // unique
}
