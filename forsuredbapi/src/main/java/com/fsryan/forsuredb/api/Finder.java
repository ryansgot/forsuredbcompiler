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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Date;

/**
 * <p>
 *     Finder provides a rich, type-safe, custom querying API for querying
 *     your database. It is rich in that you can create many different kinds
 *     of queries using the fluent API it provides. It is custom in that all
 *     concrete extensions of the Finder class have custom methods generated
 *     specifically for querying a table you have defined your extension of
 *     {@link FSGetApi}.
 * </p>
 * <p>
 *     Think of this class as a means of narrowing the amount of records
 *     returned by a query and the width of each record returned. The
 *     {@link #columns(String...)} and {@link #distinct(String...)} methods
 *     serve to narrow the width of each record returned (that is--the number
 *     of columns), and the various by... methods such as {@link #byId(long, long...)}
 *     serve to filter the returned records by the values of the columns.
 * </p>
 * @param <R> The type of {@link Resolver} to return when leaving the Finder context
 * @param <F> The Finder type to appropriately continue modifying the query when the
 *           API requires a {@link Between} or a {@link Conjunction.And} or
 *           {@link Conjunction.AndOr} in order to continue querying.
 */
public abstract class Finder<R extends Resolver, F extends Finder<R, F>> {

    /**
     * <p>
     *     The type to allow for filtering on columns with values between
     *     two values.
     * </p>
     * @param <R> The type of {@link Resolver} to return when leaving the Finder
     *           context
     * @param <F> The Finder type to appropriately continue modifying the query
     *           when the API requires a {@link Between} or a
     *           {@link Conjunction.And} or {@link Conjunction.AndOr} in order
     *           to continue querying.
     */
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
    private final Set<String> columns = new HashSet<>();
    private final FSProjection defaultProjection;
    private final Set<String> possibleColumns;
    private final StringBuffer whereBuf = new StringBuffer();
    private final List<String> replacementsList = new ArrayList<>();
    private boolean queryDistinct = false;

    private boolean incorporatedExternalFinder = false;
    private int offset = 0;
    private int top = 0;
    private int bottom = 0;

