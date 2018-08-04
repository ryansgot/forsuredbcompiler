package com.fsryan.forsuredb.api.adapter;

import org.junit.Test;

import java.lang.reflect.Type;

import static org.junit.Assert.assertTrue;

public class FSByteArraySerializerTest {

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowUnsupportedOperationExceptionWhenCreatingStringDoc() {
        createSerializer().createStringDoc(String.class, "");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowUnsupportedOperationExceptionWhenDeserializingStringDoc() {
        createSerializer().fromStorage(String.class, "");
    }

    @Test
    public void shouldReturnTrueWhenAskingWhetherToStoreAsBlob() {
        assertTrue(createSerializer().storeAsBlob(Object.class));
    }

    private FSByteArraySerializer createSerializer() {
        return new FSByteArraySerializer() {

                    @Override
                    public byte[] createBlobDoc(Type type, Object val) {
                        return new byte[0];
                    }

                    @Override
                    public <T> T fromStorage(Type typeOfT, byte[] objectBytes) {
                        return null;
                    }
                };
    }
}
