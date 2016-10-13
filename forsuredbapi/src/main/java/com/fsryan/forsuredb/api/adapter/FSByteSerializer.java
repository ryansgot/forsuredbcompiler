package com.fsryan.forsuredb.api.adapter;

import java.lang.reflect.Type;

public abstract class FSByteSerializer implements FSSerializer {

    @Override
    public final boolean storeAsBlob() {
        return true;
    }

    @Override
    public final String createStringDoc(Type type, Object val) {
        return new String(createBlobDoc(type, val));
    }

    @Override
    public final <T> T fromStorage(Type typeOfT, String stringRepresentation) {
        return fromStorage(typeOfT, stringRepresentation.getBytes());
    }
}
