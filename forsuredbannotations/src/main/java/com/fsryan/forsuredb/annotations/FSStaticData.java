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
 * <p>Use the FSStaticData annotation on an {@link FSGetApi} extension to
 * direct forsuredb to the static data XML asset you have prepared for this
 * table. For an example, see {@link #value()}
 *
 * @author Ryan Scott
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface FSStaticData {

    /**
     * <p><i>This string must define the asset relative to your project's
     * assets directory, and the asset must be an XML file as below:</i>
     * <pre>{@code
     * <static_data>
     *   <records db_version="1">
     *     <!-- inserted only after migration db version 0 to 1 -->
     *     <record
     *       byte_array_column="abc123"
     *       int_column="65536"
     *       long_column="500000000000" />
     *     <!-- you need not specify all columns -->
     *     <record
     *       int_column="65536"
     *       long_column="500000000000" />
     *   </records>
     *   <records db_version="3">
     *     <!-- inserted only after migration db version 2 to 3 -->
     *     <!-- here, a fourth column of type double has been added -->
     *     <record
     *       byte_array_column="abc123"
     *       int_column="65536"
     *       long_column="500000000000"
     *       double_column="0.0000001" />
     *     <record
     *       byte_array_column="abc123"
     *       int_column="65536"
     *       long_column="500000000000"
     *       double_column="0.0000002" />
     *   </records>
     * </static_data>
     * }</pre>
     *
     * <p>Note that the above XML schema is relevant since version 0.13.0.
     * Previous versions did not take database version into account when
     * inserting static data, making this feature nearly useless.
     *
     * @return The filename of the XML asset that defines static data for
     * the table defined in the {@link FSGetApi} extension annotated by
     * {@link FSStaticData}
     */
    String value();
}
