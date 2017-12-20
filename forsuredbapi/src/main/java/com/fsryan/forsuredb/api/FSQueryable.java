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

import java.util.List;

/**
 * <p>
 *     if you've parameterized your FSQueryable properly, U would be a class that locates records
 * </p>
 * @param <U> The class used to locate records in your database
 * @param <R> an extension of {@link RecordContainer}, which contains a record before it is inserted/updated in the database
 */
public interface FSQueryable<U, R extends RecordContainer> {

    /**
     * @param recordContainer An extension of {@link RecordContainer} which contains the record to be inserted
     * @return if you've {@link FSQueryable parameterized this class correctly}, then a record
     * locator for the inserted record
     */
    U insert(R recordContainer);

    /**
     * In the normal case, sortOrder will be null/empty. In the case that the user has
     * requested that the matching set be limited in some way (by calling {@link Finder#first()},
     * {@link Finder#first(int)}, {@link Finder#first(int, int)}, {@link Finder#last()},
     * {@link Finder#last(int)}, or {@link Finder#last(int, int)}), possibly not every
     * matching record should get updated. In that case, a list of {@link FSOrdering} is used
     * to determine the subset of matching records that should get updated.
     *
     * @param recordContainer An extension of {@link RecordContainer} which contains the record to be updated
     * @param selection The {@link FSSelection} that defines the subset of records to update
     * @param sortOrder A list of {@link FSOrdering} that the ordering of the records to match
     * @return the number of records affected by the update
     */
    int update(R recordContainer, FSSelection selection, List<FSOrdering> sortOrder);

    /**
     * An upsert is equivalent to the following:
     * <ol>
     *     <li>Check whether there are any matching records</li>
     *     <li>If no records, insert. If records exist, then update.</li>
     * </ol>
     *
     * Your implementation of this method should ensure atomic modification of the underlying
     * data store. For example, databases make use of transactions for this purpose.
     *
     * @param recordContainer An extension of {@link RecordContainer} which contains the record to be updated
     * @param selection The {@link FSSelection} that defines the subset of records to update
     * @param sortOrder A list of {@link FSOrdering} that the ordering of the records to match
     * @return A {@link SaveResult} summarizing the result of the upsert
     * @see #update(RecordContainer, FSSelection, List)
     */
    SaveResult<U> upsert(R recordContainer, FSSelection selection, List<FSOrdering> sortOrder);

    /**
     * In the normal case, the sort order will be null/empty. In the case that the user has
     * requested that the matching set be limited in some way (by calling {@link Finder#first()},
     * {@link Finder#first(int)}, {@link Finder#first(int, int)}, {@link Finder#last()},
     * {@link Finder#last(int)}, or {@link Finder#last(int, int)}), possibly not every
     * matching record should get updated. In that case, a list of {@link FSOrdering} is used
     * to determine the subset of matching records that should get deleted.
     *
     * @param selection The {@link FSSelection} that defines the subset of records to delete
     * @param sortOrder a list of {@link FSOrdering} describing the sort order
     * @return the number of records affected by the delete
     */
    int delete(FSSelection selection, List<FSOrdering> sortOrder);

    /**
     * @param projection The {@link FSProjection} that defines the subest of columns to retrieve for each record
     * @param selection The {@link FSSelection} that defines the subset of records to retrieve
     * @param sortOrder a list of {@link FSOrdering} describing the sort order
     * @return A Retriever that will be able to retrieve records returned by this query
     */
    Retriever query(FSProjection projection, FSSelection selection, List<FSOrdering> sortOrder);

    /**
     * @param joins A list of {@link FSJoin} describing how to join
     * @param projections The list of {@link FSProjection} that defines the columns to return in the SELECT query
     * @param selection The {@link FSSelection} that defines the subset of records to retrieve
     * @param sortOrder a list of {@link FSOrdering} describing the sort order
     * @return A Retriever that will be able to retrieve records returned by this join query
     */
    Retriever query(List<FSJoin> joins, List<FSProjection> projections, FSSelection selection, List<FSOrdering> sortOrder);
}
