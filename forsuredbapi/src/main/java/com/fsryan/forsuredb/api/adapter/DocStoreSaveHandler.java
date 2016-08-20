package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.api.FSQueryable;
import com.fsryan.forsuredb.api.FSSelection;
import com.fsryan.forsuredb.api.RecordContainer;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/*package*/ class DocStoreSaveHandler<U, R extends RecordContainer> extends SaveHandler<U, R> {

    // TODO: if using BSON, then change this to byte[].class
    private static final ColumnDescriptor DOC_COLUMN_DESCRIPTOR = new ColumnDescriptor("doc", String.class);

    protected DocStoreSaveHandler(FSQueryable<U, R> queryable, FSSelection selection, R recordContainer, Map<Method, ColumnDescriptor> columnTypeMap) {
        super(queryable, selection, recordContainer, columnTypeMap);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("object".equals(method.getName())) {
            updateAllProperties(args[0]);
            return proxy;
        }
        return super.invoke(proxy, method, args);
    }

    private void updateAllProperties(Object obj) {
        for (Method m : columnTypeMap.keySet()) {
            System.out.println("method in key set: " + m.getName() + "; obj methods: " + Arrays.toString(obj.getClass().getDeclaredMethods()));
        }
        // TODO: serialize the object
        performSet(DOC_COLUMN_DESCRIPTOR, "");
    }
}
