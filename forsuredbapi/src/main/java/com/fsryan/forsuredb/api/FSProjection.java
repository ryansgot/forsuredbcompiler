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

public interface FSProjection {

    FSProjection COUNT = new FSProjection() {
        @Override
        public String tableName() {
            return null;
        }

        @Override
        public String[] columns() {
            return new String[0];
        }

        @Override
        public boolean isDistinct() {
            return false;
        }
    };

    /**
     * @return The name of the table to which the column corresponds
     */
    String tableName();

    /**
     * @return All of the columns to be retrieved in a SELECT query
     */
    String[] columns();

    /**
     * @return Whether the query should be for distinct column values.
     */
    boolean isDistinct();
}
