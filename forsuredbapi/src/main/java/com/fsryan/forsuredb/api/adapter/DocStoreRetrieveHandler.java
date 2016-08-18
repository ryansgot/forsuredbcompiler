package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.api.Retriever;

import java.lang.reflect.Method;
import java.util.Map;

public class DocStoreRetrieveHandler<T> extends RetrieveHandler {

    private final Class<T> baseClass;

    public DocStoreRetrieveHandler(Class<T> baseClass, Class<? extends FSGetApi> tableApi, String tableName, Map<String, String> methodNameToColumnNameMap, boolean isUnambiguous) {
        super(tableApi, tableName, methodNameToColumnNameMap, isUnambiguous);
        this.baseClass = baseClass;
    }

    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        switch (m.getName()) {
            case "doc":
                // TODO
                return "";
            case "className":
                // TODO
                return callRetrieverMethod((Retriever) args[0], "class_name", String.class);
            case "get":
                // TODO: deserialize doc as type T
                return null;
            case "getAs":
                // TODO: deserialize doc as type C
                return null;
        }
        return super.invoke(proxy, m, args);
    }
}
