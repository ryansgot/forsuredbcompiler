package com.fsryan.forsuredb.api.adapter;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;

import static com.google.common.base.Strings.isNullOrEmpty;

/*package*/ class JsonAdapterHelper {

    private final String fsJsonAdapterFactoryClass;

    public JsonAdapterHelper() {
        this(System.getProperty("fsJsonAdapterFactoryClass"));
    }

    @VisibleForTesting
    /*package*/ JsonAdapterHelper(String fsJsonAdapterFactoryClass) {
        this.fsJsonAdapterFactoryClass = fsJsonAdapterFactoryClass;
    }

    public Gson getNew() {
        if (isNullOrEmpty(fsJsonAdapterFactoryClass)) {
            return new Gson();
        }
        try {
            Class c = Class.forName(fsJsonAdapterFactoryClass);
            if (!FSJsonAdapterFactory.class.isAssignableFrom(c)) {
                return new Gson();
            }
            Gson ret = ((FSJsonAdapterFactory) c.newInstance()).create();
            return ret == null ? new Gson() : ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Gson();
    }
}
