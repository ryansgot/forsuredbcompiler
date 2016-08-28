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
 *     Use the FSColumn annotation on methods defined in your extensions of
 *     {@link FSGetApi FSGetApi} in order to specify the column name associated
 *     with the method.
 * </p>
 * @author Ryan Scott
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface FSColumn {
    /**
     * @return the name of the column
     */
    String value();

    /**
     * <p>
     *     defaults to true. changing to false will cause the compiler to fail to generate the appropriate
     *     method that will allow you to filter results by this column. In other words, if set to false,
     *     queries will not contain WHERE clauses with this column.
     * </p>
     * <p>
     *     Why would you want to flip this to false? forsuredbcompiler will generate methods as part of the
     *     fluent api for querying allowing you filter results by some property of the column. For some
     *     data types, there are several methods that get generated. You may not want to generate methods
     *     that you will never use.
     * </p>
     * <p>
     *     Note that flipping this to false will mean that you cannot filter your results by this column
     *     at all.
     * </p>
     * @return whether the table is searchable by this column
     */
    boolean searchable() default true;

    /**
     * <p>
     *     defaults to true. changing to false will cause the compiler to fail to generate the appropriate
     *     method that will allow you to order results by this column. In other words, if set to false,
     *     queries will not contain ORDER BY clauses including this column
     * </p>
     * <p>
     *     Why would you want to flip this to false? forsuredbcompiler will generate methods as part of the
     *     fluent api for querying allowing you to set arbitrary orders to your data set. However, you may
     *     not want to generate methods that you will not use.
     * </p>
     * @return whether retrieval of records may be ordered by this column
     */
    boolean orderable() default true;
}
