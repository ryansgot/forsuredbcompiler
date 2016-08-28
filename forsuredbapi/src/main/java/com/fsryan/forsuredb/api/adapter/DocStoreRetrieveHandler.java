package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.api.Retriever;
import com.google.gson.Gson;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

/*package*/ class DocStoreRetrieveHandler<T> extends RetrieveHandler {

    private static Gson gson;

    private final Class<T> baseClass;

    public DocStoreRetrieveHandler(Class<T> baseClass, Class<? extends FSGetApi> tableApi, String tableName, Map<String, String> methodNameToColumnNameMap) {
        super(tableApi, tableName, methodNameToColumnNameMap);
        this.baseClass = baseClass;
        if (gson == null) {
            gson = new JsonAdapterHelper().getNew();
        }
    }

    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        // TODO: don't do aliasing of columns in the stupid way it's currently done. just use the unambiguous name
        switch (m.getName()) {
            case "doc":
                return getDoc((Retriever) args[0]);
            case "className":
                return callRetrieverMethod((Retriever) args[0], tableName + "_class_name", String.class);
            case "getClass":
                String className = (String) callRetrieverMethod((Retriever) args[0], tableName + "_class_name", String.class);
                try {
                    return Class.forName(className);
                } catch(ClassNotFoundException cnfe) {
                    cnfe.printStackTrace();
                }
                return null;
            case "get":
                String doc = getDoc((Retriever) args[0]);
                try {
                    return gson.fromJson(doc, baseClass);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            case "getAs":
                doc = getDoc((Retriever) args[1]);
                try {
                    return gson.fromJson(doc, (Type) args[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
        }
        return super.invoke(proxy, m, args);
    }

    private String getDoc(Retriever retriever) throws IllegalAccessException, InvocationTargetException {
        return (String) callRetrieverMethod(retriever, tableName + "_doc", String.class);
    }
}
