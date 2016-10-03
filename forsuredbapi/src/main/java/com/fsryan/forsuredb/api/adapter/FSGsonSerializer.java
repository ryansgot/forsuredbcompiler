package com.fsryan.forsuredb.api.adapter;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * <p>
 *     If you want to use {@link Gson}, then you can use this default implementation.
 *     Extending this class is strongly discouraged because you should not need to ever
 *     do so.
 * </p>
 */
public class FSGsonSerializer extends FSStringSerializer {

    private final Gson gson;

    public FSGsonSerializer() {
        this(null);
    }

    public FSGsonSerializer(Gson gson) {
        this.gson = gson == null ? new Gson() : gson;
    }

    @Override
    public String createStringDoc(Type type, Object val) {
        return gson.toJson(val, type);
    }

    @Override
    public <T> T fromStorage(Type typeOfT, String stringRepresentation) {
        return gson.fromJson(stringRepresentation, typeOfT);
    }
}