    /**
     * <p>
     *     Creates a {@link Finder} that is purpose-built for the table described by the {@link Resolver}
     *     passed in as an argument to this constructor
     * </p>
     * @param resolver The resolver to return when you leave the Finder context of the method call chain
     */
    public Finder(final R resolver) {
        this.tableName = resolver.tableName();
        defaultProjection = resolver.projection();
        possibleColumns = new HashSet<String>(resolver.methodNameToColumnNameMap().values());
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

    /**
     * <p>
     *     Specify the exact subset of columns to return on the query. You shouldn't often need to do this if your
     *     tables have a reasonable number of columns.
     * </p>
     * <p>
     *     Note that calling this method will result in overwriting the columns in any previous calls to
     *     {@link #distinct(String...)} on this {@link Finder}. The reason for this is that a projection cannot be both
     *     DISTINCT and non DISTINCT.
     * </p>
     * <p>
     *     Does nothing if you pass in no arguments or if you pass in no arguments that match column names of this
     *     finder's table.
     * </p>
     * @param projection the case-sensitive names of the columns to return
     * @return this {@link Finder} instance
     * @see #distinct(String...)
     */
    public F columns(String... projection) {
        project(false, projection);
        return (F) this;
    }

    /**
     * <p>
     *     Specify that the query should be for distinct values.
     * </p>
     * <p>
     *     Note that calling this method will result in overwriting the columns in any previous calls to
     *     {@link #columns(String...)} on this {@link Finder}. The reason for this is that a projection cannot be both
     *     DISTINCT and non DISTINCT.
     * </p>
     * <p>
     *     Does nothing if you pass in no arguments or if you pass in no arguments that match column names of this
     *     finder's table.
     * </p>
     * @param distinctProjection the case-sensitive names of the columns to return
     * @return this {@link Finder} instance
     */
    public F distinct(String... distinctProjection) {
        project(true, distinctProjection);
        return (F) this;
    }

    /**
     * Select the first record only.
     * @return this {@link Finder}
     * @throws IllegalStateException if you have already called {@link #last(int)} or
     * {@link #last(int, int)} with a positive number or {@link #last()}
     * @see #first(int, int)
     * @see #last(int)
     */
    public F first() {
        return first(1);
    }

    /**
     * Select the number of records from the first you want to have returned. Will have no effect if you pass in a
     * negative number or zero.
     * @param numRecords the count on the number of records to return
     * @return this {@link Finder}
     * @throws IllegalStateException if you have already called {@link #last(int)} or
     * {@link #last(int, int)} with a positive number or {@link #last()}
     * @see #first(int, int)
     * @see #last(int)
     * @see #last(int, int)
     */
    public F first(int numRecords) {
        return first(numRecords, 0);
    }

    /**
     * Select the number of records from the first you want to have returned. Will have no effect if you pass in a
     * negative number or zero. This also allows you to set an offset from the first.
     * @param numRecords the count on the number of records to return
     * @param offset the count on the number of records to return
     * @return this {@link Finder}
     * @throws IllegalStateException if you have already called {@link #last(int)} or
     * {@link #last(int, int)} with a positive number or {@link #last()}
     * @see #first(int)
     * @see #last(int)
     * @see #last(int, int)
     */
    public F first(int numRecords, int offset) {
        if (numRecords > 0) {
            top = numRecords;
            this.offset = Math.max(0, offset);
        }
        throwIfPagingFromTopAndBottom();
        return (F) this;
    }

    /**
     * Select the last record (the last record returned)
     * @return this {@link Finder}
     * @throws IllegalStateException if you have already called {@link #first(int)} or
     * {@link #first(int, int)} with a positive number or {@link #first()}
     */
    public F last() {
        return last(1);
    }

    /**
     * Select the number of records from the first you want to have returned. Will have no effect if you pass in a
     * negative number or zero.
     * @param numRecords the count on the number of records to return
     * @return this {@link Finder}
     * @throws IllegalStateException if you have already called {@link #first(int)} or
     * {@link #first(int, int)} with a positive number
     * @see #last(int, int)
     * @see #first(int)
     * @see #first(int, int)
     */
    public F last(int numRecords) {
        return last(numRecords, 0);
    }

    /**
     * Select the number of records from the last you want to have returned. Will have no effect if you pass in a
     * negative number or zero. This also allows you to set an offset from the last.
     * @param numRecords the count on the number of records to return
     * @param offset the count on the number of records to return
     * @return this {@link Finder}
     * @throws IllegalStateException if you have already called {@link #first(int)} or
     * {@link #first(int, int)} with a positive number or {@link #first()}
     * @see #last(int)
     * @see #first(int)
     * @see #first(int, int)
     */
    public F last(int numRecords, int offset) {
        if (numRecords > 0) {
            bottom = numRecords;
            this.offset = Math.max(0, offset);
        }
        throwIfPagingFromTopAndBottom();
        return (F) this;
    }

    /**
     * @return true if this {@link Finder} is filtering any results--false otherwise
     */
    public boolean isFilteringResultSet() {
        return !(replacementsList == null || replacementsList.isEmpty() || whereBuf == null || whereBuf.length() == 0)
                || top > 0
                || bottom > 0
                || offset > 0;
    }

    /**
     * @return true if this {@link Finder} is applying some filtering to the columns that appear in the results--
     * false otherwise
     */
    public boolean containsNonDefaultProjection() {
        return !columns.isEmpty();
    }

    /**
     * @return the {@link FSProjection} built by this finder if any was built--a non-distinct projection with all
     * columns otherwise
     */
    public final FSProjection projection() {
        return !containsNonDefaultProjection() ? defaultProjection : new FSProjection() {
            @Override
            public String tableName() {
                return tableName;
            }

            @Override
            public String[] columns() {
                return columns.toArray(new String[columns.size()]);
            }

            @Override
            public boolean isDistinct() {
                return queryDistinct;
            }
        };
    }

    /**
     * @return the {@link FSSelection} built by this finder
     */
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

            @Override
            public Limits limits() {
                return new Limits() {
                    @Override
                    public int count() {
                        // we're guaranteed to have a positive number
                        return Math.max(top, bottom);
                    }

                    @Override
                    public int offset() {
                        return offset;
                    }

                    @Override
                    public boolean isBottom() {
                        return bottom > 0;
                    }
                };
            }
        };
    }

    /**
     * <p>
     *   add criteria to a query that requires exactMatch for _id
     * </p>
     * @param exactMatch the exact value to match
     * @param orExactMatches alternative exact values to match
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byId(long exactMatch, long... orExactMatches) {
        if (orExactMatches.length == 0) {
            addToBuf("_id", OP_EQ, exactMatch);
        } else {
            List<Long> inclusionFilter = new ArrayList<Long>(1 + orExactMatches.length);
            inclusionFilter.add(exactMatch);
            for (long toInclude : orExactMatches) {
                inclusionFilter.add(toInclude);
            }
            addEqualsOrChainToBuf("_id", inclusionFilter);
        }
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
     * @param exactMatch the exact value to match
     * @param orExactMatches alternative exact values to match
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byCreatedOn(Date exactMatch, Date... orExactMatches) {
        if (orExactMatches.length == 0) {
            addToBuf("created", OP_EQ, exactMatch);
        } else {
            List<Date> inclusionFilter = new ArrayList<Date>(1 + orExactMatches.length);
            inclusionFilter.add(exactMatch);
            for (Date toInclude : orExactMatches) {
                inclusionFilter.add(toInclude);
            }
            addEqualsOrChainToBuf("created", inclusionFilter);
        }
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
     * @param exactMatch the exact value to match
     * @param orExactMatches alternative exact values to match
     * @return a {@link Conjunction.AndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.AndOr<R, F> byModifiedOn(Date exactMatch, Date... orExactMatches) {
        if (orExactMatches.length == 0) {
            addToBuf("modified", OP_EQ, exactMatch);
        } else {
            List<Date> inclusionFilter = new ArrayList<Date>(1 + orExactMatches.length);
            inclusionFilter.add(exactMatch);
            for (Date toInclude : orExactMatches) {
                inclusionFilter.add(toInclude);
            }
            addEqualsOrChainToBuf("created", inclusionFilter);
        }
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

    protected final void addEqualsOrChainToBuf(String column, List orValues) {
        if (orValues == null || orValues.isEmpty() || !canAddClause(column, orValues.get(0))) {
            return;
        }

        if (incorporatedExternalFinder) {
            whereBuf.append(" ").append(Sql.generator().andKeyword()).append(" ");
            incorporatedExternalFinder = false;
        }

        Object first = orValues.get(0);
        whereBuf.append("(").append(Sql.generator().whereOperation(tableName, column, OP_EQ)).append(" ? ");
        replacementsList.add(Date.class.equals(first.getClass()) ? Sql.generator().formatDate((Date) first) : first.toString());
        for (int i = 1; i < orValues.size(); i++) {
            Object orValue = orValues.get(i);
            whereBuf.append(Sql.generator().orKeyword()).append(" ")
                    .append(Sql.generator().whereOperation(tableName, column, OP_EQ))
                    .append(" ?");
            replacementsList.add(Date.class.equals(orValue.getClass()) ? Sql.generator().formatDate((Date) orValue) : orValue.toString());
        }
        whereBuf.append(")");
    }

    /**
     * <p>
     *     Binds the {@link Finder} class and {@link Resolver} class together so that they must be in
     *     the same package. I'm a little disappointed about this.
     * </p>
     * @param finder the child {@link Finder} to subsume within this one
     */
    /*package*/ final void incorporate(Finder finder) {
        if (finder == null || !finder.isFilteringResultSet()) {
            return;
        }

        // handle the incorporation of the Limits parts
        offset = throwWhenUnequalAndBothPositiveOrReturnMax("to offset", offset, finder.offset);
        top = throwWhenUnequalAndBothPositiveOrReturnMax("the first", top, finder.top);
        bottom = throwWhenUnequalAndBothPositiveOrReturnMax("the last", bottom, finder.bottom);
        throwIfPagingFromTopAndBottom();

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
        return column != null && !column.isEmpty() && value != null && !value.toString().isEmpty();
    }

    private void project(boolean distinct, String... projection) {
        if (projection == null || projection.length == 0) {
            return;
        }
        columns.clear();
        for (int i = 0; i < projection.length; i++) {
            final String column = projection[i];
            if (column == null || !possibleColumns.contains(column)) {
                continue;
            }
            columns.add(column);
        }
        // It is possible that none of the columns could be valid for this table. In that case, we want to revert to
        // the default projection
        queryDistinct = distinct && !columns.isEmpty();
    }

    private void throwIfPagingFromTopAndBottom() {
        if (top > 0 && bottom > 0) {
            throw new IllegalStateException("Cannot page from both first and last at same time");
        }
    }

    private static int throwWhenUnequalAndBothPositiveOrReturnMax(String toDo, int num1, int num2) {
        if (num1 > 0 && num2 > 0 && num1 != num2) {
            throw new IllegalStateException(String.format("It's ambiguous whether you want %s %d records or %d records", toDo, num1, num2));
        }
        return Math.max(num1, num2);
    }
}
