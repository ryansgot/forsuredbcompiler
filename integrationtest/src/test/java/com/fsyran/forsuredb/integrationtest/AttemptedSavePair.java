package com.fsyran.forsuredb.integrationtest;

import com.fsryan.forsuredb.api.SaveResult;
import com.fsryan.forsuredb.queryable.DirectLocator;

public class AttemptedSavePair<R> {
    public final SaveResult<DirectLocator> result;
    public final R attempted;

    public AttemptedSavePair(SaveResult<DirectLocator> result, R attempted) {
        this.result = result;
        this.attempted = attempted;
    }
}
