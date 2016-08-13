package com.fsryan.forsuredb.api.staticdata;

import com.fsryan.forsuredb.api.FSLogger;
import com.fsryan.forsuredb.api.RecordContainer;
import com.fsryan.forsuredb.api.TypedRecordContainer;
import org.xml.sax.Attributes;

/*package*/ class RecordContainerParseHandler extends ParseHandler<RecordContainer> {

    RecordContainerParseHandler(String recordName, FSLogger log, Parser.RecordListener<RecordContainer> recordListener) {
        super(recordName, log, recordListener);
    }

    @Override
    protected RecordContainer createRecord(Attributes attributes) {
        TypedRecordContainer recordContainer = new TypedRecordContainer();
        for (int i = 0; i < attributes.getLength(); i++) {
            recordContainer.put(attributes.getQName(i), attributes.getValue(i));
        }
        return recordContainer;
    }
}
