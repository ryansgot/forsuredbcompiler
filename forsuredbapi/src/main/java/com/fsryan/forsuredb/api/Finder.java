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

import com.fsryan.forsuredb.api.sqlgeneration.DBMSIntegrator;
import com.fsryan.forsuredb.api.sqlgeneration.Sql;
import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * <p>You should not implement your own version of {@link Finder}. A version
 * of {@link Finder} will be generated for each interface extension of
 * {@link FSGetApi} annotated with
 * {@link com.fsryan.forsuredb.annotations.FSTable FSTable}
 * <p>Finder provides a rich, type-safe, custom querying API for querying your
 * database. It is rich in that you can create many different kinds of queries
 * using the fluent API it provides. It is custom in that all concrete
 * extensions of the Finder class have custom methods generated specifically
 * for querying a table you have defined your extension of {@link FSGetApi}.
 *
 * <p>Think of this class as a means of narrowing the amount of records
 * returned by a query and the width of each record returned. The
 * {@link #columns(String...)} and {@link #distinct(String...)} methods serve
 * to narrow the width of each record returned (that is--the number of
 * columns), and the various by... methods such as {@link #byId(long, long...)}
 * serve to filter the returned records by the values of the columns.
 * @param <R> The type of {@link Resolver} to return when leaving the Finder
 *           context
 * @param <F> The Finder type to appropriately continue modifying the query
 *           when the API requires a {@link Between} or a
 *           {@link Conjunction.And} or {@link Conjunction.GroupableAndOr} in
 *           order to continue querying.
 */
public abstract class Finder<R extends Resolver, F extends Finder<R, F>> {

    @AutoValue
    public static abstract class WhereElement {

        public static final int TYPE_CONDITION = 1;
        public static final int TYPE_GROUP_START = TYPE_CONDITION << 1;
        public static final int TYPE_GROUP_END = TYPE_GROUP_START << 1;
        public static final int TYPE_AND_CONJUNCTION = TYPE_GROUP_END << 1;
        public static final int TYPE_OR_CONJUNCTION = TYPE_AND_CONJUNCTION << 1;

        public static final WhereElement AND = new AutoValue_Finder_WhereElement(TYPE_AND_CONJUNCTION, null, OP_NONE, null);
        public static final WhereElement OR = new AutoValue_Finder_WhereElement(TYPE_OR_CONJUNCTION, null, OP_NONE, null);
        public static final WhereElement START_GROUP = new AutoValue_Finder_WhereElement(TYPE_GROUP_START, null, OP_NONE, null);
        public static final WhereElement END_GROUP = new AutoValue_Finder_WhereElement(TYPE_GROUP_END, null, OP_NONE, null);

        // TODO: allow for NOT NULL and IS NULL queries
        static WhereElement createCondition(@Nonnull String column, int op, @Nullable Object value) {
            return new AutoValue_Finder_WhereElement(TYPE_CONDITION, column, op, value);
        }

        static String typeName(int type) {
            switch (type) {
                case TYPE_CONDITION: return "TYPE_CONDITION";
                case TYPE_GROUP_START: return "TYPE_GROUP_START";
                case TYPE_GROUP_END: return "TYPE_GROUP_END";
                case TYPE_AND_CONJUNCTION: return "TYPE_AND_CONJUNCTION";
                case TYPE_OR_CONJUNCTION: return "TYPE_OR_CONJUNCTION";
            }
            throw new IllegalArgumentException("Unknown type: " + type);
        }

        public abstract int type();
        @Nullable public abstract String column();
        public abstract int op();
        @Nullable public abstract Object value();
    }

    /**
     * <p>The type to allow for filtering on columns with values between two
     * values.
     * @param <R> The type of {@link Resolver} to return when leaving the Finder
     *           context
     * @param <F> The Finder type to appropriately continue modifying the query
     *           when the API requires a {@link Between} or a
     *           {@link Conjunction.And} or {@link Conjunction.GroupableAndOr}
     *           in order to continue querying.
     */
    public interface Between<R extends Resolver, F extends Finder<R, F>> {
        <T> Conjunction.GroupableAndOr<R, F> and(T high);
        <T> Conjunction.GroupableAndOr<R, F> andInclusive(T high);
    }

    public static final int OP_NONE = Integer.MIN_VALUE;
    public static final int OP_LT = -3;
    public static final int OP_LE = -2;
    public static final int OP_NE = -1;
    public static final int OP_EQ = 0;
    public static final int OP_GE = 1;
    public static final int OP_GT = 2;
    public static final int OP_LIKE = 3;

    protected final Conjunction.GroupableAndOr<R, F> conjunction;
    protected final List<WhereElement> whereElements;
    final R resolver;
    private final DBMSIntegrator sqlGenerator;
    private final Set<String> columns = new HashSet<>();
    private final List<Object> replacementsList = new ArrayList<>();
    private boolean queryDistinct = false;

    private boolean incorporatedExternalFinder = false;
    private int offset = 0;
    private int top = 0;
    private int bottom = 0;

    /**
     * <p>Creates a {@link Finder} that is purpose-built for the table
     * accessed via the {@link Resolver} passed in as an argument to this
     * constructor
     * @param resolver The resolver to return when you leave the Finder
     *                 context of the method call chain
     */
    public Finder(final R resolver) {
        this(Sql.generator(), resolver);
    }

    public Finder(final DBMSIntegrator sqlGenerator, final R resolver) {
        this.sqlGenerator = sqlGenerator;
        this.resolver = resolver;
        whereElements = new ArrayList<>();
        conjunction = new Conjunction.GroupableAndOr<R, F>() {

            @Override
            public F or() {
                whereElements.add(WhereElement.OR);
                return (F) Finder.this;
            }

            @Override
            public F and() {
                whereElements.add(WhereElement.AND);
                return (F) Finder.this;
            }

            @Override
            public F startGroup() {
                return Finder.this.startGroup();
            }

            @Override
            public Conjunction.GroupableAndOr<R, F> endGroup() {
                Finder.this.endGroup();
                return this;
            }

            @Override
            public R then() {
                return Finder.this.then();
            }
        };
    }

    public F startGroup() {
        if (!whereElements.isEmpty()) {
            WhereElement last = whereElements.get(whereElements.size() - 1);
            if (last.type() == WhereElement.TYPE_GROUP_END || last.type() == WhereElement.TYPE_CONDITION) {
                throw new IllegalStateException(WhereElement.typeName(WhereElement.TYPE_GROUP_START) + " must not follow type " + WhereElement.typeName(last.type()));
            }
        }
        whereElements.add(WhereElement.START_GROUP);
        return (F) this;
    }

    public F endGroup() {
        if (whereElements.isEmpty()) {
            throw new IllegalStateException(WhereElement.typeName(WhereElement.TYPE_GROUP_END) + " must not be the first WhereElement");
        }

        WhereElement last = whereElements.get(whereElements.size() - 1);
        if ((last.type() & (WhereElement.TYPE_AND_CONJUNCTION | WhereElement.TYPE_OR_CONJUNCTION | WhereElement.TYPE_GROUP_START)) != 0) {
            throw new IllegalStateException(WhereElement.typeName(WhereElement.TYPE_GROUP_END) + " must not follow type " + WhereElement.typeName(last.type()));
        }
        whereElements.add(WhereElement.END_GROUP);
        return (F) this;
    }

    public R then() {
        return resolver;
    }

    /**
     * <p>Specify the exact subset of columns to return on the query.
     * <p>Because a projection cannot be both DISTINCT and not DISTINCT,
     * Calling this method will result in overwriting the columns in any
     * previous calls to {@link #distinct(String...)} or to on this
     * {@link Finder}.
     * <p>Does nothing if you pass in no arguments or if you pass in no
     * arguments that match column names of this finder's table.
     * @param projection the case-sensitive names of the columns to return
     * @return this {@link Finder} object
     * @see #distinct(String...)
     */
    public F columns(String... projection) {
        project(false, projection);
        return (F) this;
    }

    /**
     * <p>Specify that the query should be for distinct values.
     * <p>Because a projection cannot be both DISTINCT and not DISTINCT,
     * Calling this method will result in overwriting the columns in any
     * previous calls to {@link #columns(String...)} or to on this
     * {@link Finder}.
     * <p>Does nothing if you pass in no arguments or if you pass in no
     * arguments that match column names of this finder's table.
     * @param distinctProjection the case-sensitive names of the columns to
     *                           return
     * @return this {@link Finder} object
     */
    public F distinct(String... distinctProjection) {
        project(true, distinctProjection);
        return (F) this;
    }

    /**
     * <p>Select the first record only.
     * @return this {@link Finder} object
     * @throws IllegalStateException if you have already called
     * {@link #last(int)}, {@link #last(int, int)} with a positive number or
     * {@link #last()} or {@link #nthFromLast(int)}
     * @see #first(int)
     * @see #first(int, int)
     * @see #nth(int)
     */
    public F first() {
        return first(1);
    }

    /**
     * <p>Select the nth record only.
     * @return this {@link Finder} object
     * @throws IllegalStateException if you have already called
     * {@link #last(int)} or {@link #last(int, int)} with a positive number or
     * {@link #last()} or {@link #nthFromLast(int)}
     * @see #first()
     * @see #first(int)
     * @see #first(int, int)
     */
    public F nth(int offset) {
        return first(1, offset);
    }

    /**
     * <p>Select the number of records from the first you want to have returned
     * <p>Will have no effect if you pass in a negative number or zero.
     * @param numRecords the count on the number of records to return
     * @return this {@link Finder} object
     * @throws IllegalStateException if you have already called {@link #last(int)} or
     * {@link #last(int, int)} with a positive number or {@link #last()}
     * @see #first()
     * @see #first(int, int)
     * @see #nth(int)
     */
    public F first(int numRecords) {
        return first(numRecords, 0);
    }

    /**
     * <p>Select the number of records from the first + offset you want to have
     * returned.
     * <p>Will have no effect if you pass in a negative number or zero.
     * @param numRecords the count on the number of records to return
     * @param offset the count on the number of records to return
     * @return this {@link Finder} object
     * @throws IllegalStateException if you have already called
     * {@link #last(int)} or {@link #last(int, int)} with a positive number or
     * {@link #last()} or {@link #nthFromLast(int)}
     * @see #first()
     * @see #first(int)
     * @see #nth(int)
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
     * <p>Select the last record only.
     * @return this {@link Finder} object
     * @throws IllegalStateException if you have already called
     * {@link #first(int)} or {@link #first(int, int)} with a positive number or
     * {@link #first()} or {@link #nth(int)}
     * @see #nthFromLast(int) (int)
     * @see #last(int)
     * @see #last(int, int)
     */
    public F last() {
        return last(1);
    }

    /**
     * <p>Select the record with an offset from the last record only.
     * <p>Negative input will be the same as calling {@link #last()}
     * @param offset the offset from the last matching record
     * @return this {@link Finder} object
     * @throws IllegalStateException if you have already called
     * {@link #first(int)} or {@link #first(int, int)} with a positive number
     * or {@link #first()} or {@link #nth(int)}
     * @see #last()
     * @see #last(int)
     * @see #last(int, int)
     */
    public F nthFromLast(int offset) {
        return last(1, offset);
    }

    /**
     * <p>Select the number of records from the last you want to have returned.
     * <p>Will have no effect if you pass in a negative number or zero.
     * @param numRecords the count on the number of records to return
     * @return this {@link Finder}
     * @throws IllegalStateException if you have already called
     * {@link #first(int)} or {@link #first(int, int)} with a positive number
     * or {@link #first()} or {@link #nth(int)}
     * @see #last()
     * @see #nthFromLast(int)
     * @see #last(int, int)
     */
    public F last(int numRecords) {
        return last(numRecords, 0);
    }

    /**
     * <p>Select the number of records from the last + offset you want to have
     * returned.
     * <p>Will have no effect if you pass in a negative number or zero.
     * @param numRecords the count on the number of records to return
     * @param offset the count on the number of records to return
     * @return this {@link Finder}
     * @throws IllegalStateException if you have already called
     * {@link #first(int)} or {@link #first(int, int)} with a positive number or
     * {@link #first()} or {@link #nth(int)}
     * @see #last()
     * @see #nthFromLast(int)
     * @see #last(int)
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
        return !whereElements.isEmpty() || top > 0 || bottom > 0 || offset > 0;
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
        return !containsNonDefaultProjection() ? resolver.projection() : new FSProjection() {
            @Override
            public String tableName() {
                return resolver.tableName();
            }

            @Override
            public String[] columns() {
                return columns.toArray(new String[0]);
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
            String where = whereElements.isEmpty() ? null : sqlGenerator.createWhere(resolver.tableName(), whereElements);
            Object[] replacements = replacementsList.toArray(new Object[0]);

            @Override
            public String where() {
                return where;
            }

            @Override
            public Object[] replacements() {
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
     * <p>add criteria to a query that requires exactMatch for _id
     * @param exactMatch the exact value to match
     * @param orExactMatches alternative exact values to match
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byId(long exactMatch, long... orExactMatches) {
        whereElements.add(WhereElement.START_GROUP);
        addToBuf("_id", OP_EQ, exactMatch);
        for(int i = 0; i < (orExactMatches == null ? 0 : orExactMatches.length); i++) {
            whereElements.add(WhereElement.OR);
            addToBuf("_id", OP_EQ, orExactMatches[i]);
        }
        whereElements.add(WhereElement.END_GROUP);
        return conjunction;
    }

    // TODO: add varargs array of further not exact matches
    /**
     * <p>add criteria to a query that requires exclusion for _id
     * @param exclusion the id of the record to not return
     * @param furtherExclusions further ids to exclude
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byIdNot(long exclusion, long... furtherExclusions) {
        whereElements.add(WhereElement.START_GROUP);
        addToBuf("_id", OP_NE, exclusion);
        for(int i = 0; i < (furtherExclusions == null ? 0 : furtherExclusions.length); i++) {
            whereElements.add(WhereElement.AND);
            addToBuf("_id", OP_NE, furtherExclusions[i]);
        }
        whereElements.add(WhereElement.END_GROUP);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires nonInclusiveUpperBound for _id
     * @param nonInclusiveUpperBound the upper bound which is non-inclusive
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byIdLessThan(long nonInclusiveUpperBound) {
        addToBuf("_id", OP_LT, nonInclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires nonInclusiveLowerBound for _id
     * @param nonInclusiveLowerBound the lower bound which is non-inclusive
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byIdGreaterThan(long nonInclusiveLowerBound) {
        addToBuf("_id", OP_GT, nonInclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires inclusiveUpperBound for _id
     * @param inclusiveUpperBound the upper bound which is inclusive
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byIdLessThanInclusive(long inclusiveUpperBound) {
        addToBuf("_id", OP_LE, inclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires inclusiveLowerBound for _id
     * @param inclusiveLowerBound the lower bound which is inclusive
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byIdGreaterThanInclusive(long inclusiveLowerBound) {
        addToBuf("_id", OP_GE, inclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires nonInclusiveLowerBound for _id
     * @param nonInclusiveLowerBound the lower bound which is non-inclusive
     * @return a {@link Between} that allows you to provide an upper bound for
     * this criteria
     */
    public Between<R, F>  byIdBetween(long nonInclusiveLowerBound) {
        addToBuf("_id", OP_GT, nonInclusiveLowerBound);
        return createBetween(long.class, "_id");
    }

    /**
     * <p>add criteria to a query that requires inclusiveLowerBound for _id
     * @param inclusiveLowerBound the lower bound which is inclusive
     * @return a {@link Between} that allows you to provide an upper bound for
     * this criteria
     */
    public Between<R, F>  byIdBetweenInclusive(long inclusiveLowerBound) {
        addToBuf("_id", OP_GE, inclusiveLowerBound);
        return createBetween(long.class, "_id");
    }

    /**
     * <p>add criteria to a query that requires nonInclusiveUpperBound for
     * created
     * @param nonInclusiveUpperBound the upper bound which is non-inclusive
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byCreatedBefore(Date nonInclusiveUpperBound) {
        addToBuf("created", OP_LT, nonInclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires nonInclusiveLowerBound for
     * created
     * @param nonInclusiveLowerBound the lower bound which is non-inclusive
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byCreatedAfter(Date nonInclusiveLowerBound) {
        addToBuf("created", OP_GT, nonInclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires inclusiveUpperBound for
     * created
     * @param inclusiveUpperBound the upper bound which is inclusive
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byCreatedBeforeInclusive(Date inclusiveUpperBound) {
        addToBuf("created", OP_LE, inclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires inclusiveLowerBound for
     * created
     * @param inclusiveLowerBound the lower bound which is inclusive
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byCreatedAfterInclusive(Date inclusiveLowerBound) {
        addToBuf("created", OP_GE, inclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires nonInclusiveLowerBound for
     * created
     * @param nonInclusiveLowerBound the lower bound which is non-inclusive
     * @return a {@link Between} that allows you to provide an upper bound for
     * this criteria
     */
    public Between<R, F>  byCreatedBetween(Date nonInclusiveLowerBound) {
        addToBuf("created", OP_GT, nonInclusiveLowerBound);
        return createBetween(java.util.Date.class, "created");
    }

    /**
     * <p>add criteria to a query that requires inclusiveLowerBound for
     * created
     * @param inclusiveLowerBound the lower bound whic his inclusive
     * @return a {@link Between} that allows you to provide an upper bound for
     * this criteria
     */
    public Between<R, F>  byCreatedBetweenInclusive(Date inclusiveLowerBound) {
        addToBuf("created", OP_GE, inclusiveLowerBound);
        return createBetween(java.util.Date.class, "created");
    }

    /**
     * <p>add criteria to a query that requires exactMatch for created
     * @param exactMatch the exact value to match
     * @param orExactMatches alternative exact values to match
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byCreatedOn(Date exactMatch, Date... orExactMatches) {
        whereElements.add(WhereElement.START_GROUP);
        addToBuf("created", OP_EQ, exactMatch);
        for(int i = 0; i < (orExactMatches == null ? 0 : orExactMatches.length); i++) {
            whereElements.add(WhereElement.OR);
            addToBuf("created", OP_EQ, orExactMatches[i]);
        }
        whereElements.add(WhereElement.END_GROUP);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires exclusion for created
     * @param exclusion the created date of the records to not return
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byNotCreatedOn(Date exclusion, Date... furtherExclusions) {
        whereElements.add(WhereElement.START_GROUP);
        addToBuf("created", OP_NE, exclusion);
        for(int i = 0; i < (furtherExclusions == null ? 0 : furtherExclusions.length); i++) {
            whereElements.add(WhereElement.AND);
            addToBuf("created", OP_NE, furtherExclusions[i]);
        }
        whereElements.add(WhereElement.END_GROUP);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires records to be marked as deleted
     * <p>because booleans are represented as 0 (false) and 1 (true), there is
     * no need for an argument to this method. If you want to match records for
     * which deleted = false, then call {@link #byNotDeleted()}
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     * @see #byNotDeleted()
     */
    public Conjunction.GroupableAndOr<R, F> byDeleted() {
        addToBuf("deleted", OP_EQ, 1);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires records to not be marked as
     * deleted
     * <p>because booleans are represented as 0 (false) and 1 (true), there is
     * no need for an argument to this method. If you want to match records for
     * which deleted = true, then call {@link #byDeleted()}
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     * @see #byDeleted()
     */
    public Conjunction.GroupableAndOr<R, F> byNotDeleted() {
        addToBuf("deleted", OP_NE, 1);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires nonInclusiveUpperBound for
     * modified
     * @param nonInclusiveUpperBound the upper bound which is non-inclusive
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byModifiedBefore(Date nonInclusiveUpperBound) {
        addToBuf("modified", OP_LT, nonInclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires nonInclusiveLowerBound for
     * modified
     * @param nonInclusiveLowerBound the lower bound which is non-inclusive
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byModifiedAfter(Date nonInclusiveLowerBound) {
        addToBuf("modified", OP_GT, nonInclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires inclusiveUpperBound for
     * modified
     * @param inclusiveUpperBound the upper bound which is inclusive
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byModifiedBeforeInclusive(Date inclusiveUpperBound) {
        addToBuf("modified", OP_LE, inclusiveUpperBound);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires inclusiveLowerBound for
     * modified
     * @param inclusiveLowerBound the lower bound which is inclusive
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byModifiedAfterInclusive(Date inclusiveLowerBound) {
        addToBuf("modified", OP_GE, inclusiveLowerBound);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires nonInclusiveLowerBound for
     * modified
     * @param nonInclusiveLowerBound the lower bound which is non-inclusive
     * @return a {@link Between} that allows you to provide an upper bound for
     * this criteria
     */
    public Between<R, F>  byModifiedBetween(Date nonInclusiveLowerBound) {
        addToBuf("modified", OP_GT, nonInclusiveLowerBound);
        return createBetween(java.util.Date.class, "modified");
    }

    /**
     * <p>add criteria to a query that requires inclusiveLowerBound for
     * modified
     * @param inclusiveLowerBound the lower bound which is inclusive
     * @return a {@link Between} that allows you to provide an upper bound for
     * this criteria
     */
    public Between<R, F> byModifiedBetweenInclusive(Date inclusiveLowerBound) {
        addToBuf("modified", OP_GE, inclusiveLowerBound);
        return createBetween(java.util.Date.class, "modified");
    }

    /**
     * <p>add criteria to a query that requires exactMatch for modified
     * @param exactMatch the exact value to match
     * @param orExactMatches alternative exact values to match
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byModifiedOn(Date exactMatch, Date... orExactMatches) {
        whereElements.add(WhereElement.START_GROUP);
        addToBuf("modified", OP_EQ, exactMatch);
        for(int i = 0; i < (orExactMatches == null ? 0 : orExactMatches.length); i++) {
            whereElements.add(WhereElement.OR);
            addToBuf("modified", OP_EQ, orExactMatches[i]);
        }
        whereElements.add(WhereElement.END_GROUP);
        return conjunction;
    }

    /**
     * <p>add criteria to a query that requires exclusion for modified
     * @param exclusion the modified date of the records to exclude
     * @return a {@link Conjunction.GroupableAndOr} that allows you to continue
     * adding more query criteria
     */
    public Conjunction.GroupableAndOr<R, F> byNotModifiedOn(Date exclusion, Date... furtherExclusions) {
        whereElements.add(WhereElement.START_GROUP);
        addToBuf("modified", OP_NE, exclusion);
        for(int i = 0; i < (furtherExclusions == null ? 0 : furtherExclusions.length); i++) {
            whereElements.add(WhereElement.AND);
            addToBuf("modified", OP_NE, furtherExclusions[i]);
        }
        whereElements.add(WhereElement.END_GROUP);
        return conjunction;
    }

    /**
     * <p>Binds the {@link Finder} class and {@link Resolver} class together
     * so that they must be in the same package. I'm a little disappointed
     * about this.
     * @param finder the child {@link Finder} to subsume within this one
     */
    final void incorporate(Finder finder) {
        if (finder == null || !finder.isFilteringResultSet()) {
            return;
        }

        // handle the incorporation of the Limits parts
        offset = throwWhenUnequalAndBothPositiveOrReturnMax("to offset", offset, finder.offset);
        top = throwWhenUnequalAndBothPositiveOrReturnMax("the first", top, finder.top);
        bottom = throwWhenUnequalAndBothPositiveOrReturnMax("the last", bottom, finder.bottom);
        throwIfPagingFromTopAndBottom();

        if (whereElements.isEmpty()) {
            whereElements.addAll(finder.whereElements);
            replacementsList.addAll(finder.replacementsList);
            return;
        }

        // TODO: check the logic for using AND here. AND should limit the kind of queries you could run
        whereElements.add(WhereElement.AND);
        whereElements.addAll(finder.whereElements);
        replacementsList.addAll(finder.replacementsList);
        incorporatedExternalFinder = true;
    }

    protected final void addToBuf(String column, int op, @Nullable Object value) {
        if (column == null || column.isEmpty()) {
            throw new IllegalArgumentException("null or empty column not allowed");
        }

        if (incorporatedExternalFinder) {
            // TODO: figure out whether this preserved behavior makes sense
            // This is synthesizing an AND and injecting it into the WHERE part of the query for use when
            // incorporating an external Finder object
            // It seems to me that this would make it impossible to write some combinations of queries like
            // table1.a = 'a' OR table2.a = 'b'
            whereElements.add(WhereElement.AND);
            incorporatedExternalFinder = false;
        }

        whereElements.add(WhereElement.createCondition(column, op, value));
        Object forReplacement = sqlGenerator.objectForReplacement(op, value);
        if (forReplacement != null) {
            replacementsList.add(forReplacement);
        }
    }

    protected final <T> Between<R, F> createBetween(Class<T> qualifiedType, final String column) {
        return new Between<R, F>() {
            @Override
            public <T> Conjunction.GroupableAndOr<R, F> and(T high) {
                return conjoin(OP_LT, high);
            }

            @Override
            public <T> Conjunction.GroupableAndOr<R, F> andInclusive(T high) {
                return conjoin(OP_LE, high);
            }

            private <T> Conjunction.GroupableAndOr<R, F> conjoin(int operator, T high) {
                whereElements.add(WhereElement.AND);
                addToBuf(column, operator, high);
                return conjunction;
            }
        };
    }

    private static int throwWhenUnequalAndBothPositiveOrReturnMax(String toDo, int num1, int num2) {
        if (num1 > 0 && num2 > 0 && num1 != num2) {
            throw new IllegalStateException(String.format("It's ambiguous whether you want %s %d records or %d records", toDo, num1, num2));
        }
        return Math.max(num1, num2);
    }

    private void project(boolean distinct, String... projection) {
        if (projection == null || projection.length == 0) {
            return;
        }
        columns.clear();
        Set<String> possibleColumns = new HashSet<>(Arrays.asList(resolver.columns()));
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
}
