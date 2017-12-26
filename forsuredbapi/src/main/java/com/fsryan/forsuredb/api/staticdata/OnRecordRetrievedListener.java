package com.fsryan.forsuredb.api.staticdata;

import com.fsryan.forsuredb.api.RecordContainer;

import java.util.List;
import java.util.Map;

public interface OnRecordRetrievedListener {
    void onRecord(Map<Integer, List<RecordContainer>> versionToRecordsMap);
}
