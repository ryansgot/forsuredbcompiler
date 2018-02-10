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
package com.fsryan.forsuredb.annotations;

import com.fsryan.forsuredb.api.FSGetApi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 *     Use the {@link FSForeignKey} annotation on your {@link FSGetApi} extension methods in
 *     order to define the foreign key constraints of your table. If you intend to perform
 *     queries that join two tables, and you want Forsuredb to generate the querying interface
 *     for you, then you must define foreign keys using this annotation
 * </p>
 * @author Ryan Scott
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface FSForeignKey {

    /**
     * @return the {@link FSGetApi} class that defines the table to which this
     * {@link FSForeignKey} points
     */
    Class<? extends FSGetApi> apiClass();

    /**
     * @return The name of the column in the table, defined by {@link #apiClass()}, to
     * which this {@link FSForeignKey} points
     */
    String columnName();

    /**
     * <p>
     *     Specify a compositeId if you intend to make a composite foreign key. You just have
     *     to be sure that the other columns in the composite foreign key have the same
     *     composite id. Empty string specifies that you do not want a composite foreign key.
     * </p>
     * @return the compositeId for this foreign key so that it can be composited
     */
    String compositeId() default "";

    /**
     * <p>
     *     defaults to empty string--so the update action depends upon the DBMS that you use
     *     For example, in SQLite, the following actions are valid:
     *     <ul>
     *         <li>NO ACTION (the default for SQLite)</li>
     *         <li>RESTRICT</li>
     *         <li>SET NULL</li>
     *         <li>SET DEFAULT</li>
     *         <li>CASCADE</li>
     *     </ul>
     * </p>
     * @return the change action that should take place if the foreign key column is updated
     */
    String updateAction() default "";

    /**
     * <p>
     *     defaults to empty string--so the delete action depends upon the DBMS that you use
     *     For example, in SQLite, the following actions are valid:
     *     <ul>
     *         <li>NO ACTION (the default for SQLite)</li>
     *         <li>RESTRICT</li>
     *         <li>SET NULL</li>
     *         <li>SET DEFAULT</li>
     *         <li>CASCADE</li>
     *     </ul>
     * </p>
     * @return the change action that should take place if the foreign key record is deleted
     */
    String deleteAction() default "";
}
