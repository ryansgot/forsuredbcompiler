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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;

/**
 * <p>
 *     Handler for {@link SAXParser SAXParser} that is capable of handling static data XML
 * </p>
 * @author Ryan Scott
 */
/*package*/ abstract class ParseHandler<T> extends DefaultHandler {

    private final String recordName;
    private final FSLogger log;
    private final Parser.RecordListener<T> recordListener;

    /*package*/ ParseHandler(String recordName, FSLogger log, Parser.RecordListener<T> recordListener) {
        this.recordName = recordName;
        this.log = log == null ? new FSLogger.SilentLog() : log;
        this.recordListener = recordListener == null ? (Parser.RecordListener<T>) Parser.RecordListener.NOOP : recordListener;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (!isRecordElement(qName)) {
            return;
        }

        log.i("found " + recordName);
        recordListener.onRecord(createRecord(attributes));
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        log.i("End Element :" + qName + " with localName: " + localName);
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        log.i("characters: " + new String(ch, start, length));
    }

    protected abstract T createRecord(Attributes attributes);

    private boolean isRecordElement(String qName) {
        return qName != null && recordName.equals(qName);
    }
}
