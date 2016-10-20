package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.api.sqlgeneration.Sql;

import java.util.Date;

public class DocStoreFinder<T, U, R extends RecordContainer, G extends FSDocStoreGetApi<T>, S extends FSDocStoreSaveApi<U, T>, F extends DocStoreFinder<T, U, R, G, S, F, O>, O extends DocStoreOrderBy<T, U, R, G, S, F, O>> extends Finder {

    public interface Conjunction<T, U, R extends RecordContainer, G extends FSDocStoreGetApi<T>, S extends FSDocStoreSaveApi<U, T>, F extends DocStoreFinder<T, U, R, G, S, F, O>, O extends DocStoreOrderBy<T, U, R, G, S, F, O>> {
        DocStoreResolver<T, U, R, G, S, F, O> then();
        F and();
        F or();
    }

    public interface Between<T, U, R extends RecordContainer, G extends FSDocStoreGetApi<T>, S extends FSDocStoreSaveApi<U, T>, F extends DocStoreFinder<T, U, R, G, S, F, O>, O extends DocStoreOrderBy<T, U, R, G, S, F, O>> {
        <Typ> Conjunction<T, U, R, G, S, F, O> and(Typ high);
        <Typ> Conjunction<T, U, R, G, S, F, O> andInclusive(Typ high);
    }

    protected final Conjunction<T, U, R, G, S, F, O> conjunction;


    public DocStoreFinder(final DocStoreResolver<T, U, R, G, S, F, O> resolver) {
        super(resolver.tableName());
        conjunction = new Conjunction<T, U, R, G, S, F, O>() {
            @Override
            public DocStoreResolver<T, U, R, G, S, F, O> then() {
                return resolver;
            }

            @Override
            public F and() {
                if (whereBuf.length() > 0) {
                    surroundCurrentWhereWithParens();
                    whereBuf.append(" ").append(Sql.generator().and()).append(" ");
                }
                return (F) DocStoreFinder.this;
            }

            @Override
            public F or() {
                if (whereBuf.length() > 0) {
                    surroundCurrentWhereWithParens();
                    whereBuf.append(" ").append(Sql.generator().or()).append(" ");
                }
                return (F) DocStoreFinder.this;
            }
        };
    }

