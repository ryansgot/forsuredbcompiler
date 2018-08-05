package com.fsryan.forsuredb.api.adapter;

import java.lang.reflect.Type;

public interface FSSerializer {
    /**
     * @return true if your serializer serializes best to a byte array. Otherwise, return false
     */
    boolean storeAsBlob(Type type);
    String createStringDoc(Type type, Object val);
    byte[] createBlobDoc(Type type, Object val);
    <T> T fromStorage(Type typeOfT, byte[] objectBytes);
    <T> T fromStorage(Type typeOfT, String stringRepresentation);
}
