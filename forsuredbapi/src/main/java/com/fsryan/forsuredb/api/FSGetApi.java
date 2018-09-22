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

import java.util.Date;

/**
 * <p>The parent interface for all table definitions. You <i>MUST</i> extend
 * this interface or {@link FSDocStoreGetApi} and then annotate this new type
 * with FSTable.
 *
 * @author Ryan Scott
 */
public interface FSGetApi {

    /**
     * <p>Defines an integer, which will be the primary key for the table
     * unless otherwise specified. If you specify a different primary key, then
     * the result of calling this method will likely be zero, but actual result
     * will be determined by the DBMS.
     *
     * @param retriever A Retriever object that can get records of the table
     *                  this extension of FSGetApi defines
     * @return the id of the record
     */
    long id(Retriever retriever);

    /**
     * <p>forsuredb automatically sets this column's value to the time the
     * record was inserted into the table.
     *                  this extension of FSGetApi defines
     * @return the {@link Date} this record was created
     */
    Date created(Retriever retriever);

    /**
     * <p>forsuredb automatically triggers an update of this column whenever an
     * update is made to the record. Its starting value will be the same as
     * {@link #created(Retriever)}
     *
     * @param retriever A Retriever object that can get records of the table
     *                  this extension of FSGetApi defines
     * @return the {@link Date} this record was last modified
     */
    Date modified(Retriever retriever);

    /**
     * <p>{@link BaseSetter} has a {@link BaseSetter#softDelete()} method that
     * will update this from false to true instead of actually removing a
     * record.
     *
     * @param retriever A Retriever object that can get records of the table
     *                  this extension of FSGetApi defines
     * @return true if the record is deleted, false if not
     */
    boolean deleted(Retriever retriever);
}
