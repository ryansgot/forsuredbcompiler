package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.api.adapter.FSSerializer;
import com.fsryan.forsuredb.api.adapter.FSSerializerFactoryPluginHelper;
import com.fsryan.forsuredb.api.sqlgeneration.DBMSIntegrator;
import com.fsryan.forsuredb.api.sqlgeneration.Sql;

public abstract class BaseDocStoreGetter<T> extends BaseGetter implements FSDocStoreGetApi<T> {

    private static FSSerializer serializer = new FSSerializerFactoryPluginHelper().getNew().create();

    private final Class<T> baseCls;

    public BaseDocStoreGetter(String tableName, Class<T> baseCls) {
        this(Sql.generator(), tableName, baseCls);
    }

    BaseDocStoreGetter(DBMSIntegrator sqlGenerator, String tableName, Class<T> baseCls) {
        super(sqlGenerator, tableName);
        this.baseCls = baseCls;
    }

    @Override
    public <C extends T> C getAs(Class<C> cls, Retriever retriever) {
        try {
            return (C) (serializer.storeAsBlob()
                    ? serializer.fromStorage(cls, blobDoc(retriever))
                    : serializer.fromStorage(cls, doc(retriever)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T get(Retriever retriever) {
        return getAs(getJavaClass(retriever), retriever);
    }

    @Override
    public T getAsBaseType(Retriever retriever) {
        return getAs(baseCls, retriever);
    }

    @Override
    public String doc(Retriever retriever) {
        return retrieveString(retriever, "doc");
    }

    @Override
    public byte[] blobDoc(Retriever retriever) {
        throwIfNullRetriever(retriever);
        return retriever.getBlob(disambiguateColumn("blob_doc"));
    }

    @Override
    public String className(Retriever retriever) {
        return retrieveString(retriever, "class_name");
    }

    @Override
    public <C extends T> Class<C> getJavaClass(Retriever retriever) {
        final String className = className(retriever);
        try {
            return (Class<C>) Class.forName(className).asSubclass(baseCls);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
