package com.fsryan.forsuredb.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.google.common.base.Strings.isNullOrEmpty;

public abstract class PluginHelper<I> {

    private final String className;
    private final Class<I> interfaceClass;

    public PluginHelper(Class<I> interfaceClass, String className) {
        this.interfaceClass = interfaceClass;
        this.className = className;
    }

    public I getNew() {
        if (isNullOrEmpty(className)) {
            return defaultImplementation();
        }
        try {
            Class c = Class.forName(className);
            if (!interfaceClass.isAssignableFrom(c)) {
                return defaultImplementation();
            }
            I ret = (I) c.getConstructor().newInstance();
            return ret == null ? defaultImplementation() : ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultImplementation();
    }

    protected abstract I defaultImplementation();

    protected static String getImplementationClassName(Class c) {
        InputStream is = c.getClassLoader().getResourceAsStream("META-INF/services/" + c.getName());
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
