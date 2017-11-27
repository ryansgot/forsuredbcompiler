package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.api.adapter.FSSerializer;
import com.fsryan.forsuredb.api.adapter.FSSerializerFactoryPluginHelper;
import com.fsryan.forsuredb.api.sqlgeneration.DBMSIntegrator;
import com.fsryan.forsuredb.api.sqlgeneration.Sql;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public abstract class BaseDocStoreSetter<U, T> extends BaseSetter<U> implements FSDocStoreSaveApi<U, T> {

    private static FSSerializer serializer = new FSSerializerFactoryPluginHelper().getNew().create();

    protected final DBMSIntegrator sqlGenerator;

    public BaseDocStoreSetter(FSQueryable<U, RecordContainer> queryable,
                              FSSelection selection,
                              List<FSOrdering> orderings,
                              RecordContainer recordContainer) {
        this(Sql.generator(), queryable, selection, orderings, recordContainer);
    }

    public BaseDocStoreSetter(DBMSIntegrator sqlGenerator,
                              FSQueryable<U, RecordContainer> queryable,
                              FSSelection selection,
                              List<FSOrdering> orderings,
                              RecordContainer recordContainer) {
        super(queryable, selection, orderings, recordContainer);
        this.sqlGenerator = sqlGenerator;
    }

    @Override
    public final FSDocStoreSaveApi<U, T> object(T obj) {
        enrichRecordContainerFromPropertiesOf(obj);
        if (serializer.storeAsBlob()) {
            recordContainer.put("blob_doc", serializer.createBlobDoc(obj.getClass(), obj));
        } else {
            recordContainer.put("doc", serializer.createStringDoc(obj.getClass(), obj));
        }
        return this;
    }

    protected abstract void enrichRecordContainerFromPropertiesOf(T obj);

    protected void performPropertyEnrichment(String columnName, Object obj) {
        Type type = obj.getClass();
        if (type.equals(String.class)) {
            recordContainer.put(columnName, (String) obj);
        } else if (type.equals(int.class) || type.equals(Integer.class)) {
            recordContainer.put(columnName, (int) obj);
        } else if (type.equals(long.class) || type.equals(Long.class)) {
            recordContainer.put(columnName, (long) obj);
        } else if (type.equals(double.class) || type.equals(Double.class)) {
            recordContainer.put(columnName, (double) obj);
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            recordContainer.put(columnName, (boolean) obj ? 1 : 0);
        } else if (type.equals(BigDecimal.class) || type.equals(BigInteger.class)) {
            recordContainer.put(columnName, obj.toString());
        } else if (type.equals(float.class) || type.equals(Float.class)) {
            recordContainer.put(columnName, (float) obj);
        } else if (type.equals(Date.class)) {
            recordContainer.put(columnName, sqlGenerator.formatDate((Date) obj));
        } else if (type.equals(byte[].class)) {
            recordContainer.put(columnName, (byte[]) obj);
        } else {
            throw new IllegalArgumentException("Cannot handle object of type: " + obj.getClass());
        }
    }
}
