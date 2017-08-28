package com.fsryan.forsuredb.api;

import lombok.AccessLevel;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *   <i>WARNING:</i> order-by methods on {@link java.math.BigDecimal} columns may not
 *   sort based upon numeric value, depending upon DBMS. For example, current known
 *   SQLite fsryan integrations store BigDecimal values as string columns, and
 *   therefore, will sort as strings rather than numbers. This is currently a limitation.
 * </p>
 */
public abstract class OrderBy<R extends Resolver, O extends OrderBy<R, O>> {

    public static final int ORDER_ASC = 0;
    public static final int ORDER_DESC = -1;

    @lombok.Getter(AccessLevel.PROTECTED) private final List<FSOrdering> orderings = new ArrayList<>();
    protected final String tableName;
    protected final Conjunction.And<R, O> conjunction;

    public OrderBy(final R resolver) {
        tableName = resolver.tableName();
        conjunction = new Conjunction.And<R, O>() {
            @Override
            public R then() {
                return resolver;
            }

            @Override
            public O and() {
                return (O) OrderBy.this;
            }
        };
    }

    /**
     * <p>
     *   Order the results of the query by _id
     * </p>
     * @param order the direction to order the results {@link #ORDER_ASC} (or 0 or more) or
     * {@link #ORDER_DESC} (or -1 or less)
     * @return a {@link Conjunction.And} that allows for either adding to the orderBy or continue
     * adding other query parameters
     */
    public Conjunction.And<R, O> byId(int order) {
        appendOrder("_id", order);
        return conjunction;
    }

    /**
     * <p>
     *   Order the results of the query by deleted. Because it is assumed that true and false
     *   values are stored as 1 and 0 respectively, {@link #ORDER_ASC} will cause all
     *   non-deleted records to be followed by all deleted records. {@link #ORDER_DESC} will
     *   have the opposite effect.
     * </p>
     * @param order the direction to order the results {@link #ORDER_ASC} (or 0 or more) or
     * {@link #ORDER_DESC} (or -1 or less)
     * @return a {@link Conjunction.And} that allows for either adding to the orderBy or continue
     * adding other query parameters
     */
    public Conjunction.And<R, O> byDeleted(int order) {
        appendOrder("deleted", order);
        return conjunction;
    }

    /**
     * <p>
     *   Order the results of the query by date created
     * </p>
     * @param order the direction to order the results {@link #ORDER_ASC} (or 0 or more) or
     * {@link #ORDER_DESC} (or -1 or less)
     * @return a {@link Conjunction.And} that allows for either adding to the orderBy or continue
     * adding other query parameters
     */
    public Conjunction.And<R, O> byCreated(int order) {
        appendOrder("created", order);
        return conjunction;
    }

    /**
     * <p>
     *   Order the results of the query by date modified
     * </p>
     * @param order the direction to order the results {@link #ORDER_ASC} (or 0 or more) or
     * {@link #ORDER_DESC} (or -1 or less)
     * @return a {@link Conjunction.And} that allows for either adding to the orderBy or continue
     * adding other query parameters
     */
    public Conjunction.And<R, O> byModified(int order) {
        appendOrder("modified", order);
        return conjunction;
    }

    protected void appendOrder(String columnName, int order) {
        // Since we allow app developer to set the order, assume >= 0 means "ascending" and
        // < 0 means "descending"
        orderings.add(new FSOrdering(tableName, columnName, order < 0 ? ORDER_DESC : ORDER_ASC));
    }

    /*package*/ void appendOrderings(List<FSOrdering> orderingsToAdd) {
        if (orderingsToAdd == null || orderingsToAdd.isEmpty()) {
            return;
        }
        this.orderings.addAll(orderingsToAdd);
    }
}
