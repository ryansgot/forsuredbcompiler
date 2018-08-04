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
 * <p>Use the FSStaticData annotation on an {@link FSGetApi FSGetApi} extension to direct
 * the forsuredb to the static data XML asset you have prepared for this table. For an
 * example, {@link #value()} () asset()}
 * </p>
 * @author Ryan Scott
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface FSStaticData {

    /**
     * <p><i>This string must define the asset relative to your project's assets directory, and the
     * asset must be an XML file as below:</i>
     * <pre>{@code
     * <static_data>
     *   <records db_version="1" /><!-- will be inserted after running migration from db_version 0 to 1-->
     *     <record column1="column1_value" column2="column2_value" column3="column3_value" />
     *     <record column1="column1_value" column2="column2_value" column3="column3_value" />
     *   </records>
     *   <records db_version="3" /><!-- will be inserted after running migration from db_version 2 to 3-->
     *     <record column1="column1_value" column2="column2_value" column3="column3_value" column4="column4_value" />
     *     <record column1="column1_value" column2="column2_value" column3="column3_value" column4="column4_value" />
     *   </records>
     * </static_data>
     * }</pre>
     * <p>Note that the above schema is relevant since version 0.13.0.
     * @return The filename of the XML asset that defines static data for the table defined in the
     * {@link FSGetApi FSGetApi} extension that this FSStaticData annotation
     * annotates.
     */
    String value();
}
