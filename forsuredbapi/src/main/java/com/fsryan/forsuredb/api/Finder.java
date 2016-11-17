/*
   forsuredbcompiler, an annotation processor and code generator for the forsuredb project

   Copyright 2015 Ryan Scott

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.api.sqlgeneration.Sql;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class Finder<R extends Resolver, F extends Finder<R, F>> {

    public interface Between<R extends Resolver, F extends Finder<R, F>> {
        <T> Conjunction.AndOr<R, F> and(T high);
        <T> Conjunction.AndOr<R, F> andInclusive(T high);
    }

    public static final int OP_LT = -3;
    public static final int OP_LE = -2;
    public static final int OP_NE = -1;
    public static final int OP_EQ = 0;
    public static final int OP_GE = 1;
    public static final int OP_GT = 2;
    public static final int OP_LIKE = 3;

    protected final String tableName;
    protected final Conjunction.AndOr<R, F> conjunction;
    protected final StringBuffer whereBuf = new StringBuffer();
    protected final List<String> replacementsList = new ArrayList<>();
    private boolean incorporatedExternalFinder = false;

    public Finder(final R resolver) {
        this.tableName = resolver.tableName();
        conjunction = new Conjunction.AndOr<R, F>() {
            @Override
            public R then() {
                return resolver;
            }

            @Override
            public F or() {
                if (whereBuf.length() > 0) {
                    surroundCurrentWhereWithParens();
                    whereBuf.append(" ").append(Sql.generator().orKeyword()).append(" ");
                }
                return (F) Finder.this;
            }

            @Override
            public F and() {
                if (whereBuf.length() > 0) {
                    surroundCurrentWhereWithParens();
                    whereBuf.append(" ").append(Sql.generator().andKeyword()).append(" ");
                }
                return (F) Finder.this;
            }
        };
    }

    public final FSSelection selection() {
        return new FSSelection() {
            String where = whereBuf.toString();
            String[] replacements = replacementsList.toArray(new String[replacementsList.size()]);

            @Override
            public String where() {
                return where;
            }

            @Override
            public String[] replacements() {
                return replacements;
            }
        };
    }

    /**
     * <p>
     *   add criteria to a query that requires exactMatch for _id
     * </p>
     * @param exactMatch
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byId(long exactMatch) {
        addToBuf("_id", OP_EQ, exactMatch);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires exclusion for _id
     * </p>
     * @param exclusion
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byIdNot(long exclusion) {
        addToBuf("_id", OP_NE, exclusion);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveUpperBound for _id
     * </p>
     * @param nonInclusiveUpperBound
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byIdLessThan(long nonInclusiveUpperBound) {
        addToBuf("_id", OP_LT, nonInclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveLowerBound for _id
     * </p>
     * @param nonInclusiveLowerBound
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byIdGreaterThan(long nonInclusiveLowerBound) {
        addToBuf("_id", OP_GT, nonInclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveUpperBound for _id
     * </p>
     * @param inclusiveUpperBound
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byIdLessThanInclusive(long inclusiveUpperBound) {
        addToBuf("_id", OP_LE, inclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveLowerBound for _id
     * </p>
     * @param inclusiveLowerBound
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byIdGreaterThanInclusive(long inclusiveLowerBound) {
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
    public Between<R, F>  byIdBetween(long nonInclusiveLowerBound) {
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
    public Between<R, F>  byIdBetweenInclusive(long inclusiveLowerBound) {
        addToBuf("_id", OP_GE, inclusiveLowerBound);
        return createBetween(long.class, "_id");
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveUpperBound for created
     * </p>
     * @param nonInclusiveUpperBound
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byCreatedBefore(Date nonInclusiveUpperBound) {
        addToBuf("created", OP_LT, nonInclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveLowerBound for created
     * </p>
     * @param nonInclusiveLowerBound
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byCreatedAfter(Date nonInclusiveLowerBound) {
        addToBuf("created", OP_GT, nonInclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveUpperBound for created
     * </p>
     * @param inclusiveUpperBound
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byCreatedBeforeInclusive(Date inclusiveUpperBound) {
        addToBuf("created", OP_LE, inclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveLowerBound for created
     * </p>
     * @param inclusiveLowerBound
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byCreatedAfterInclusive(Date inclusiveLowerBound) {
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
    public Between<R, F>  byCreatedBetween(Date nonInclusiveLowerBound) {
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
    public Between<R, F>  byCreatedBetweenInclusive(Date inclusiveLowerBound) {
        addToBuf("created", OP_GE, inclusiveLowerBound);
        return createBetween(java.util.Date.class, "created");
    }

    /**
     * <p>
     *   add criteria to a query that requires exactMatch for created
     * </p>
     * @param exactMatch
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byCreatedOn(Date exactMatch) {
        addToBuf("created", OP_EQ, exactMatch);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires exclusion for created
     * </p>
     * @param exclusion
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byNotCreatedOn(Date exclusion) {
        addToBuf("created", OP_NE, exclusion);
        return conjunction;
    }

    /**
     * <p>
     *   because booleans are represented as 0 (false) and 1 (true), there is no need for an
     *   argument to this method. If you want to match records for which deleted = false,
     *   then call {@link #byNotDeleted()}
     * </p>
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     * @see #byNotDeleted()
     */
    public Conjunction.AndOr<R, F> byDeleted() {
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
    public Conjunction.AndOr<R, F> byNotDeleted() {
        addToBuf("deleted", OP_NE, 1);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveUpperBound for modified
     * </p>
     * @param nonInclusiveUpperBound
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byModifiedBefore(Date nonInclusiveUpperBound) {
        addToBuf("modified", OP_LT, nonInclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires nonInclusiveLowerBound for modified
     * </p>
     * @param nonInclusiveLowerBound
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byModifiedAfter(Date nonInclusiveLowerBound) {
        addToBuf("modified", OP_GT, nonInclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveUpperBound for modified
     * </p>
     * @param inclusiveUpperBound
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byModifiedBeforeInclusive(Date inclusiveUpperBound) {
        addToBuf("modified", OP_LE, inclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires inclusiveLowerBound for modified
     * </p>
     * @param inclusiveLowerBound
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byModifiedAfterInclusive(Date inclusiveLowerBound) {
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
    public Between<R, F>  byModifiedBetween(Date nonInclusiveLowerBound) {
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
    public Between<R, F> byModifiedBetweenInclusive(Date inclusiveLowerBound) {
        addToBuf("modified", OP_GE, inclusiveLowerBound);
        return createBetween(java.util.Date.class, "modified");
    }

    /**
     * <p>
     *   add criteria to a query that requires exactMatch for modified
     * </p>
     * @param exactMatch
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byModifiedOn(Date exactMatch) {
        addToBuf("modified", OP_EQ, exactMatch);
        return conjunction;
    }

    /**
     * <p>
     *   add criteria to a query that requires exclusion for modified
     * </p>
     * @param exclusion
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byNotModifiedOn(Date exclusion) {
        addToBuf("modified", OP_NE, exclusion);
        return conjunction;
    }

    protected final void addToBuf(String column, int operator, Object value) {
        if (!canAddClause(column, value)) {
            return;
        }

        if (incorporatedExternalFinder) {
            whereBuf.append(" ").append(Sql.generator().andKeyword()).append(" ");
            incorporatedExternalFinder = false;
        }

        whereBuf.append(Sql.generator().whereOperation(tableName, column, operator)).append(" ");
        if (operator == OP_LIKE) {
            whereBuf.append(Sql.generator().wildcardKeyword()).append("?").append(Sql.generator().wildcardKeyword());
        } else {
            whereBuf.append("?");
        }

        replacementsList.add(Date.class.equals(value.getClass()) ? Sql.generator().formatDate((Date) value) : value.toString());
    }

    protected final void incorporate(Finder finder) {
        if (finder == null
                || finder.replacementsList == null
                || finder.replacementsList.isEmpty()
                || finder.whereBuf == null
                || finder.whereBuf.length() == 0) {
            return;
        }

        if (whereBuf.length() == 0) {
            whereBuf.append(finder.whereBuf);
            replacementsList.addAll(finder.replacementsList);
            return;
        }

        surroundCurrentWhereWithParens();
        finder.surroundCurrentWhereWithParens();
        whereBuf.append(" ").append(Sql.generator().andKeyword()).append(" ").append(finder.whereBuf);
        replacementsList.addAll(finder.replacementsList);
        incorporatedExternalFinder = true;
    }

    protected void surroundCurrentWhereWithParens() {
        String currentWhere = whereBuf.toString();
        whereBuf.delete(0, whereBuf.length());
        whereBuf.trimToSize();
        whereBuf.append("(").append(currentWhere).append(")");
    }

    protected final <T> Between<R, F> createBetween(Class<T> qualifiedType, final String column) {
        return new Between<R, F>() {
            @Override
            public <T> Conjunction.AndOr<R, F> and(T high) {
                return conjoin(OP_LT, high);
            }

            @Override
            public <T> Conjunction.AndOr<R, F> andInclusive(T high) {
                return conjoin(OP_LE, high);
            }

            private <T> Conjunction.AndOr<R, F> conjoin(int operator, T high) {
                whereBuf.append(" ").append(Sql.generator().andKeyword()).append(" ");
                addToBuf(column, operator, high);
                return conjunction;
            }
        };
    }

    private boolean canAddClause(String column, Object value) {
        return !Strings.isNullOrEmpty(column) && value != null && !value.toString().isEmpty();
    }
}
