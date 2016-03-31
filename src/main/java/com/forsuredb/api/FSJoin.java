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

public interface FSJoin {

    /**
     * <p>
     *     Corresponds to some of the types of joins that are possible. Future versions may
     *     have more joins, but the most common is the {@link #INNER} type.
     * </p>
     */
    enum Type {
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

    /**
     * @return the {@link FSJoin.Type} of this join
     */
    Type type();

    /**
     * @return the name of the table that is the parent in this relationship. This need not be
     * the table you're querying.
     * @see #childTable()
     */
    String parentTable();

    /**
     * @return The column in the parent table that should correspond to the {@link #childColumn()}.
     * These columns need not have the same name.
     * @see #childColumn()
     */
    String parentColumn();

    /**
     * @return the name of the table that is the child in this relationship.
     * @see #parentTable()
     */
    String childTable();

    /**
     * @return The column in the child table that should correspond to the {@link #parentColumn()}.
     * These columns need not have the same name.
     * @see #parentColumn()
     */
    String childColumn();
}
