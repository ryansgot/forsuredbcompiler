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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * <p>
 *     Wrapper for a SAXParser that simplifies calls to parse
 * </p>
 * @author Ryan Scott
 */
/*package*/ class Parser<T extends ParseHandler<T>> {

    public interface RecordListener<T> {
        void onRecord(T record);
        RecordListener<?> NOOP = new RecordListener() {
            public void onRecord(Object record) {}
        };
    }

    public static <T> void parse(String staticDataFilePath, ParseHandler<T> parseHandler) throws FileNotFoundException {
        parse(new File(staticDataFilePath), parseHandler);
    }

    public static <T> void parse(File staticDataFile, ParseHandler<T> parseHandler) throws FileNotFoundException {
        FileInputStream fis = new FileInputStream(staticDataFile);
        try {
            parse(fis, parseHandler);
        } finally {
            try {
                fis.close();
            } catch (IOException ioe) {
                // can't do anything about this
            }
        }
    }

    public static <T> void parse(InputStream xmlStream, ParseHandler<T> parseHandler) {
        try {
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(xmlStream, parseHandler);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
