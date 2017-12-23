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
import com.fsryan.forsuredb.api.TestData;
import com.fsryan.forsuredb.gsonserialization.FSDbInfoGsonSerializer;
import com.fsryan.forsuredb.migration.MigrationSet;
import com.fsryan.forsuredb.serialization.FSDbInfoSerializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.fsryan.forsuredb.api.TestData.resourceText;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class ParserHandlerTest {

    private static final FSDbInfoSerializer dbInfoSerializer = new FSDbInfoGsonSerializer();

    private OnRecordRetrievedListener mockRetrieveListener;

    private final List<MigrationSet> migrationSets;
    private final String staticDataXmlResource;
    private final DateFormat dateFormat;
    private final String tableName;
    private final List<Integer> expectedRecordContainerSizes;
    private InputStream xmlStream;

    public ParserHandlerTest(List<String> migrationSetResources,
                             String staticDataXmlResource,
                             String dateFormatStr,
                             String tableName,
                             List<Integer> expectedRecordContainerSizes) throws IOException {
        migrationSets = parseMigrationSets(migrationSetResources);
        this.staticDataXmlResource = staticDataXmlResource;
        dateFormat = new SimpleDateFormat(dateFormatStr);
        this.tableName = tableName;
        this.expectedRecordContainerSizes = expectedRecordContainerSizes;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {   // 00: properly ordered db_versions result in correctly sized and ordered record containers--all migrations
                        Arrays.asList(
                                "three_versions_static_data_migration_1.json",  // defines 7 non-default columns
                                "three_versions_static_data_migration_2.json",  // defines 5 more non-default columns
                                "three_versions_static_data_migration_3.json"   // defines 3 more non-default columns
                        ),
                        "three_versions_static_data.xml",
                        "yyyy-MM-DD HH:mm:ss.SSS",
                        "all_types",
                        Arrays.asList(7, 12, 15)
                },
                {   // 01: incorrectly ordered db_versions result in correctly sized
                        Arrays.asList(
                                "three_versions_static_data_migration_1.json",  // defines 7 non-default columns
                                "three_versions_static_data_migration_2.json",  // defines 5 more non-default columns
                                "three_versions_static_data_migration_3.json"   // defines 3 more non-default columns
                        ),
                        "three_versions_with_misordered_db_versions.xml",
                        "yyyy-MM-DD HH:mm:ss.SSS",
                        "all_types",
                        Arrays.asList(7, 12, 15)
                },
                {   // 02: correctly ordered db_versions result in correctly sized  and ordered record containers--migrations 2 and 3
                        Arrays.asList(
                                "three_versions_static_data_migration_2.json",  // defines 5 more non-default columns
                                "three_versions_static_data_migration_3.json"   // defines 3 more non-default columns
                        ),
                        "three_versions_static_data.xml",
                        "yyyy-MM-DD HH:mm:ss.SSS",
                        "all_types",
                        Arrays.asList(12, 15)
                },
                {   // 03: incorrectly ordered db_versions result in correctly sized  and ordered record containers--migrations 2 and 3
                        Arrays.asList(
                                "three_versions_static_data_migration_2.json",  // defines 5 more non-default columns
                                "three_versions_static_data_migration_3.json"   // defines 3 more non-default columns
                        ),
                        "three_versions_static_data.xml",
                        "yyyy-MM-DD HH:mm:ss.SSS",
                        "all_types",
                        Arrays.asList(12, 15)
                },
                {   // 04: reverse ordered db_versions result in correctly sized  and ordered record containers--migrations 2 and 3
                        Arrays.asList(
                                "three_versions_static_data_migration_1.json",  // defines 7 non-default columns
                                "three_versions_static_data_migration_2.json",  // defines 5 more non-default columns
                                "three_versions_static_data_migration_3.json"   // defines 3 more non-default columns
                        ),
                        "three_versions_static_data.xml",
                        "yyyy-MM-DD HH:mm:ss.SSS",
                        "all_types",
                        Arrays.asList(7, 12, 15)
                }
        });
    }

    @Before
    public void setUp() {
        mockRetrieveListener = mock(OnRecordRetrievedListener.class);
        xmlStream = TestData.resourceStream(staticDataXmlResource);
    }

    @After
    public void tearDown() throws Exception {
        if (xmlStream != null) {
            xmlStream.close();
        }
    }

    @Test
    public void testParserFindsEachMigrationLine() {
        ArgumentCaptor<Map> mapCaptor = ArgumentCaptor.forClass(Map.class);

        StaticDataRetrieverFactory.createFor(dateFormat, tableName, migrationSets, xmlStream)
                .retrieve(mockRetrieveListener);

        verify(mockRetrieveListener).onRecord(mapCaptor.capture());

        Map<Integer, List<RecordContainer>> actual = (Map<Integer, List<RecordContainer>>) mapCaptor.getValue();
        List<RecordContainer> sortedActual = sortRecordContainers(actual);

        for (int idx = 0; idx < expectedRecordContainerSizes.size(); idx++) {
            assertEquals(expectedRecordContainerSizes.get(idx).intValue(), sortedActual.get(idx).keySet().size());
        }
    }

    private static List<MigrationSet> parseMigrationSets(List<String> migrationSetResources) throws IOException {
        List<MigrationSet> ret = new ArrayList<>(migrationSetResources.size());
        for (String migrationSetResource : migrationSetResources) {
            ret.add(dbInfoSerializer.deserializeMigrationSet(resourceText(migrationSetResource)));
        }
        return ret;
    }

    // compresses the returned Map of lists into one list to make testing easier
    private static List<RecordContainer> sortRecordContainers(Map<Integer, List<RecordContainer>> actual) {
        List<Integer> indexOrder = new ArrayList<>(actual.keySet());
        Collections.sort(indexOrder);

        List<RecordContainer> ret = new ArrayList<>();
        for (Integer version : indexOrder) {
            ret.addAll(actual.get(version));
        }
        return ret;
    }
}
