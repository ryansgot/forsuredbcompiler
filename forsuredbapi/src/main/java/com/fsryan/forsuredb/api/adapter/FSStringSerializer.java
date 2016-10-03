package com.fsryan.forsuredb.api.adapter;

import java.lang.reflect.Type;

public abstract class FSStringSerializer implements FSSerializer {
    @Override
    public final boolean storeAsBlob() {
        return false;
    }

    @Override
    public final byte[] createBlobDoc(Type type, Object val) {
        return createStringDoc(type, val).getBytes();
    }

    @Override
    public final <T> T fromStorage(Type typeOfT, byte[] objectBytes) {
        return fromStorage(typeOfT, new String(objectBytes));
    }
}
