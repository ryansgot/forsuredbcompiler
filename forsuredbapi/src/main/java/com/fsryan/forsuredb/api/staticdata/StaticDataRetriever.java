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
package com.fsryan.forsuredb.api.staticdata;


import com.fsryan.forsuredb.api.RecordContainer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *     An interface for getting {@link RecordContainer} objects describing the forsuredb specific format
 *     or {@link Map} objects that describe a raw key-value record format
 * </p>
 * @author Ryan Scott
 */
public interface StaticDataRetriever {
    StaticDataRetriever NOOP = new StaticDataRetriever() {
        @Override
        public List<RecordContainer> getRecords(String recordName) {
            return Collections.emptyList();
        }

        @Override
        public List<Map<String, String>> getRawRecords(String recordName) {
            return Collections.emptyList();
        }
    };

    List<RecordContainer> getRecords(String recordName);
    List<Map<String, String>> getRawRecords(String recordName);
}
