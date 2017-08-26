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

/**
 * <p>
 *     Describes a WHERE clause of an SQL query in (possibly) two parts, a {@link #where() where()}
 *     String that perhaps has '?' characters to be replaced by the array of String returned by
 *     {@link #replacements() replacements()}.
 * </p>
 */
public interface FSSelection {

    FSSelection ALL = new FSSelection() {

        @Override
        public String where() {
            return "";
        }

        @Override
        public String[] replacements() {
            return new String[0];
        }

        @Override
        public RetrieverLimits retrieverLimits() {
            return RetrieverLimits.NONE;
        }
    };

    /**
     * @return the String where clause, possibly including '?' characters to be replaced by Strings
     * in the {@link #replacements() replacements()} String array
     */
    String where();

    /**
     * @return the array of Strings that are to replace all '?' characters in String returned by
     * {@link #where() where()}.
     */
    String[] replacements();

    /**
     * Allows you to position the start/end position and number of records from which a retreiver
     * can retrieve values
     * @return a {@link RetrieverLimits} describing any limits to put on the returned records
     */
    RetrieverLimits retrieverLimits();
}
