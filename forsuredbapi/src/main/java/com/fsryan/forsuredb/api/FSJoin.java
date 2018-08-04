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

import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import java.util.Map;

@AutoValue
public abstract class FSJoin {

    /**
     * <p>
     *     Corresponds to some of the types of joins that are possible. Future versions may
     *     have more joins, but the most common is the {@link #INNER} type.
     * </p>
     */
    public enum Type {
        /**
         * <p>
         *     It is debatable whether this is useful, given the choices that have been made
         *     in the forsuredb project thus far
         * </p>
         */
        NATURAL,
        LEFT,

        /**
         * <p>
         *     Probably the most common, and if you're wondering which {@link FSJoin.Type}
         *     you should use, then it's probably this one.
         * </p>
         */
        INNER,
        OUTER,
        LEFT_OUTER,
        CROSS;

        @Override
        public String toString() {
            return name().replace("_", " ");
        }
    }

    public static FSJoin create(@Nonnull Type type, @Nonnull String parent, @Nonnull String child, @Nonnull Map<String, String> childToParentColumnMap) {
        return new AutoValue_FSJoin(type, parent, child, childToParentColumnMap);
    }

    public abstract Type type();
    public abstract String parentTable();
    public abstract String childTable();
    public abstract Map<String, String> childToParentColumnMap();
}
