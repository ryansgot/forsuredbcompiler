package com.fsryan.forsuredb.api.adapter;

import org.junit.Test;

import java.lang.reflect.Type;

import static org.junit.Assert.assertFalse;

public class FSStringSerializerTest {

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowUnsupportedOperationExceptionWhenCreatingBlobDoc() {
        createSerializer().createBlobDoc(String.class, new byte[0]);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowUnsupportedOperationExceptionWhenDeserializingBlobDoc() {
        createSerializer().fromStorage(String.class, new byte[0]);
    }

    @Test
    public void shouldReturnTrueWhenAskingWhetherToStoreAsBlob() {
        assertFalse(createSerializer().storeAsBlob(Object.class));
    }

    private FSStringSerializer createSerializer() {
        return new FSStringSerializer() {
            @Override
            public String createStringDoc(Type type, Object val) {
                return null;
            }

            @Override
            public <T> T fromStorage(Type typeOfT, String stringRepresentation) {
                return null;
            }
        };
    }
}
