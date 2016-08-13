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


import com.fsryan.forsuredb.api.FSLogger;
import com.fsryan.forsuredb.api.RecordContainer;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *     Factory that creates {@link StaticDataRetriever StaticDataRetriever} objects that are capable
 *     of getting {@link RecordContainer RecordContainer} objects.
 * </p>
 * @author Ryan Scott
 */
public class StaticDataRetrieverFactory {

    private final FSLogger log;

    public StaticDataRetrieverFactory(FSLogger log) {
        this.log = log == null ? new FSLogger.SilentLog() : log;
    }

    /**
     * @param xmlStream {@link InputStream InputStream} that <i>MUST</i> be XML
     * @return A {@link StaticDataRetriever StaticDataRetriever} that can get
     * {@link RecordContainer RecordContainer} objects given the {@link InputStream InputStream}
     */
    public StaticDataRetriever fromStream(final InputStream xmlStream) {
        if (xmlStream == null) {
            return new StaticDataRetriever() {
                @Override
                public List<RecordContainer> getRecords(String recordName) {
                    return Collections.emptyList();
                }

                @Override
                public List<Map<String, String>> getRawRecords(String recordName) {
                    return Collections.emptyList();
                }
            };
        }

        return new StaticDataRetriever() {
            List<RecordContainer> records;
            List<Map<String, String>> rawRecords;

            @Override
            public List<RecordContainer> getRecords(final String recordName) {
                if (records != null) {
                    return records;
                }

                records = new LinkedList<>();
                Parser.parse(xmlStream, new RecordContainerParseHandler(recordName, log, new Parser.RecordListener<RecordContainer>() {
                    @Override
                    public void onRecord(RecordContainer record) {
                        records.add(record);
                    }
                }));

                return records;
            }

            @Override
            public List<Map<String, String>> getRawRecords(String recordName) {
                if (rawRecords != null) {
                    return rawRecords;
                }

                records = new LinkedList<>();
                Parser.parse(xmlStream, new RawRecordParseHandler(recordName, log, new Parser.RecordListener<Map<String, String>>() {
                    @Override
                    public void onRecord(Map<String, String> record) {
                        rawRecords.add(record);
                    }
                }));

                return rawRecords;
            }
        };
    }
}
