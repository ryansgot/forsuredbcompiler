package com.fsryan.forsuredb.api.adapter;

import java.lang.reflect.Type;

public abstract class FSStringSerializer implements FSSerializer {

    @Override
    public final boolean storeAsBlob(Type type) {
        return false;
    }

    @Override
    public final byte[] createBlobDoc(Type type, Object val) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final <T> T fromStorage(Type typeOfT, byte[] blob) {
        throw new UnsupportedOperationException();
    }
}
