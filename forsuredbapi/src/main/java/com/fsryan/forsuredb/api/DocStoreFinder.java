package com.fsryan.forsuredb.api;

import java.util.ArrayList;
import java.util.List;

public class DocStoreFinder<R extends DocStoreResolver, F extends DocStoreFinder<R, F>> extends Finder<R, F> {
    
    public DocStoreFinder(R resolver) {
        super(resolver);
    }

    /**
     * <p>
     *     add criteria to a query that is a convenience for calling {@link #byClassName(String, String...)} with the
     *     {@link Class} object instead of the fully qualified class name.
     * </p>
     * @param exactClassMatch the exact class you want
     * @param orExactClassMatches the other possible class matches
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue adding more query criteria
     * @see #byClassName(String, String...)
     */
    public Conjunction.GroupableAndOr<R, F> byClass(Class exactClassMatch, Class... orExactClassMatches) {
        String[] otherClassNames = new String[orExactClassMatches.length];
        for (int i = 0; i < orExactClassMatches.length; i++) {
            otherClassNames[i] = orExactClassMatches[i].getName();
        }
        return byClassName(exactClassMatch == null ? "" : exactClassMatch.getName(), otherClassNames);
    }

    /**
     * <p>
     *     add criteria to a query that is a convenience for calling {@link #byClassNameNot(String)} with the
     *     {@link Class} object instead of the fully qualified class name.
     * </p>
     * @param exclusion
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue adding more query criteria
     * @see #byClassNameNot(String)
     */
    public Conjunction.GroupableAndOr<R, F> byClassNot(Class exclusion) {
        return byClassNameNot(exclusion == null ? "" : exclusion.getName());
    }

    /**
     * <p>
     *   add criteria to a query that requires exact match for class_name
     * </p>
     * @param exactMatch the exact class name of the class you want
     * @param orExactMatches the other possible matches
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byClassName(String exactMatch, String... orExactMatches) {
        if (orExactMatches.length == 0) {
            addToBuf("class_name", OP_EQ, exactMatch);
        } else {
            List<String> inclusionFilter = new ArrayList<String>(1 + orExactMatches.length);
            inclusionFilter.add(exactMatch);
            for (String toInclude : orExactMatches) {
                inclusionFilter.add(toInclude);
            }
            addEqualsOrChainToBuf("class_name", inclusionFilter);
        }
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires exclusion for class_name
     * </p>
     * @param exclusion
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byClassNameNot(String exclusion) {
        addToBuf("class_name", OP_NE, exclusion);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveUpperBound for class_name
     * </p>
     * @param nonInclusiveUpperBound
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byClassNameLessThan(String nonInclusiveUpperBound) {
        addToBuf("class_name", OP_LT, nonInclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveLowerBound for class_name
     * </p>
     * @param nonInclusiveLowerBound
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byClassNameGreaterThan(String nonInclusiveLowerBound) {
        addToBuf("class_name", OP_GT, nonInclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveUpperBound for class_name
     * </p>
     * @param inclusiveUpperBound
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byClassNameLessThanInclusive(String inclusiveUpperBound) {
        addToBuf("class_name", OP_LE, inclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveLowerBound for class_name
     * </p>
     * @param inclusiveLowerBound
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byClassNameGreaterThanInclusive(String inclusiveLowerBound) {
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
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byClassNameLike(String like) {
        addToBuf("class_name", OP_LIKE, like);
        return conjunction;
    }
}
