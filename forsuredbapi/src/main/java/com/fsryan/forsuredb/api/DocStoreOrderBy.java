package com.fsryan.forsuredb.api;

/**
 * <p>
 *   Use this class to order the results of any of your queries. For the purpose of type
 *   safety, it is parameterized the same way as the {@link Resolver} class.
 * </p>
 * <p>
 *   The order-by methods for all of the base columns are defined within this class:
 *   <ul>
 *     <li>{@link #byId(int)}</li>
 *     <li>{@link #byCreated(int)}</li>
 *     <li>{@link #byDeleted(int)}</li>
 *     <li>{@link #byModified(int)}</li>
 *   </ul>
 *   Other order-by methods will be defined within the concrete implementations of the
 *   OrderBy class.
 * </p>
 * @param <T> The type of the base class for the {@link DocStoreResolver}
 * @param <U> The result parameter for insertion queries your project uses
 * @param <R> The extension of {@link RecordContainer} your project uses
 * @param <G> The extension of {@link FSGetApi} that defines the table
 * @param <S> The extension of {@link FSSaveApi} parameterized by the result parameter
 *           your project uses that was generated from the {@link FSGetApi}
 * @param <F> The extension of {@link Finder} parameterized with the same parameters
 *           as this class. This {@link Finder} was also generated from the {@link FSGetApi}
 * @param <O> The specific extension of the DocStoreOrderBy class
 * @see OrderBy
 * @see RelationalOrderBy
 */
public class DocStoreOrderBy<T, U, R extends RecordContainer, G extends FSDocStoreGetApi<T>, S extends FSDocStoreSaveApi<U, T>, F extends DocStoreFinder<T, U, R, G, S, F, O>, O extends DocStoreOrderBy<T, U, R, G, S, F, O>> extends OrderBy {

    public interface Conjunction<T, U, R extends RecordContainer, G extends FSDocStoreGetApi<T>, S extends FSDocStoreSaveApi<U, T>, F extends DocStoreFinder<T, U, R, G, S, F, O>, O extends DocStoreOrderBy<T, U, R, G, S, F, O>> {
        DocStoreResolver<T, U, R, G, S, F, O> then();
        O and();
    }

    protected final Conjunction<T, U, R, G, S, F, O> conjunction;

    public DocStoreOrderBy(final DocStoreResolver<T, U, R, G, S, F, O> resolver) {
        super(resolver.tableName());
        conjunction = new Conjunction<T, U, R, G, S, F, O>() {
            @Override
            public DocStoreResolver<T, U, R, G, S, F, O> then() {
                return resolver;
            }

            @Override
            public O and() {
                return (O) DocStoreOrderBy.this;
            }
        };
    }

    /**
     * <p>
     *   Order the results of the query by _id
     * </p>
     * @param order the direction to order the results {@link #ORDER_ASC} (or 0 or more) or
     * {@link #ORDER_DESC} (or -1 or less)
     * @return a {@link Conjunction} that allows for either adding to the orderBy or continue
     * adding other query parameters
     */
    public Conjunction<T, U, R, G, S, F, O> byId(int order) {
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
     * @return a {@link Conjunction} that allows for either adding to the orderBy or continue
     * adding other query parameters
     */
    public Conjunction<T, U, R, G, S, F, O> byDeleted(int order) {
        appendOrder("deleted", order);
        return conjunction;
    }

    /**
     * <p>
     *   Order the results of the query by date created
     * </p>
     * @param order the direction to order the results {@link #ORDER_ASC} (or 0 or more) or
     * {@link #ORDER_DESC} (or -1 or less)
     * @return a {@link Conjunction} that allows for either adding to the orderBy or continue
     * adding other query parameters
     */
    public Conjunction<T, U, R, G, S, F, O> byCreated(int order) {
        appendOrder("created", order);
        return conjunction;
    }

    /**
     * <p>
     *   Order the results of the query by date updated
     * </p>
     * @param order the direction to order the results {@link #ORDER_ASC} (or 0 or more) or
     * {@link #ORDER_DESC} (or -1 or less)
     * @return a {@link Conjunction} that allows for either adding to the orderBy or continue
     * adding other query parameters
     */
    public Conjunction<T, U, R, G, S, F, O> byModified(int order) {
        appendOrder("modified", order);
        return conjunction;
    }
}
