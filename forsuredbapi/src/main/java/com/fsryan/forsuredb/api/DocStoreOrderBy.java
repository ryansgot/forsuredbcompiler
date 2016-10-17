package com.fsryan.forsuredb.api;

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
     * @param order the direction to order the results
     * @return a {@link Conjunction} that allows for either adding to the orderBy or continue
     * adding other query parameters
     */
    public Conjunction<T, U, R, G, S, F, O> byId(Order order) {
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
    public Conjunction<T, U, R, G, S, F, O> byDeleted(Order order) {
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
    public Conjunction<T, U, R, G, S, F, O> byCreated(Order order) {
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
    public Conjunction<T, U, R, G, S, F, O> byModified(Order order) {
        appendOrder("modified", order);
        return conjunction;
    }
}
