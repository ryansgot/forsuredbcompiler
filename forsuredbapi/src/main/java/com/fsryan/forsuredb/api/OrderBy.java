package com.fsryan.forsuredb.api;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *   <i>WARNING:</i> order-by methods on {@link java.math.BigDecimal} columns may not
 *   sort based upon numeric value, depending upon DBMS. For example, current known
 *   SQLite fsryan integrations store BigDecimal values as string columns, and
 *   therefore, will sort as strings rather than numbers. This is currently a limitation.
 * </p>
 * <p>
 *   Use this class to order the results of any of your queries. For the purpose of type
 *   safety, it is parameterized the same way as the {@link Resolver} class.
 * </p>
 * <p>
 *   The order-by methods for all of the base columns are defined within this class:
 *   <ul>
 *     <li>{@link #byId(Order)}</li>
 *     <li>{@link #byCreated(Order)}</li>
 *     <li>{@link #byDeleted(Order)}</li>
 *     <li>{@link #byModified(Order)}</li>
 *   </ul>
 *   Other order-by methods will be defined within the concrete implementations of the
 *   OrderBy class.
 * </p>
 * @param <U> The result parameter for insertion queries your project uses
 * @param <R> The extension of {@link RecordContainer} your project uses
 * @param <G> The extension of {@link FSGetApi} that defines the table
 * @param <S> The extension of {@link FSSaveApi} parameterized by the result parameter
 *           your project uses that was generated from the {@link FSGetApi}
 * @param <F> The extension of {@link Finder} parameterized with the same parameters
 *           as this class. This {@link Finder} was also generated from the {@link FSGetApi}
 * @param <O> The specific extension of the OrderBy class
 */
public abstract class OrderBy<U, R extends RecordContainer, G extends FSGetApi, S extends FSSaveApi<U>, F extends Finder<U, R, G, S, F, O>, O extends OrderBy<U, R, G, S, F, O>> {

    public interface Conjunction<U, R extends RecordContainer, G extends FSGetApi, S extends FSSaveApi<U>, F extends Finder<U, R, G, S, F, O>, O extends OrderBy<U, R, G, S, F, O>> {
        Resolver<U, R, G, S, F, O> andFinally();
        O and();
    }

    public enum Order {
        ASC, DESC
    }

    protected final Conjunction<U, R, G, S, F, O> conjunction;

    private final List<String> orderByList;
    private final String tableName;

    public OrderBy(final Resolver<U, R, G, S, F, O> resolver) {
        tableName = resolver.tableName();
        orderByList = new ArrayList<>();
        conjunction = new Conjunction<U, R, G, S, F, O>() {
            @Override
            public Resolver<U, R, G, S, F, O> andFinally() {
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
     * @param order the direction to order the results
     * @return a {@link Conjunction} that allows for either adding to the orderBy or continue
     * adding other query parameters
     */
    public Conjunction<U, R, G, S, F, O> byId(Order order) {
        appendOrder("_id", order);
        return conjunction;
    }

    /**
     * <p>
     *   Order the results of the query by deleted. Because it is assumed that true and false
     *   values are stored as 1 and 0 respectively, {@link Order#ASC} will cause all
     *   non-deleted records to be followed by all deleted records. {@link Order#DESC} will
     *   have the opposite effect.
     * </p>
     * @param order the direction to order the results
     * @return a {@link Conjunction} that allows for either adding to the orderBy or continue
     * adding other query parameters
     */
    public Conjunction<U, R, G, S, F, O> byDeleted(Order order) {
        appendOrder("deleted", order);
        return conjunction;
    }

    /**
     * <p>
     *   Order the results of the query by date created
     * </p>
     * @param order the direction to order the results
     * @return a {@link Conjunction} that allows for either adding to the orderBy or continue
     * adding other query parameters
     */
    public Conjunction<U, R, G, S, F, O> byCreated(Order order) {
        appendOrder("created", order);
        return conjunction;
    }

    /**
     * <p>
     *   Order the results of the query by date updated
     * </p>
     * @param order the direction to order the results
     * @return a {@link Conjunction} that allows for either adding to the orderBy or continue
     * adding other query parameters
     */
    public Conjunction<U, R, G, S, F, O> byModified(Order order) {
        appendOrder("updated", order);
        return conjunction;
    }

    /**
     * @return the SQL string for the order by portion of the query
     */
    public String getOrderByString() {
        // kind of a trick, but it so happens that the toString method for ArrayList almost works
        // for the ORDER BY syntax.
        return orderByList.size() == 0 ? "" : orderByList.toString().replaceAll("(\\[|\\])", "");
    }

    protected void appendOrder(String columnName, Order order) {
        if (order != null) {
            orderByList.add(tableName + "." + columnName + " " + order.name());
        }
    }
}
