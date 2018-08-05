package com.fsryan.forsuredb.api.adapter;

import java.lang.reflect.Type;

public abstract class FSByteArraySerializer implements FSSerializer {

    @Override
    public final boolean storeAsBlob(Type type) {
        return true;
    }

    @Override
    public final String createStringDoc(Type type, Object val) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final <T> T fromStorage(Type typeOfT, String stringRepresentation) {
        throw new UnsupportedOperationException();
    }
}
