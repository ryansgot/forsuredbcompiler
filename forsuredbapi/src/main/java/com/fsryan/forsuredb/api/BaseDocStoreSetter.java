package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.api.adapter.FSSerializer;
import com.fsryan.forsuredb.api.adapter.FSSerializerFactoryPluginHelper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public abstract class BaseDocStoreSetter<U, R extends RecordContainer, T, S extends BaseDocStoreSetter<U, R, T, S>> extends BaseSetter<U, R, S> {

    private static FSSerializer serializer = new FSSerializerFactoryPluginHelper().getNew().create();

    public BaseDocStoreSetter(FSQueryable<U, R> queryable,
                              FSSelection selection,
                              List<FSOrdering> orderings,
                              R recordContainer) {
        super(queryable, selection, orderings, recordContainer);
    }

    // intended for use in testing
    protected BaseDocStoreSetter(DateFormat dateFormat,
                       FSQueryable<U, R> queryable,
                       FSSelection selection,
                       List<FSOrdering> orderings,
                       R recordContainer) {
        super(dateFormat, queryable, selection, orderings, recordContainer);
    }


    /**
     * <p>Set the object to persist. The fully-qualified class name of the
     * object as well as a serialized version of the object will be persisted
     * when you call {@link #save()}
     * @param obj The object to persist
     * @return this same {@link BaseDocStoreSetter} object
     */
    public S obj(T obj) {
        enrichRecordContainerFromPropertiesOf(obj);
        recordContainer.put("class_name", obj.getClass().getName());
        if (serializer.storeAsBlob(obj.getClass())) {
            recordContainer.put("blob_doc", serializer.createBlobDoc(obj.getClass(), obj));
        } else {
            recordContainer.put("doc", serializer.createStringDoc(obj.getClass(), obj));
        }
        return (S) this;
    }

    protected abstract void enrichRecordContainerFromPropertiesOf(T obj);

    protected void performPropertyEnrichment(String columnName, Object obj) {
        if (obj == null || columnName == null || columnName.isEmpty()) {
            // TODO: check what to do when this occurs
            return;
        }

        Class<?> type = obj.getClass();
        if (type.equals(String.class)) {
            recordContainer.put(columnName, (String) obj);
        } else if (type.equals(Integer.class)) {
            recordContainer.put(columnName, (int) obj);
        } else if (type.equals(Long.class)) {
            recordContainer.put(columnName, (long) obj);
        } else if (type.equals(Double.class)) {
            recordContainer.put(columnName, (double) obj);
        } else if (type.equals(Boolean.class)) {
            recordContainer.put(columnName, (boolean) obj ? 1 : 0);
        } else if (type.equals(BigInteger.class) || type.equals(BigDecimal.class)) {
            recordContainer.put(columnName, obj.toString());
        } else if (type.equals(Float.class)) {
            recordContainer.put(columnName, (float) obj);
        } else if (type.equals(Date.class)) {
            recordContainer.put(columnName, dateFormat.format((Date) obj));
        } else if (type.equals(byte[].class)) {
            recordContainer.put(columnName, (byte[]) obj);
        } else {
            throw new IllegalArgumentException("Cannot handle object of type: " + obj.getClass());
        }
    }
}
