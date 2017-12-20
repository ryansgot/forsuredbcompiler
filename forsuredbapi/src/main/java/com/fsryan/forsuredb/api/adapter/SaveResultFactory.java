package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.api.SaveResult;

public abstract class SaveResultFactory {
    public static <U> SaveResult<U> create(final U inserted, final int rowsAffected, final Exception e) {
        return new SaveResult<U>() {
            @Override
            public Exception exception() {
                return e;
            }

            @Override
            public U inserted() {
                return inserted;
            }

            @Override
            public int rowsAffected() {
                return rowsAffected;
            }
        };
    }
}
