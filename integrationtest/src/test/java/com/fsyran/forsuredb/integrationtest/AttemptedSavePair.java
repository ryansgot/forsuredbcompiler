package com.fsyran.forsuredb.integrationtest;

import com.fsryan.forsuredb.api.SaveResult;
import com.fsryan.forsuredb.queryable.DirectLocator;

public class AttemptedSavePair<R> extends Pair<SaveResult<DirectLocator>, R> {

    public AttemptedSavePair(SaveResult<DirectLocator> result, R attempted) {
        super(result, attempted);
    }

    public SaveResult<DirectLocator> getResult() {
        return first;
    }

    public R getRecord() {
        return second;
    }
}
