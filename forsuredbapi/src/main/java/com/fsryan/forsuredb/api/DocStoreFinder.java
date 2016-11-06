package com.fsryan.forsuredb.api;

public class DocStoreFinder<R extends DocStoreResolver, F extends DocStoreFinder<R, F>> extends Finder<R, F> {
    
    public DocStoreFinder(R resolver) {
        super(resolver);
    }

    /**
     * <p>
     *     add criteria to a query that is a convenience for calling {@link #byClassName(String)} with the
     *     {@link Class} object instead of the fully qualified class name.
     * </p>
     * @param exactClassMatch
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     * @see #byClassName(String)
     */
    public Conjunction.AndOr<R, F> byClass(Class exactClassMatch) {
        return byClassName(exactClassMatch == null ? "" : exactClassMatch.getName());
    }

    /**
     * <p>
     *     add criteria to a query that is a convenience for calling {@link #byClassNameNot(String)} with the
     *     {@link Class} object instead of the fully qualified class name.
     * </p>
     * @param exclusion
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     * @see #byClassNameNot(String)
     */
    public Conjunction.AndOr<R, F> byClassNot(Class exclusion) {
        return byClassNameNot(exclusion == null ? "" : exclusion.getName());
    }

    /**
     * <p>
     *   add criteria to a query that requires exact match for class_name
     * </p>
     * @param exactMatch
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byClassName(String exactMatch) {
        addToBuf("class_name", OP_EQ, exactMatch);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires exclusion for class_name
     * </p>
     * @param exclusion
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byClassNameNot(String exclusion) {
        addToBuf("class_name", OP_NE, exclusion);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveUpperBound for class_name
     * </p>
     * @param nonInclusiveUpperBound
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byClassNameLessThan(String nonInclusiveUpperBound) {
        addToBuf("class_name", OP_LT, nonInclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveLowerBound for class_name
     * </p>
     * @param nonInclusiveLowerBound
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byClassNameGreaterThan(String nonInclusiveLowerBound) {
        addToBuf("class_name", OP_GT, nonInclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveUpperBound for class_name
     * </p>
     * @param inclusiveUpperBound
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byClassNameLessThanInclusive(String inclusiveUpperBound) {
        addToBuf("class_name", OP_LE, inclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveLowerBound for class_name
     * </p>
     * @param inclusiveLowerBound
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byClassNameGreaterThanInclusive(String inclusiveLowerBound) {
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
    public Between<R, F> byClassNameBetween(String nonInclusiveLowerBound) {
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
    public Between<R, F> byClassNameBetweenInclusive(String inclusiveLowerBound) {
        addToBuf("class_name", OP_GE, inclusiveLowerBound);
        return createBetween(String.class, "class_name");
    }

    /**
     * <p>
     *   add criteria to a query that requires like for class_name
     * </p>
     * @param like
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byClassNameLike(String like) {
        addToBuf("class_name", OP_LIKE, like);
        return conjunction;
    }
}
