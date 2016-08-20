package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.api.Retriever;
import com.google.gson.Gson;

import java.lang.reflect.Method;
import java.util.Map;

public class DocStoreRetrieveHandler<T> extends RetrieveHandler {

    private static final Gson gson = new Gson();

    private final Class<T> baseClass;

    public DocStoreRetrieveHandler(Class<T> baseClass, Class<? extends FSGetApi> tableApi, String tableName, Map<String, String> methodNameToColumnNameMap, boolean isUnambiguous) {
        super(tableApi, tableName, methodNameToColumnNameMap, isUnambiguous);
        this.baseClass = baseClass;
    }

    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        switch (m.getName()) {
            case "doc":
                return callRetrieverMethod((Retriever) args[0], tableName + "_doc", String.class);
            case "className":
                return callRetrieverMethod((Retriever) args[0], tableName + "_class_name", String.class);
            case "get":
                String doc = (String) callRetrieverMethod((Retriever) args[0], tableName + "_doc", String.class);
                try {
                    return gson.fromJson(doc, baseClass);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            case "getAs":
                doc = (String) callRetrieverMethod((Retriever) args[0], tableName + "_doc", String.class);
                try {
                    return gson.fromJson(doc, (Class) args[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
        }
        return super.invoke(proxy, m, args);
    }
}
