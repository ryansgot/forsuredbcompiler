package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.api.sqlgeneration.DBMSIntegrator;
import com.fsryan.forsuredb.api.sqlgeneration.Sql;

public class DocStoreFinder<R extends DocStoreResolver, F extends DocStoreFinder<R, F>> extends Finder<R, F> {

    public DocStoreFinder(R resolver) {
        this(Sql.generator(), resolver);
    }

    DocStoreFinder(DBMSIntegrator dbmsIntegrator, R resolver) {
        super(dbmsIntegrator, resolver);
    }

    /**
     * <p>add criteria to a query that is a convenience for calling
     * {@link #byClassName(String, String...)} with the @link Class} object
     * instead of the fully qualified class name.
     * @param exactClassMatch the exact class you want
     * @param orExactClassMatches the other possible class matches
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     * @see #byClassName(String, String...)
     */
    public Conjunction.GroupableAndOr<R, F> byClass(Class exactClassMatch, Class... orExactClassMatches) {
        whereElements.add(WhereElement.START_GROUP);
        addClassToBuf(exactClassMatch, OP_EQ);
        for (int i = 0; i < (orExactClassMatches == null ? 0 : orExactClassMatches.length); i++) {
            whereElements.add(WhereElement.OR);
            addClassToBuf(orExactClassMatches[i], OP_EQ);
        }
        whereElements.add(WhereElement.END_GROUP);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that is a convenience for calling
     * {@link #byClassNameNot(String, String...)} with the {@link Class} object
     *  instead of the fully qualified class name.
     * @param exclusion the {@link Class} to exclude (by its name)
     * @param furtherExclusions further classes to exclude (by their names)
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     * @see #byClassNameNot(String, String...)
     */
    public Conjunction.GroupableAndOr<R, F> byClassNot(Class exclusion, Class... furtherExclusions) {
        whereElements.add(WhereElement.START_GROUP);
        addClassToBuf(exclusion, OP_NE);
        for (int i = 0; i < (furtherExclusions == null ? 0 : furtherExclusions.length); i++) {
            whereElements.add(WhereElement.AND);
            addClassToBuf(furtherExclusions[i], OP_NE);
        }
        whereElements.add(WhereElement.END_GROUP);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires exact match for class_name or a
     * match for any of the other strings passed in the varargs argument
     * @param exactMatch the exact class name of the class you want
     * @param orExactMatches the other possible matches
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byClassName(String exactMatch, String... orExactMatches) {
        whereElements.add(WhereElement.START_GROUP);
        addClassToBuf(exactMatch, OP_EQ);
        for (int i = 0; i < (orExactMatches == null ? 0 : orExactMatches.length); i++) {
            whereElements.add(WhereElement.OR);
            addClassToBuf(orExactMatches[i], OP_EQ);
        }
        whereElements.add(WhereElement.END_GROUP);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires exclusion for class_name and
     * any other class_name passed in
     * @param exclusion a class name to exclude
     * @param furtherExclusions further class names to exclude
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byClassNameNot(String exclusion, String... furtherExclusions) {
        whereElements.add(WhereElement.START_GROUP);
        addClassToBuf(exclusion, OP_NE);
        for (int i = 0; i < (furtherExclusions == null ? 0 : furtherExclusions.length); i++) {
            whereElements.add(WhereElement.AND);
            addClassToBuf(furtherExclusions[i], OP_NE);
        }
        whereElements.add(WhereElement.END_GROUP);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires nonInclusiveUpperBound for
     * class_name
     * @param nonInclusiveUpperBound
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byClassNameLessThan(String nonInclusiveUpperBound) {
        addToBuf("class_name", OP_LT, nonInclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires nonInclusiveLowerBound for
     * class_name
     * @param nonInclusiveLowerBound
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byClassNameGreaterThan(String nonInclusiveLowerBound) {
        addToBuf("class_name", OP_GT, nonInclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires inclusiveUpperBound for
     * class_name
     * @param inclusiveUpperBound
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byClassNameLessThanInclusive(String inclusiveUpperBound) {
        addToBuf("class_name", OP_LE, inclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires inclusiveLowerBound for
     * class_name
     * @param inclusiveLowerBound
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byClassNameGreaterThanInclusive(String inclusiveLowerBound) {
        addToBuf("class_name", OP_GE, inclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires nonInclusiveLowerBound for
     * class_name
     * @param nonInclusiveLowerBound
     * @return a {@link Between} that allows you to provide an upper bound for
     * this criteria
     */
    public Between<R, F> byClassNameBetween(String nonInclusiveLowerBound) {
        addToBuf("class_name", OP_GT, nonInclusiveLowerBound);
        return createBetween(String.class, "class_name");
    }

    /**
     * <p>add criteria to a query that requires inclusiveLowerBound for
     * class_name
     * @param inclusiveLowerBound
     * @return a {@link Between} that allows you to provide an upper bound for
     * this criteria
     */
    public Between<R, F> byClassNameBetweenInclusive(String inclusiveLowerBound) {
        addToBuf("class_name", OP_GE, inclusiveLowerBound);
        return createBetween(String.class, "class_name");
    }

    /**
     * <p>add criteria to a query that requires like for class_name
     * @param like
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byClassNameLike(String like) {
        addToBuf("class_name", OP_LIKE, like);
        return conjunction;
    }

    private void addClassToBuf(Class cls, int op) {
        if (cls == null) {
            throw new IllegalArgumentException("cannot search for null class");
        }
        addClassToBuf(cls.getName(), op);
    }

    private void addClassToBuf(String clsName, int op) {
        if (clsName == null) {
            throw new IllegalArgumentException("cannot search for null class name");
        }
        addToBuf("class_name", op, clsName);
    }
}
