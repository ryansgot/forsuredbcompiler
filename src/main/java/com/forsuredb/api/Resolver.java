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
package com.forsuredb.api;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *     Resolver is the entry point for all querying of a specific table.
 *     Typical usage is to call {@link #find()}, followed by chained method
 *     calls that narrow down the records that will be affected/returned by the
 *     query. Retrieval queries are terminated by one of the following calls:
 *     <ul>
 *         <li>
 *             {@link #get()}
 *         </li>
 *         <li>
 *             {@link #preserveQueryStateAndGet()}
 *         </li>
 *     </ul>
 *     Delete/Create/Update queries have the preceding stage of narrowing down
 *     the affected records, followed by an additional set of method calls
 *     that determine how to change any affected records. The transition to
 *     this stage is triggered by a the {@link #set()} call.
 * </p>
 * <p>
 *     An extension of Resolver will be automatically generated for you at
 *     compile time for each of your extensions of {@link FSGetApi} that are
 *     annotated with the {@link com.forsuredb.annotation.FSTable} annotation.
 *     If the table you defined has {@link com.forsuredb.annotation.ForeignKey}
 *     annotated methods, then additional public methods will be created that
 *     allow you to create a query that joins to a foreign table. Access to
 *     instances of this resolver is made available through the generated
 *     ForSure class that will be specific to each project.
 * </p>
 * @param <U> The uniform locator for records
 *           You set the "resultParameter" property of the forsuredb
 *           gradle extension with the fully-qualified class name of this
 *           class. If you do not set this property, it will be {@link Object}
 * @param <R> The class which contains records prior to insertion/update
 *           You set the "recordContainer" property of the forsuredb
 *           gradle extension with the fully-qualified class name of this
 *           class. If you do not set this property, it will be the built-in
 *           {@link TypedRecordContainer} class
 * @param <G> The extension of {@link FSGetApi} that defines the interface for
 *           retrieving fields of a record
 * @param <S> The extension of {@link FSSaveApi} that is able to perform
 *           delete/insert/update actions
 * @param <F> The extension of {@link Finder} that is able to form queries
 * @see RecordContainer
 * @see TypedRecordContainer
 * @see FSGetApi
 * @see FSSaveApi
 * @see Finder
 * @author Ryan Scott
 */
public abstract class Resolver<U, R extends RecordContainer, G extends FSGetApi, S extends FSSaveApi<U>, F extends Finder<U, R, G, S, F, O>, O extends OrderBy<U, R, G, S, F, O>> {

    private final ForSureInfoFactory<U, R> infoFactory;
    private final List<FSJoin> joins = new ArrayList<>();
    private final List<FSProjection> projections = new ArrayList<>();

    private U lookupResource;
    private G getApi;
    private F finder;
    private O orderBy;

    public Resolver(ForSureInfoFactory<U, R> infoFactory) {
        this.infoFactory = infoFactory;
        lookupResource = tableLocator();
    }

    public final G getApi() {
        if (getApi == null) {
            getApi = FSGetAdapter.createUnambiguous(getApiClass());
        }
        return getApi;
    }

    public final Retriever get() {
        try {
            return preserveQueryStateAndGet();
        } finally {
            finder = null;  // <-- When a finder's selection method is called, it must be nullified
            joins.clear();  // <-- the state of the joins must be empty at the start of each query
            projections.clear();    // <-- the state of the projections must be empty at the start of each query
            lookupResource = tableLocator();
        }
    }

    public final Retriever preserveQueryStateAndGet() {
        final FSSelection selection = finder == null ? new FSSelection.SelectAll() : finder.selection();
        final FSQueryable<U, R> queryable = infoFactory.createQueryable(lookupResource);
        projections.add(projection());
        return joins.size() == 0 ? queryable.query(projection(), selection, orderBy.getOrderByString())
                : queryable.query(joins, projections, selection, orderBy.getOrderByString());
    }

    public final O order() {
        orderBy = newOrderByInstance();
        return orderBy;
    }

    public final S set() {
        FSQueryable<U, R> queryable = infoFactory.createQueryable(lookupResource);
        R recordContainer = infoFactory.createRecordContainer();
        FSSelection selection = finder == null ? null : finder.selection();
        finder = null;  // <-- When a finder's selection method is called, it must be nullified
        return FSSaveAdapter.create(queryable, selection, recordContainer, setApiClass());
    }

    public final F find() {
        finder = newFinderInstance();
        return finder;
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

    // the following methods fill in the details for the Resolver class

    protected abstract Class<G> getApiClass();
    protected abstract Class<S> setApiClass();
    protected abstract FSProjection projection();
    protected abstract F newFinderInstance();
    protected abstract O newOrderByInstance();
    protected abstract String tableName();

    protected void addJoin(FSJoin join, FSProjection foreignTableProjection) {
        projections.add(foreignTableProjection);
        joins.add(join);
        lookupResource = infoFactory.locatorWithJoins(lookupResource, joins);
    }
}
