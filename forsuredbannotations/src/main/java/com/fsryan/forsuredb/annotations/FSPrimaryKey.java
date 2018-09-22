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

import com.fsryan.forsuredb.info.TableInfo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <p>Use the FSPrimaryKey annotation on your extensions of
 * {@link com.fsryan.forsuredb.api.FSGetApi FSGetApi} in order to specify
 * that the columns are a primary key in the table. If you do not specify,
 * then the automatically created _id column will become the primary key.
 *
 * @author Ryan Scott
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface FSPrimaryKey {
    /**
     * <p>Defaults to {@link TableInfo#DEFAULT_PRIMARY_KEY_COLUMN}
     * @return the names of the columns of the primary key
     */
    String[] value() default {TableInfo.DEFAULT_PRIMARY_KEY_COLUMN};

    /**
     * <p> Defaults to the empty string, which will result in the DBMS default
     * being used
     *
     * <p>If your DBMS is SQLite, then the following values are possible:
     * <ul>
     *   <li>ROLLBACK</li>
     *   <li>ABORT (which is default for SQLite)</li>
     *   <li>FAIL</li>
     *   <li>IGNORE</li>
     *   <li>REPLACE</li>
     * </ul>
     * @return the on-conflict algorithm name
     */
    String onConflict() default "";
}
