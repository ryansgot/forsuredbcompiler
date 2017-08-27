package com.fsryan.forsuredb.api;

import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * A description of the ordering of a query--either in part or in whole
 */
@ToString
@AllArgsConstructor
public class FSOrdering {

    /**
     * The column to order by
     */
    public final String column;
    /**
     * The direction of the ordering (either {@link OrderBy#ORDER_ASC} or {@link OrderBy#ORDER_DESC}
     */
    public final int direction;
}
