package com.fsryan.forsuredb.api;

/**
 *
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
public abstract class RelationalOrderBy<U, R extends RecordContainer, G extends FSGetApi, S extends FSSaveApi<U>, F extends RelationalFinder<U, R, G, S, F, O>, O extends RelationalOrderBy<U, R, G, S, F, O>> extends OrderBy {

    public interface Conjunction<U, R extends RecordContainer, G extends FSGetApi, S extends FSSaveApi<U>, F extends RelationalFinder<U, R, G, S, F, O>, O extends RelationalOrderBy<U, R, G, S, F, O>> {
        Resolver<U, R, G, S, F, O> andFinally();
        O and();
    }

    protected final Conjunction<U, R, G, S, F, O> conjunction;

    public RelationalOrderBy(final Resolver<U, R, G, S, F, O> resolver) {
        super(resolver.tableName());
        conjunction = new Conjunction<U, R, G, S, F, O>() {
            @Override
            public Resolver<U, R, G, S, F, O> andFinally() {
                return resolver;
            }

            @Override
            public O and() {
                return (O) RelationalOrderBy.this;
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
        appendOrder("modified", order);
        return conjunction;
    }
}
