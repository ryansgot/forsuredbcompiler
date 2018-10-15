package com.fsryan.forsuredb.info;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class TableIndexInfoUtil {

    public static TableIndexInfo nonCompositeUniqueIndex(String column, String sort) {
        return uniqueIndex(column, sort);
    }

    public static TableIndexInfo uniqueIndex(String... columnToSortOrderArray) {
        return create(true, columnToSortOrderArray);
    }

    public static TableIndexInfo uniqueIndex(List<String> cols, List<String> sorts) {
        return TableIndexInfo.create(true, cols, sorts);
    }

    public static TableIndexInfo nonCompositeNonUniqueIndex(String column, String sort) {
        return nonUniqueIndex(column, sort);
    }

    public static TableIndexInfo uniqueDefaultSorts(String... cols) {
        return withDefaultSorts(true, cols);
    }

    public static TableIndexInfo nonUniqueIndex(String... columnToSortOrderArray) {
        return create(false, columnToSortOrderArray);
    }

    public static TableIndexInfo nonUniqueIndex(List<String> cols, List<String> sorts) {
        return TableIndexInfo.create(false, cols, sorts);
    }

    public static TableIndexInfo nonUniqueDefaultSorts(String... cols) {
        return withDefaultSorts(false, cols);
    }

    private static TableIndexInfo withDefaultSorts(boolean unique, String... cols) {
        String[] sorts = new String[cols.length];
        Arrays.fill(sorts, "");
        return TableIndexInfo.create(unique, Arrays.asList(cols), Arrays.asList(sorts));
    }

    private static TableIndexInfo create(boolean unique, String... columnToSortOrderArray) {
        if (columnToSortOrderArray.length % 2 != 0) {
            throw new IllegalArgumentException("input array must map column names (even indices) to sort orders (odd indices); an even-sized array is required");
        }

        List<String> cols = new ArrayList<>(columnToSortOrderArray.length / 2);
        List<String> sorts = new ArrayList<>(cols.size());
        for (int idx = 0; idx < columnToSortOrderArray.length; idx += 2) {
            cols.add(columnToSortOrderArray[idx]);
            sorts.add(columnToSortOrderArray[idx + 1]);
        }
        return TableIndexInfo.create(unique, cols, sorts);
    }
}
