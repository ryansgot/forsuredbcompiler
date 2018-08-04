package com.fsryan.forsuredb.api;

import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;

/**
 * A description of the ordering of a query--either in part or in whole
 */
@AutoValue
public abstract class FSOrdering {

    public static FSOrdering create(@Nonnull String table, @Nonnull String column, int direction) {
        return new AutoValue_FSOrdering(table, column, direction);
    }

    /**
     * For column-disambiguation purposes, this is the table name of the column
     */
    public abstract String table();

    /**
     * The column to order by
     */
    public abstract String column();
    /**
     * The direction of the ordering (either {@link OrderBy#ORDER_ASC} or {@link OrderBy#ORDER_DESC}
     */
    public abstract int direction();
}
