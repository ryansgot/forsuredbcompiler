package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.api.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/*package*/ class SaveHandler<U, R extends RecordContainer> implements InvocationHandler {

    private static final ColumnDescriptor DOC_COLUMN_DESCRIPTOR = new ColumnDescriptor("doc", String.class);

    private final FSQueryable<U, R> queryable;
    private final FSSelection selection;
    private final R recordContainer;
    protected final Map<Method, ColumnDescriptor> columnTypeMap;

    protected SaveHandler(FSQueryable<U, R> queryable, FSSelection selection, R recordContainer, Map<Method, ColumnDescriptor> columnTypeMap) {
        this.queryable = queryable;
        this.selection = selection;
        this.recordContainer = recordContainer;
        this.columnTypeMap = columnTypeMap;
    }

//    public static <U, R extends RecordContainer, S extends FSSaveApi<U>> SaveHandler<U, R> getFor(Class<S> saveApiClass, FSQueryable<U, R> queryable, FSSelection selection, R emptyRecord, Map<Method, ColumnDescriptor> columnTypeMap) {
//        return FSDocStoreSaveApi.class.isAssignableFrom(saveApiClass) ? new DocStoreSaveHandler<>(queryable, selection, emptyRecord, columnTypeMap)
//                : new RelationalSaveHandler<>(queryable, selection, emptyRecord, columnTypeMap);
//    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }

        // The following methods are terminal
        switch(method.getName()) {
            case "save":
                return performSave();
            case "softDelete":
                recordContainer.clear();
                recordContainer.put("deleted", 1);
                return performUpdate();
            case "hardDelete":
                recordContainer.clear();
                return queryable.delete(selection);
            case "object":
                updateDocProperties(args[0]);
                return proxy;
        }

        performSet(columnTypeMap.get(method), args[0]);
        return proxy;
    }

    private void updateDocProperties(Object obj) {
        for (Method m : columnTypeMap.keySet()) {
            System.out.println("method in key set: " + m.getName() + "; obj methods: " + Arrays.toString(obj.getClass().getDeclaredMethods()));
        }
        // TODO: serialize the object
        performSet(DOC_COLUMN_DESCRIPTOR, "");
    }

    protected void performSet(ColumnDescriptor columnDescriptor, Object arg) {
        Type type = columnDescriptor.getType();
        if (type.equals(byte[].class)) {
            recordContainer.put(columnDescriptor.getColumnName(), (byte[]) arg);
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            recordContainer.put(columnDescriptor.getColumnName(), (Boolean) arg ? 1 : 0);
        } else if (type.equals(Date.class)) {
            recordContainer.put(columnDescriptor.getColumnName(), FSGetAdapter.DATETIME_FORMAT.format((Date) arg));
        } else {
            recordContainer.put(columnDescriptor.getColumnName(), arg.toString());
        }
    }

    private SaveResult<U> performSave() {
        if (selection == null) {
            return performInsert();
        }
        return performUpsert();
    }

    private SaveResult<U> performUpsert() {
        Retriever cursor = queryable.query(null, selection, null);
        try {
            if (cursor == null || cursor.getCount() < 1) {
                return performInsert();
            }
            return performUpdate();
        } catch (Exception e) {
            return SaveResultFactory.create(null, 0, e);
        } finally {
            recordContainer.clear();
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private SaveResult<U> performInsert() {
        try {
            final U inserted = queryable.insert(recordContainer);
            return SaveResultFactory.create(inserted, inserted == null ? 0 : 1, null);
        } catch (Exception e) {
            return SaveResultFactory.create(null, 0, e);
        } finally {
            recordContainer.clear();
        }
    }

    private SaveResult<U> performUpdate() {
        int rowsAffected = queryable.update(recordContainer, selection);
        return SaveResultFactory.create(null, rowsAffected, null);
    }
}
