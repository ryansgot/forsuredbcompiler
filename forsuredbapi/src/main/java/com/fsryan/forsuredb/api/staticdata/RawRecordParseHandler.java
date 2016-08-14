package com.fsryan.forsuredb.api.staticdata;

import com.fsryan.forsuredb.api.FSLogger;
import org.xml.sax.Attributes;

import java.util.HashMap;
import java.util.Map;

/*package*/ class RawRecordParseHandler extends ParseHandler<Map<String, String>> {

    /*package*/ RawRecordParseHandler(String recordName, FSLogger log, Parser.RecordListener<Map<String, String>> recordListener) {
        super(recordName, log, recordListener);
    }

    protected Map<String, String> createRecord(Attributes attributes) {
        Map<String, String> rawRecord = new HashMap<>();
        for (int i = 0; i < attributes.getLength(); i++) {
            rawRecord.put(attributes.getQName(i), attributes.getValue(i));
        }
        return rawRecord;
    }
}
