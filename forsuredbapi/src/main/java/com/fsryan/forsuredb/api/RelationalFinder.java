package com.fsryan.forsuredb.api;

import java.util.Date;

public class RelationalFinder<U, R extends RecordContainer, G extends FSGetApi, S extends FSSaveApi<U>, F extends RelationalFinder<U, R, G, S, F, O>, O extends RelationalOrderBy<U, R, G, S, F, O>> extends Finder {

    public interface Conjunction<U, R extends RecordContainer, G extends FSGetApi, S extends FSSaveApi<U>, F extends RelationalFinder<U, R, G, S, F, O>, O extends RelationalOrderBy<U, R, G, S, F, O>> {
        Resolver<U, R, G, S, F, O> andFinally();
        F and();
        F or();
    }

    public interface Between<U, R extends RecordContainer, G extends FSGetApi, S extends FSSaveApi<U>, F extends RelationalFinder<U, R, G, S, F, O>, O extends RelationalOrderBy<U, R, G, S, F, O>> {
        <T> Conjunction<U, R, G, S, F, O> and(T high);
        <T> Conjunction<U, R, G, S, F, O> andInclusive(T high);
    }

    protected final Conjunction<U, R, G, S, F, O> conjunction;

    public RelationalFinder(final Resolver<U, R, G, S, F, O> resolver) {
        super(resolver.tableName());
        conjunction = new Conjunction<U, R, G, S, F, O>() {
            @Override
            public Resolver<U, R, G, S, F, O> andFinally() {
                return resolver;
            }

            @Override
            public F and() {
                if (whereBuf.length() > 0) {
                    surroundCurrentWhereWithParens();
                    whereBuf.append(" AND ");
                }
                return (F) RelationalFinder.this;
            }

            @Override
            public F or() {
                if (whereBuf.length() > 0) {
                    surroundCurrentWhereWithParens();
                    whereBuf.append(" OR ");
                }
                return (F) RelationalFinder.this;
            }
        };
    }

