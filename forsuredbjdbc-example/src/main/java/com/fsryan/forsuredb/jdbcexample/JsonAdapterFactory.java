package com.fsryan.forsuredb.jdbcexample;

import com.fsryan.forsuredb.api.adapter.FSSerializer;
import com.fsryan.forsuredb.api.adapter.FSSerializerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public class JsonAdapterFactory implements FSSerializerFactory {

    private static final Gson gson = new GsonBuilder().setDateFormat(ExampleApp.DATE_FORMAT_STRING).create();

    @Override
    public FSSerializer create() {
        return new FSSerializer() {
            @Override
            public boolean storeAsBlob() {
                return false;
            }

            @Override
            public String createStringDoc(Type type, Object val) {
                return gson.toJson(val, type);
            }

            @Override
            public byte[] createBlobDoc(Type type, Object val) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> T fromStorage(Type typeOfT, byte[] objectBytes) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> T fromStorage(Type typeOfT, String stringRepresentation) {
                return gson.fromJson(stringRepresentation, typeOfT);
            }
        };
    }
}
