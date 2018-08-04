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
import com.fsryan.forsuredb.api.sqlgeneration.Sql;
import com.fsryan.forsuredb.migration.MigrationSet;

import javax.annotation.Nonnull;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.util.List;

/**
 * <p>Factory that creates {@link StaticDataRetriever StaticDataRetriever} objects that are capable
 * of parsing {@link RecordContainer RecordContainer} objects from static data XML
 * @author Ryan Scott
 */
public abstract class StaticDataRetrieverFactory {

    public static StaticDataRetriever createFor(@Nonnull String tableName,
                                                @Nonnull List<MigrationSet> migrationSets,
                                                @Nonnull URL staticDataAsset) throws IOException {
        return createFor(tableName, migrationSets, staticDataAsset.openStream());
    }

    public static StaticDataRetriever createFor(@Nonnull String tableName,
                                                @Nonnull List<MigrationSet> migrationSets,
                                                @Nonnull final InputStream xmlStream) {
        return createFor(Sql.generator().getDateFormat(), tableName, migrationSets, xmlStream);
    }

    static StaticDataRetriever createFor(@Nonnull final DateFormat dateFormat,
                                         @Nonnull final String tableName,
                                         @Nonnull final List<MigrationSet> migrationSets,
                                         @Nonnull final InputStream xmlStream) {
        return new StaticDataRetriever() {
            @Override
            public void retrieve(OnRecordRetrievedListener recordRetrievalListener) {
                try {
                    ParseHandler parseHandler = new ParseHandler(
                            dateFormat,
                            tableName,
                            migrationSets,
                            recordRetrievalListener
                    );
                    SAXParserFactory.newInstance().newSAXParser().parse(xmlStream, parseHandler);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                } finally {
                    try {
                        xmlStream.close();
                    } catch (IOException ioe) {
                        // TODO: check what to do
                    }
                }
            }
        };
    }
}
