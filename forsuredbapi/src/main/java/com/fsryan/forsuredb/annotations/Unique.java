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
 *     Use the Unique annotation on methods defined in your extensions of {@link FSGetApi FSGetApi}
 *     in order to mark this column as unique.
 * </p>
 * <p>
 *     Due to the fact that you can create an index that is not unique, there is also a
 *     {@link Index} annotation. Annotating an {@link com.fsryan.forsuredb.api.FSGetApi} method with
 *     both {@link Unique} and {@link Index} will have the same effect as setting {@link #index()}
 *     to true.
 * </p>
 * @author Ryan Scott
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Unique {
    boolean index() default false;
}
