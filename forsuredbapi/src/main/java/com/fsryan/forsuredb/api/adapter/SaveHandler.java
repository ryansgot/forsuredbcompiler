package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.api.*;
import com.fsryan.forsuredb.api.sqlgeneration.Sql;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/*package*/ abstract class SaveHandler<U, R extends RecordContainer> implements InvocationHandler {

    private final FSQueryable<U, R> queryable;
    private final FSSelection selection;
    private final List<FSOrdering> orderings;
    protected final R recordContainer;
    protected final Map<Method, ColumnDescriptor> columnTypeMap;

    protected SaveHandler(
            FSQueryable<U, R> queryable,
            FSSelection selection,
            List<FSOrdering> orderings,
            R recordContainer,
            Map<Method, ColumnDescriptor> columnTypeMap
    ) {
        this.queryable = queryable;
        this.selection = selection;
        this.orderings = orderings == null ? Collections.<FSOrdering>emptyList() : orderings;
        this.recordContainer = recordContainer;
        this.columnTypeMap = columnTypeMap;
    }

    public static <U, R extends RecordContainer, S extends FSSaveApi<U>> SaveHandler<U, R> getFor(
            Class<S> saveApiClass,
            FSQueryable<U, R> queryable,
            FSSelection selection,
            List<FSOrdering> orderings,
            R emptyRecord, Map<Method,
            ColumnDescriptor> columnTypeMap
    ) {
        return FSDocStoreSaveApi.class.isAssignableFrom(saveApiClass)
                ? new DocStoreSaveHandler<>(queryable, selection, orderings, emptyRecord, columnTypeMap)
                : new RelationalSaveHandler<>(queryable, selection, orderings, emptyRecord, columnTypeMap);
    }

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
                return queryable.delete(selection, orderings);
        }

        performSet(columnTypeMap.get(method), args[0]);
        return proxy;
    }

    protected void performSet(ColumnDescriptor columnDescriptor, Object arg) {
        Type type = columnDescriptor.getType();
        String columnName = columnDescriptor.getColumnName();
        if (type.equals(String.class)) {
            recordContainer.put(columnName, (String) arg);
        } else if (type.equals(int.class) || type.equals(Integer.class)) {
            recordContainer.put(columnName, (int) arg);
        } else if (type.equals(long.class) || type.equals(Long.class)) {
            recordContainer.put(columnName, (long) arg);
        } else if (type.equals(double.class) || type.equals(Double.class)) {
            recordContainer.put(columnName, (double) arg);
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            recordContainer.put(columnName, (Boolean) arg ? 1 : 0);
        } else if (type.equals(BigDecimal.class) || type.equals(BigInteger.class)) {
            recordContainer.put(columnName, arg.toString());
        } else if (type.equals(Date.class)) {
            recordContainer.put(columnName, Sql.generator().formatDate((Date) arg));
        } else if (type.equals(byte[].class)) {
            recordContainer.put(columnName, (byte[]) arg);
        } else {
            handleTypeMiss(columnName, type, arg);
        }
    }

    /**
     * <p>
     *     If you override this method, you <i>SHOULD NOT</i> call the super class method
     * </p>
     * @param columnName the name of the column to set with value arg
     * @param type the type of the column
     * @param val the value to set
     */
    protected void handleTypeMiss(String columnName, Type type, Object val) {
        recordContainer.put(columnName, val.toString());
    }

    private SaveResult<U> performSave() {
        if (selection == null) {
            return performInsert();
        }
        return performUpsert();
    }

    private SaveResult<U> performUpsert() {
        Retriever retriever = queryable.query(null, selection, null);
        try {
            if (retriever == null || retriever.getCount() < 1) {
                return performInsert();
            }
            return performUpdate();
        } catch (Exception e) {
            return SaveResultFactory.create(null, 0, e);
        } finally {
            recordContainer.clear();
            if (retriever != null) {
                retriever.close();
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
        int rowsAffected = queryable.update(recordContainer, selection, orderings);
        return SaveResultFactory.create(null, rowsAffected, null);
    }
}
