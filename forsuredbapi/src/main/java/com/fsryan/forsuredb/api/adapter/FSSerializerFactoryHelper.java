package com.fsryan.forsuredb.api.adapter;

import com.google.common.annotations.VisibleForTesting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.google.common.base.Strings.isNullOrEmpty;

/*package*/ class FSSerializerFactoryHelper {

    private static final String FACTORY_CLASS = createFSSerializerFactoryClass();

    private final String fsSerializerFactoryClass;

    public FSSerializerFactoryHelper() {
        this(FACTORY_CLASS);
    }

    @VisibleForTesting
    /*package*/ FSSerializerFactoryHelper(String fsSerializerFactoryClass) {
        this.fsSerializerFactoryClass = fsSerializerFactoryClass;
    }

    public FSSerializer getNew() {
        if (isNullOrEmpty(fsSerializerFactoryClass)) {
            return new FSGsonSerializer();
        }
        try {
            Class c = Class.forName(fsSerializerFactoryClass);
            if (!FSSerializerFactory.class.isAssignableFrom(c)) {
                return new FSGsonSerializer();
            }
            FSSerializer ret = ((FSSerializerFactory) c.newInstance()).create();
            return ret == null ? new FSGsonSerializer() : ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new FSGsonSerializer();
    }

    private static String createFSSerializerFactoryClass() {
        InputStream is = FSSerializerFactoryHelper.class.getClassLoader()
                .getResourceAsStream("META-INF/services/" + FSSerializerFactory.class.getName());
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
