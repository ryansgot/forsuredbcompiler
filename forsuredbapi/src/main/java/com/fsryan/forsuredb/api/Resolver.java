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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Resolver is the entry point for all querying of a specific table.
 * Typical usage is to call {@link #find()}, followed by chained method calls
 * that narrow down the records that will be affected/returned by the query.
 * Retrieval queries are terminated by one of the following methods:
 * <ul>
 *   <li>{@link #get()}</li>
 *   <li>{@link #preserveQueryStateAndGet()}</li>
 * </ul>
 * <p>Delete/Create/Update queries have the preceding stage of narrowing down
 * the affected records followed by an additional set of method calls
 * determining how to change any affected records. The transition to this stage
 * is triggered by a the {@link #set()} method.
 * <p>An extension of Resolver will be automatically generated for you at
 * compile time for each of your extensions of {@link FSGetApi} that are
 * annotated with the {@link com.fsryan.forsuredb.annotations.FSTable}\
 * annotation. If the table you defined has
 * {@link com.fsryan.forsuredb.annotations.ForeignKey} or
 * {@link com.fsryan.forsuredb.annotations.FSForeignKey} annotated methods,
 * then additional public methods will be created that allow you to joins to a
 * the foreign table in the same querying chain.
 * <p>Access to instances of this resolver is made available through the
 * generated ForSure class that will be specific to each project.
 * @param <T> The class of resolver that gets passed back when context is
 *           reverted to Resolver context from Finder, OrderBy, or joining
 *           context
 * @param <U> The uniform locator for records
 *           You set the "resultParameter" property of the fsryan
 *           gradle extension with the fully-qualified class name of this
 *           class. If you do not set this property, it will be {@link Object}
 * @param <R> The class which contains records prior to insertion/update
 *           You set the "recordContainer" property of the fsryan
 *           gradle extension with the fully-qualified class name of this
 *           class. If you do not set this property, it will be the built-in
 *           {@link TypedRecordContainer} class
 * @param <G> The extension of {@link FSGetApi} that defines the interface for
 *           retrieving fields of a record
 * @param <S> The extension of {@link BaseSetter} that is able to perform
 *           delete/insert/update actions
 * @param <F> The extension of {@link Finder} that is able to form queries
 * @see RecordContainer
 * @see TypedRecordContainer
 * @see FSGetApi
 * @see BaseSetter
 * @see Finder
 * @author Ryan Scott
 */
public abstract class Resolver<T extends Resolver, U, R extends RecordContainer, G extends FSGetApi, S extends BaseSetter<U, R, S>, F extends Finder<T, F>, O extends OrderBy<T, O>> {

    protected final ForSureInfoFactory<U, R> infoFactory;
    protected U lookupResource;

    private final List<FSJoin> joins = new ArrayList<>();
    private final List<FSProjection> projections = new ArrayList<>();
    private boolean addedThisProjection = false;

    private F finder;
    private O orderBy;

    public Resolver(ForSureInfoFactory<U, R> infoFactory) {
        this.infoFactory = infoFactory;
        lookupResource = tableLocator();
    }

    public static void joinResolvers(Resolver parent, Resolver child) {
        if (child.orderBy != null) {
            parent.orderBy = parent.orderBy == null ? parent.newOrderByInstance() : parent.orderBy;
            parent.orderBy.appendOrderings(child.orderBy.getOrderings());
        }
        parent.finder = parent.finder == null ? parent.newFinderInstance() : parent.finder;
        parent.finder.incorporate(child.finder);

        // If the child also had projections (via a joinResolvers call) then they need to be included as well
        parent.projections.add(child.finder == null ? child.projection() : child.finder.projection());
        parent.projections.addAll(child.projections);
    }

    public Retriever get() {
        try {
            return preserveQueryStateAndGet();
        } finally {
            cleanQueryState();
        }
    }

    public Retriever preserveQueryStateAndGet() {
        final List<FSOrdering> orderings = orderBy == null ? Collections.<FSOrdering>emptyList() : orderBy.getOrderings();
        final FSSelection selection = finder == null ? FSSelection.ALL : finder.selection();
        final FSQueryable<U, R> queryable = infoFactory.createQueryable(lookupResource);
        if (!addedThisProjection) {
            projections.add(finder == null ? projection() : finder.projection());
            addedThisProjection = true;
        }
        return joins.size() == 0
                ? queryable.query(projections.get(0), selection, orderings)
                : queryable.query(joins, projections, selection, orderings);
    }

    public final O order() {
        if (orderBy == null) {
            orderBy = newOrderByInstance();
        }
        return orderBy;
    }

    public final F find() {
        if (finder == null) {
            finder = newFinderInstance();
        }
        return finder;
    }

    public int getCount() {
        final FSSelection selection = finder == null ? FSSelection.ALL : finder.selection();
        final FSQueryable<U, R> queryable = infoFactory.createQueryable(lookupResource);
        try {
            return queryable.countRecords(joins, selection);
        } finally {
            cleanQueryState();
        }
    }

    public final U tableLocator() {
        return infoFactory.tableResource(tableName());
    }

    public final U recordLocator(long id) {
        return infoFactory.locatorFor(tableName(), id);
    }

    public final U currentLocator() {
        return lookupResource;
    }

    public abstract G getApi();
    public abstract S set();
    public abstract FSProjection projection();
    public abstract String[] columns();
    public abstract String tableName();
    protected abstract F newFinderInstance();
    protected abstract O newOrderByInstance();

    protected FSSelection determineSelection(boolean retainFinder) {
        final FSSelection ret = finder == null ? null : finder.selection();
        if (!retainFinder) {
            finder = null;  // <-- When used, a finder must be nullified unless specifically preserving query state
        }
        return ret;
    }

    protected List<FSOrdering> determineOrderings(boolean retainOrderBy) {
        final List<FSOrdering> ret = orderBy == null ? Collections.<FSOrdering>emptyList() : orderBy.getOrderings();
        if (!retainOrderBy) {
            orderBy = null; // <-- When used, an orderBy must be nullified unless specifically preserving query state
        }
        return ret;
    }

    protected void addJoin(FSJoin join) {
        joins.add(join);
        lookupResource = infoFactory.locatorWithJoins(lookupResource, joins);
    }

    private void cleanQueryState() {
        orderBy = null;             // <-- When a finder's get method is called, avoid leaking into the next query
        finder = null;              // <-- When a finder's get method is called, avoid leaking into the next query
        joins.clear();              // <-- the state of the joins must be empty at the start of each query
        projections.clear();        // <-- the state of the projections must be empty at the start of each query
        lookupResource = tableLocator();
        addedThisProjection = false;
    }
}