    /**
     * <p>
     *   add criteria to a query that requires exactMatch for _id
     * </p>
     * @param exactMatch the _id of the only record to include in the results
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byId(long exactMatch) {
        addToBuf("_id", OP_EQ, exactMatch);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires exclusion for _id
     * </p>
     * @param exclusion the _id of the record to exclude from the results
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byIdNot(long exclusion) {
        addToBuf("_id", OP_NE, exclusion);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveUpperBound for _id
     * </p>
     * @param nonInclusiveUpperBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byIdLessThan(long nonInclusiveUpperBound) {
        addToBuf("_id", OP_LT, nonInclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveLowerBound for _id
     * </p>
     * @param nonInclusiveLowerBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byIdGreaterThan(long nonInclusiveLowerBound) {
        addToBuf("_id", OP_GT, nonInclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveUpperBound for _id
     * </p>
     * @param inclusiveUpperBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byIdLessThanInclusive(long inclusiveUpperBound) {
        addToBuf("_id", OP_LE, inclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveLowerBound for _id
     * </p>
     * @param inclusiveLowerBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byIdGreaterThanInclusive(long inclusiveLowerBound) {
        addToBuf("_id", OP_GE, inclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveLowerBound for _id
     * </p>
     * @param nonInclusiveLowerBound
     * @return a {@link Between} that allows you to provide an upper bound for this criteria
     */
    public Between<T, U, R, G, S, F, O> byIdBetween(long nonInclusiveLowerBound) {
        addToBuf("_id", OP_GT, nonInclusiveLowerBound);
        return createBetween(long.class, "_id");
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveLowerBound for _id
     * </p>
     * @param inclusiveLowerBound
     * @return a {@link Between} that allows you to provide an upper bound for this criteria
     */
    public Between<T, U, R, G, S, F, O> byIdBetweenInclusive(long inclusiveLowerBound) {
        addToBuf("_id", OP_GE, inclusiveLowerBound);
        return createBetween(long.class, "_id");
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveUpperBound for created
     * </p>
     * @param nonInclusiveUpperBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byCreatedBefore(Date nonInclusiveUpperBound) {
        addToBuf("created", OP_LT, nonInclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveLowerBound for created
     * </p>
     * @param nonInclusiveLowerBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byCreatedAfter(Date nonInclusiveLowerBound) {
        addToBuf("created", OP_GT, nonInclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveUpperBound for created
     * </p>
     * @param inclusiveUpperBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byCreatedBeforeInclusive(Date inclusiveUpperBound) {
        addToBuf("created", OP_LE, inclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveLowerBound for created
     * </p>
     * @param inclusiveLowerBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byCreatedAfterInclusive(Date inclusiveLowerBound) {
        addToBuf("created", OP_GE, inclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveLowerBound for created
     * </p>
     * @param nonInclusiveLowerBound
     * @return a {@link Between} that allows you to provide an upper bound for this criteria
     */
    public Between<T, U, R, G, S, F, O> byCreatedBetween(Date nonInclusiveLowerBound) {
        addToBuf("created", OP_GT, nonInclusiveLowerBound);
        return createBetween(java.util.Date.class, "created");
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveLowerBound for created
     * </p>
     * @param inclusiveLowerBound
     * @return a {@link Between} that allows you to provide an upper bound for this criteria
     */
    public Between<T, U, R, G, S, F, O> byCreatedBetweenInclusive(Date inclusiveLowerBound) {
        addToBuf("created", OP_GE, inclusiveLowerBound);
        return createBetween(java.util.Date.class, "created");
    }

    /**
     * <p>
     *   add criteria to a query that requires exactMatch for created
     * </p>
     * @param exactMatch
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byCreatedOn(Date exactMatch) {
        addToBuf("created", OP_EQ, exactMatch);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires exclusion for created
     * </p>
     * @param exclusion
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byNotCreatedOn(Date exclusion) {
        addToBuf("created", OP_NE, exclusion);
        return conjunction;
    }

    /**
     * <p>
     *   because booleans are represented as 0 (false) and 1 (true), there is no need for an
     *   argument to this method. If you want to match records for which deleted = false,
     *   then call {@link #byNotDeleted()}
     * </p>
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     * @see #byNotDeleted()
     */
    public Conjunction<T, U, R, G, S, F, O> byDeleted() {
        addToBuf("deleted", OP_EQ, 1);
        return conjunction;
    }

    /**
     * <p>
     *   because booleans are represented as 0 (false) and 1 (true), there is no need for an
     *   argument to this method. If you want to match records for which deleted = true,
     *   then call {@link #byDeleted()}
     * </p>
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     * @see #byDeleted()
     */
    public Conjunction<T, U, R, G, S, F, O> byNotDeleted() {
        addToBuf("deleted", OP_NE, 1);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveUpperBound for modified
     * </p>
     * @param nonInclusiveUpperBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byModifiedBefore(Date nonInclusiveUpperBound) {
        addToBuf("modified", OP_LT, nonInclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveLowerBound for modified
     * </p>
     * @param nonInclusiveLowerBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byModifiedAfter(Date nonInclusiveLowerBound) {
        addToBuf("modified", OP_GT, nonInclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveUpperBound for modified
     * </p>
     * @param inclusiveUpperBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byModifiedBeforeInclusive(Date inclusiveUpperBound) {
        addToBuf("modified", OP_LE, inclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveLowerBound for modified
     * </p>
     * @param inclusiveLowerBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byModifiedAfterInclusive(Date inclusiveLowerBound) {
        addToBuf("modified", OP_GE, inclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveLowerBound for modified
     * </p>
     * @param nonInclusiveLowerBound
     * @return a {@link Between} that allows you to provide an upper bound for this criteria
     */
    public Between<T, U, R, G, S, F, O> byModifiedBetween(Date nonInclusiveLowerBound) {
        addToBuf("modified", OP_GT, nonInclusiveLowerBound);
        return createBetween(java.util.Date.class, "modified");
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveLowerBound for modified
     * </p>
     * @param inclusiveLowerBound
     * @return a {@link Between} that allows you to provide an upper bound for this criteria
     */
    public Between<T, U, R, G, S, F, O> byModifiedBetweenInclusive(Date inclusiveLowerBound) {
        addToBuf("modified", OP_GE, inclusiveLowerBound);
        return createBetween(java.util.Date.class, "modified");
    }

    /**
     * <p>
     *   add criteria to a query that requires exactMatch for modified
     * </p>
     * @param exactMatch
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byModifiedOn(Date exactMatch) {
        addToBuf("modified", OP_EQ, exactMatch);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires exclusion for modified
     * </p>
     * @param exclusion
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byNotModifiedOn(Date exclusion) {
        addToBuf("modified", OP_NE, exclusion);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires exactMatch for class_name
     * </p>
     * @param exactMatch
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byClassName(String exactMatch) {
        addToBuf("class_name", OP_EQ, exactMatch);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires exclusion for class_name
     * </p>
     * @param exclusion
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byClassNameNot(String exclusion) {
        addToBuf("class_name", OP_NE, exclusion);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveUpperBound for class_name
     * </p>
     * @param nonInclusiveUpperBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byClassNameLessThan(String nonInclusiveUpperBound) {
        addToBuf("class_name", OP_LT, nonInclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveLowerBound for class_name
     * </p>
     * @param nonInclusiveLowerBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byClassNameGreaterThan(String nonInclusiveLowerBound) {
        addToBuf("class_name", OP_GT, nonInclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveUpperBound for class_name
     * </p>
     * @param inclusiveUpperBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byClassNameLessThanInclusive(String inclusiveUpperBound) {
        addToBuf("class_name", OP_LE, inclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveLowerBound for class_name
     * </p>
     * @param inclusiveLowerBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byClassNameGreaterThanInclusive(String inclusiveLowerBound) {
        addToBuf("class_name", OP_GE, inclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveLowerBound for class_name
     * </p>
     * @param nonInclusiveLowerBound
     * @return a {@link Between} that allows you to provide an upper bound for this criteria
     */
    public Between<T, U, R, G, S, F, O> byClassNameBetween(String nonInclusiveLowerBound) {
        addToBuf("class_name", OP_GT, nonInclusiveLowerBound);
        return createBetween(String.class, "class_name");
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveLowerBound for class_name
     * </p>
     * @param inclusiveLowerBound
     * @return a {@link Between} that allows you to provide an upper bound for this criteria
     */
    public Between<T, U, R, G, S, F, O> byClassNameBetweenInclusive(String inclusiveLowerBound) {
        addToBuf("class_name", OP_GE, inclusiveLowerBound);
        return createBetween(String.class, "class_name");
    }

    /**
     * <p>
     *   add criteria to a query that requires like for class_name
     * </p>
     * @param like
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<T, U, R, G, S, F, O> byClassNameLike(String like) {
        addToBuf("class_name", OP_LIKE, like);
        return conjunction;
    }

    protected final <Typ> Between<T, U, R, G, S, F, O> createBetween(final Class<Typ> cls, final String column) {
        return new Between<T, U, R, G, S, F, O>() {
            @Override
            public <Typ> Conjunction<T, U, R, G, S, F, O> and(Typ high) {
                return conjoin(OP_LT, high);
            }

            @Override
            public <Typ> Conjunction<T, U, R, G, S, F, O> andInclusive(Typ high) {
                return conjoin(OP_LE, high);
            }

            private <Typ> Conjunction<T, U, R, G, S, F, O> conjoin(int operator, Typ high) {
                whereBuf.append(" ").append(Sql.generator().and()).append(" ");
                addToBuf(column, operator, high);
                return conjunction;
            }
        };
    }
}
