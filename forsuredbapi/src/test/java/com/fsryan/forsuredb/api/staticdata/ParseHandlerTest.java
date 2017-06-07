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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayInputStream;
import java.util.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static com.fsryan.forsuredb.api.CollectionUtil.arrayListOf;
import static com.fsryan.forsuredb.api.CollectionUtil.mapOf;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class ParseHandlerTest {

    private SAXParser saxParser;
    private ByteArrayInputStream xmlStream;
    private final List<RecordContainer> parsedRecordContainers = new ArrayList<>();
    private final List<Map<String, String>> parsedRawRecords = new ArrayList<>();
    private final Parser.RecordListener<RecordContainer> recordContainerListener = new Parser.RecordListener<RecordContainer>() {
        @Override
        public void onRecord(RecordContainer recordContainer) {
            parsedRecordContainers.add(recordContainer);
        }
    };
    private final Parser.RecordListener<Map<String, String>> rawRecordListener = new Parser.RecordListener<Map<String, String>>() {
        @Override
        public void onRecord(Map<String, String> rawRecord) {
            parsedRawRecords.add(rawRecord);
        }
    };

    private String inputXml;
    private String recordName;
    private List<Map<String, String>> expected;

    public ParseHandlerTest(String inputXml, String recordName, List<Map<String, String>> expected) {
        this.inputXml = inputXml;
        this.recordName = recordName;
        this.expected = expected;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // the basic success case with some values
                {
                        new StringBuffer().append("<static_data>\n")
                                          .append("<user global_id=\"1\" login_count=\"2\" app_rating=\"4.3\" competitor_app_rating=\"3.5\" />\n")
                                          .append("</static_data>")
                                          .toString(),
                        "user",
                        arrayListOf(mapOf("global_id", "1",
                                                 "login_count", "2",
                                                 "app_rating", "4.3",
                                                 "competitor_app_rating", "3.5"))
                },
                // wrong record name (users instead of user)
                {
                        new StringBuffer().append("<static_data>\n")
                                          .append("<user global_id=\"1\" login_count=\"2\" app_rating=\"4.3\" competitor_app_rating=\"3.5\" />\n")
                                          .append("</static_data>")
                                          .toString(),
                        "users",
                        Collections.<Map<String, String>>emptyList()
                },
                // multiple records
                {
                        new StringBuffer().append("<static_data>\n")
                                          .append("<user global_id=\"1\" login_count=\"2\" app_rating=\"4.3\" competitor_app_rating=\"3.5\" />\n")
                                          .append("<user global_id=\"2\" login_count=\"3\" app_rating=\"5.4\" competitor_app_rating=\"4.6\" />\n")
                                          .append("</static_data>")
                                          .toString(),
                        "user",
                        arrayListOf(mapOf("global_id", "1",
                                          "login_count", "2",
                                          "app_rating", "4.3",
                                          "competitor_app_rating", "3.5"),
                                    mapOf("global_id", "2",
                                          "login_count", "3",
                                          "app_rating", "5.4",
                                          "competitor_app_rating", "4.6"))
                },
        });
    }



    @Before
    public void setUp() throws Exception {
        xmlStream = new ByteArrayInputStream(inputXml.getBytes());
        saxParser = SAXParserFactory.newInstance().newSAXParser();
    }

    @After
    public void tearDown() throws Exception {
        xmlStream.close();
        saxParser = null;
        parsedRecordContainers.clear();
        parsedRawRecords.clear();
    }

    @Test
    public void shouldHaveCorrectNumberOfRawRecords() throws Exception {
        saxParser.parse(xmlStream, new RawRecordParseHandler(recordName, new FSLogger.DefaultFSLogger(), rawRecordListener));
        assertEquals(expected.size(), parsedRawRecords.size());
    }

    @Test
    public void shouldHaveCorrectNumberOfRecordContainerRecords() throws Exception {
        saxParser.parse(xmlStream, new RecordContainerParseHandler(recordName, new FSLogger.DefaultFSLogger(), recordContainerListener));
        assertEquals(expected.size(), parsedRecordContainers.size());
    }

    @Test
    public void shouldHaveCorrectRawRecords() throws Exception {
        saxParser.parse(xmlStream, new RawRecordParseHandler(recordName, new FSLogger.DefaultFSLogger(), rawRecordListener));
        for (int idx = 0; idx < expected.size(); idx++) {
            validateExpectedKeysAndValues(expected.get(0), parsedRawRecords.get(0));
        }
    }

    @Test
    public void shouldHaveCorrectRecordContainerRecords() throws Exception {
        saxParser.parse(xmlStream, new RecordContainerParseHandler(recordName, new FSLogger.DefaultFSLogger(), recordContainerListener));
        for (int idx = 0; idx < expected.size(); idx++) {
            validateMapEqualsRecordContainer(expected.get(0), parsedRecordContainers.get(0));
        }
    }

    private void validateExpectedKeysAndValues(Map<String, String> expected, Map<String, String> actual) {
        for (Map.Entry<String, String> entry : expected.entrySet()) {
            assertNotNull("Did not parse " + entry.getKey(), actual.get(entry.getKey()));
            assertEquals(entry.getValue(), actual.get(entry.getKey()));
        }
    }

    private void validateMapEqualsRecordContainer(Map<String, String> expected, RecordContainer recordContainer) {
        for (Map.Entry<String, String> entry : expected.entrySet()) {
            assertNotNull("Did not parse " + entry.getKey(), recordContainer.get(entry.getKey()));
            assertEquals(entry.getValue(), recordContainer.get(entry.getKey()));
        }
    }
}