    /**
     * <p>
     *   add criteria to a query that requires exactMatch for _id
     * </p>
     * @param exactMatch
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<U, R, G, S, F, O>  byId(long exactMatch) {
        addToBuf("_id", Finder.Operator.EQ, exactMatch);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires exclusion for _id
     * </p>
     * @param exclusion
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<U, R, G, S, F, O> byIdNot(long exclusion) {
        addToBuf("_id", Finder.Operator.NE, exclusion);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveUpperBound for _id
     * </p>
     * @param nonInclusiveUpperBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<U, R, G, S, F, O>  byIdLessThan(long nonInclusiveUpperBound) {
        addToBuf("_id", Finder.Operator.LT, nonInclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveLowerBound for _id
     * </p>
     * @param nonInclusiveLowerBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<U, R, G, S, F, O>  byIdGreaterThan(long nonInclusiveLowerBound) {
        addToBuf("_id", Finder.Operator.GT, nonInclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveUpperBound for _id
     * </p>
     * @param inclusiveUpperBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<U, R, G, S, F, O>  byIdLessThanInclusive(long inclusiveUpperBound) {
        addToBuf("_id", Finder.Operator.LE, inclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveLowerBound for _id
     * </p>
     * @param inclusiveLowerBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<U, R, G, S, F, O>  byIdGreaterThanInclusive(long inclusiveLowerBound) {
        addToBuf("_id", Finder.Operator.GE, inclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveLowerBound for _id
     * </p>
     * @param nonInclusiveLowerBound
     * @return a {@link Between} that allows you to provide an upper bound for this criteria
     */
    public Between<U, R, G, S, F, O>  byIdBetween(long nonInclusiveLowerBound) {
        addToBuf("_id", Finder.Operator.GT, nonInclusiveLowerBound);
        return createBetween(long.class, "_id");
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveLowerBound for _id
     * </p>
     * @param inclusiveLowerBound
     * @return a {@link Between} that allows you to provide an upper bound for this criteria
     */
    public Between<U, R, G, S, F, O>  byIdBetweenInclusive(long inclusiveLowerBound) {
        addToBuf("_id", Finder.Operator.GE, inclusiveLowerBound);
        return createBetween(long.class, "_id");
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveUpperBound for created
     * </p>
     * @param nonInclusiveUpperBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<U, R, G, S, F, O>  byCreatedBefore(Date nonInclusiveUpperBound) {
        addToBuf("created", Finder.Operator.LT, nonInclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveLowerBound for created
     * </p>
     * @param nonInclusiveLowerBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<U, R, G, S, F, O>  byCreatedAfter(Date nonInclusiveLowerBound) {
        addToBuf("created", Finder.Operator.GT, nonInclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveUpperBound for created
     * </p>
     * @param inclusiveUpperBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<U, R, G, S, F, O>  byCreatedBeforeInclusive(Date inclusiveUpperBound) {
        addToBuf("created", Finder.Operator.LE, inclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveLowerBound for created
     * </p>
     * @param inclusiveLowerBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<U, R, G, S, F, O>  byCreatedAfterInclusive(Date inclusiveLowerBound) {
        addToBuf("created", Finder.Operator.GE, inclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveLowerBound for created
     * </p>
     * @param nonInclusiveLowerBound
     * @return a {@link Between} that allows you to provide an upper bound for this criteria
     */
    public Between<U, R, G, S, F, O>  byCreatedBetween(Date nonInclusiveLowerBound) {
        addToBuf("created", Finder.Operator.GT, nonInclusiveLowerBound);
        return createBetween(java.util.Date.class, "created");
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveLowerBound for created
     * </p>
     * @param inclusiveLowerBound
     * @return a {@link Between} that allows you to provide an upper bound for this criteria
     */
    public Between<U, R, G, S, F, O>  byCreatedBetweenInclusive(Date inclusiveLowerBound) {
        addToBuf("created", Finder.Operator.GE, inclusiveLowerBound);
        return createBetween(java.util.Date.class, "created");
    }

    /**
     * <p>
     *   add criteria to a query that requires exactMatch for created
     * </p>
     * @param exactMatch
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<U, R, G, S, F, O>  byCreatedOn(Date exactMatch) {
        addToBuf("created", Finder.Operator.EQ, exactMatch);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires exclusion for created
     * </p>
     * @param exclusion
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<U, R, G, S, F, O>  byNotCreatedOn(Date exclusion) {
        addToBuf("created", Finder.Operator.NE, exclusion);
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
    public Conjunction<U, R, G, S, F, O>  byDeleted() {
        addToBuf("deleted", Finder.Operator.EQ, 1);
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
    public Conjunction<U, R, G, S, F, O>  byNotDeleted() {
        addToBuf("deleted", Finder.Operator.NE, 1);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveUpperBound for modified
     * </p>
     * @param nonInclusiveUpperBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<U, R, G, S, F, O>  byModifiedBefore(Date nonInclusiveUpperBound) {
        addToBuf("modified", Finder.Operator.LT, nonInclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveLowerBound for modified
     * </p>
     * @param nonInclusiveLowerBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<U, R, G, S, F, O>  byModifiedAfter(Date nonInclusiveLowerBound) {
        addToBuf("modified", Finder.Operator.GT, nonInclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveUpperBound for modified
     * </p>
     * @param inclusiveUpperBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<U, R, G, S, F, O>  byModifiedBeforeInclusive(Date inclusiveUpperBound) {
        addToBuf("modified", Finder.Operator.LE, inclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveLowerBound for modified
     * </p>
     * @param inclusiveLowerBound
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<U, R, G, S, F, O>  byModifiedAfterInclusive(Date inclusiveLowerBound) {
        addToBuf("modified", Finder.Operator.GE, inclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveLowerBound for modified
     * </p>
     * @param nonInclusiveLowerBound
     * @return a {@link Between} that allows you to provide an upper bound for this criteria
     */
    public Between<U, R, G, S, F, O>  byModifiedBetween(Date nonInclusiveLowerBound) {
        addToBuf("modified", Finder.Operator.GT, nonInclusiveLowerBound);
        return createBetween(java.util.Date.class, "modified");
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveLowerBound for modified
     * </p>
     * @param inclusiveLowerBound
     * @return a {@link Between} that allows you to provide an upper bound for this criteria
     */
    public Between<U, R, G, S, F, O> byModifiedBetweenInclusive(Date inclusiveLowerBound) {
        addToBuf("modified", Finder.Operator.GE, inclusiveLowerBound);
        return createBetween(java.util.Date.class, "modified");
    }

    /**
     * <p>
     *   add criteria to a query that requires exactMatch for modified
     * </p>
     * @param exactMatch
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<U, R, G, S, F, O>  byModifiedOn(Date exactMatch) {
        addToBuf("modified", Finder.Operator.EQ, exactMatch);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires exclusion for modified
     * </p>
     * @param exclusion
     * @return a {@link Conjunction} that allows you to continue adding more query criteria
     */
    public Conjunction<U, R, G, S, F, O>  byNotModifiedOn(Date exclusion) {
        addToBuf("modified", Finder.Operator.NE, exclusion);
        return conjunction;
    }

    protected final <T> Between<U, R, G, S, F, O> createBetween(Class<T> qualifiedType, final String column) {
        return new Between<U, R, G, S, F, O>() {
            @Override
            public <T> Conjunction<U, R, G, S, F, O> and(T high) {
                return conjoin(Operator.LT, high);
            }

            @Override
            public <T> Conjunction<U, R, G, S, F, O> andInclusive(T high) {
                return conjoin(Operator.LE, high);
            }

            private <T> Conjunction<U, R, G, S, F, O> conjoin(Operator o, T high) {
                whereBuf.append(" AND ");
                addToBuf(column, o, high);
                return conjunction;
            }
        };
    }
}