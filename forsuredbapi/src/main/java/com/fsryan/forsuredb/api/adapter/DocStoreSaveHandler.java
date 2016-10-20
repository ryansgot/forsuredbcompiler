package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.api.FSQueryable;
import com.fsryan.forsuredb.api.FSSelection;
import com.fsryan.forsuredb.api.RecordContainer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

/*package*/ class DocStoreSaveHandler<U, R extends RecordContainer> extends SaveHandler<U, R> {

    private static FSSerializer serializer;
    private static final ColumnDescriptor DOC_COLUMN_DESCRIPTOR = new ColumnDescriptor("doc", String.class);
    private static final ColumnDescriptor DOC_BLOB_COLUMN_DESCRIPTOR = new ColumnDescriptor("blob_doc", byte[].class);
    private static final ColumnDescriptor CLASS_NAME_COLUMN_DESCRIPTOR = new ColumnDescriptor("class_name", String.class);

    protected DocStoreSaveHandler(FSQueryable<U, R> queryable, FSSelection selection, R recordContainer, Map<Method, ColumnDescriptor> columnTypeMap) {
        super(queryable, selection, recordContainer, columnTypeMap);
        if (serializer == null) {
            serializer = new FSSerializerFactoryPluginHelper().getNew().create();
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("object".equals(method.getName())) {
            updateDocProperties(args[0]);
            return proxy;
        }
        return super.invoke(proxy, method, args);
    }

    @Override
    protected void handleTypeMiss(String columnName, Type type, Object val) {
        // TODO: this assumes the underlying type was a string type. This is not a safe assumption.
        recordContainer.put(columnName, serializer.createStringDoc(type, val));
    }

    private void updateDocProperties(Object obj) {
        for (Map.Entry<Method, ColumnDescriptor> methodToColumnDescriptorEntry : columnTypeMap.entrySet()) {
            String methodName = methodToColumnDescriptorEntry.getKey().getName();
            if (methodName.equals("id") || methodName.equals("deleted")) {
                continue;
            }
            Object val = null;

            Field f = getFieldForClassOrSuperclass(methodName, obj);
            try {
                f.setAccessible(true);
                val = f.get(obj);
                if (val == null) {
                    continue;
                }
                performSet(methodToColumnDescriptorEntry.getValue(), val);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        performSet(CLASS_NAME_COLUMN_DESCRIPTOR, obj.getClass().getName());

        if (serializer.storeAsBlob()) {
            performSet(DOC_BLOB_COLUMN_DESCRIPTOR, serializer.createBlobDoc(obj.getClass(), obj));
        } else {
            performSet(DOC_COLUMN_DESCRIPTOR, serializer.createStringDoc(obj.getClass(), obj));
        }
    }

    private Field getFieldForClassOrSuperclass(String fieldName, Object obj) {
        Class<?> cls = obj.getClass();
        Field ret = null;
        while (cls != Object.class && ret == null) {
            try {
                ret = cls.getDeclaredField(fieldName);
            } catch (NoSuchFieldException nsfe) {
                cls = cls.getSuperclass();
            }
        }
        return ret;
    }
}
