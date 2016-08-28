package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.api.FSQueryable;
import com.fsryan.forsuredb.api.FSSelection;
import com.fsryan.forsuredb.api.RecordContainer;
import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

/*package*/ class DocStoreSaveHandler<U, R extends RecordContainer> extends SaveHandler<U, R> {

    private static Gson gson;
    private static final ColumnDescriptor DOC_COLUMN_DESCRIPTOR = new ColumnDescriptor("doc", String.class);
    private static final ColumnDescriptor CLASS_NAME_COLUMN_DESCRIPTOR = new ColumnDescriptor("class_name", String.class);

    protected DocStoreSaveHandler(FSQueryable<U, R> queryable, FSSelection selection, R recordContainer, Map<Method, ColumnDescriptor> columnTypeMap) {
        super(queryable, selection, recordContainer, columnTypeMap);
        if (gson == null) {
            gson = new JsonAdapterHelper().getNew();
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
        recordContainer.put(columnName, gson.toJson(val, type));
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
        performSet(DOC_COLUMN_DESCRIPTOR, gson.toJson(obj, obj.getClass()));
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
