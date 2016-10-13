package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.api.Retriever;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

/*package*/ class DocStoreRetrieveHandler<T> extends RetrieveHandler {

    private static FSSerializer serializer;

    private final Class<T> baseClass;

    public DocStoreRetrieveHandler(Class<T> baseClass, Class<? extends FSGetApi> tableApi, String tableName, Map<String, String> methodNameToColumnNameMap) {
        super(tableApi, tableName, methodNameToColumnNameMap);
        this.baseClass = baseClass;
        if (serializer == null) {
            serializer = new FSSerializerFactoryHelper().getNew();
        }
    }

    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        // TODO: don't do aliasing of columns in the stupid way it's currently done. just use the unambiguous name
        switch (m.getName()) {
            case "doc":
                return getStringDoc((Retriever) args[0]);
            case "className":
                return callRetrieverMethod((Retriever) args[0], tableName + "_class_name", String.class);
            case "getClass":
                return getClassOfObject((Retriever) args[0]);
            // TODO: this logic makes the assumption that the same serailizer in use when storing is the one used when retrieving
            case "get":
                try {
                    return serializer.storeAsBlob()
                            ? serializer.fromStorage(getClassOfObject((Retriever) args[0]), getBlobDoc((Retriever) args[0]))
                            : serializer.fromStorage(getClassOfObject((Retriever) args[0]), getStringDoc((Retriever) args[0]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            case "getAs":
                try {
                    return serializer.storeAsBlob()
                            ? serializer.fromStorage((Type) args[0], getBlobDoc((Retriever) args[1]))
                            : serializer.fromStorage((Type) args[0], getStringDoc((Retriever) args[1]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            case "getAsBaseType":
                try {
                    return serializer.storeAsBlob()
                            ? serializer.fromStorage(baseClass, getBlobDoc((Retriever) args[0]))
                            : serializer.fromStorage(baseClass, getStringDoc((Retriever) args[0]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
        }
        return super.invoke(proxy, m, args);
    }

    private <C extends T> Class<C> getClassOfObject(Retriever retriever) throws IllegalAccessException, InvocationTargetException {
        String className = (String) callRetrieverMethod(retriever, tableName + "_class_name", String.class);
        try {
            return (Class<C>) Class.forName(className).asSubclass(baseClass);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getStringDoc(Retriever retriever) throws IllegalAccessException, InvocationTargetException {
        return (String) callRetrieverMethod(retriever, tableName + "_doc", String.class);
    }

    private byte[] getBlobDoc(Retriever retriever) throws IllegalAccessException, InvocationTargetException {
        return (byte[]) callRetrieverMethod(retriever, tableName + "_blob_doc", byte[].class);
    }
}
