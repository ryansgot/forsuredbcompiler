package com.fsryan.forsuredb.api.adapter;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.google.common.base.Strings.isNullOrEmpty;

/*package*/ class JsonAdapterHelper {

    private static final String FACTORY_CLASS = createFSJsonAdapterFactoryClass();

    private final String fsJsonAdapterFactoryClass;

    public JsonAdapterHelper() {
        this(FACTORY_CLASS);
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

    private static String createFSJsonAdapterFactoryClass() {
        InputStream is = JsonAdapterHelper.class.getClassLoader()
                .getResourceAsStream("META-INF/services/" + FSJsonAdapterFactory.class.getName());
        if (is == null) {
            return null;
        }

        String ret = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            ret = reader.readLine();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException ioe1) {}
            try {
                is.close();
            } catch (IOException ioe1) {}
        }
        return ret;
    }
}
