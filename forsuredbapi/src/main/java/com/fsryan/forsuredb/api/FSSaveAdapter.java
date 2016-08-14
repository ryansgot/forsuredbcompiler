/*
   forsuredbcompiler, an annotation processor and code generator for the forsuredb project

   Copyright 2015 Ryan Scott

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.fsryan.forsuredb.api;

import com.google.common.collect.BiMap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *     Adapter capable of creating an implementation of any {@link FSSaveApi} extension
 * </p>
 * @author Ryan Scott
 */
public class FSSaveAdapter {

    /**
     * <p>
     *     Caches the column information so that reflection on each interface must happen only once
     * </p>
     */
    private static final Map<Class<? extends FSSaveApi>, Map<Method, ColumnDescriptor>> API_TO_COLUMNS_MAP = new HashMap<>();

    /**
     * <p>
     *     Creates a fluent API capable of performing insert, update, and delete operations into the database.
     *     There are three ways to terminate the method call chain:
     * </p>
     * <ol>
     *     <li>{@link FSSaveApi#save()}</li>
     *     <li>{@link FSSaveApi#softDelete()}</li>
     *     <li>{@link FSSaveApi#hardDelete()}</li>
     * </ol>
     * @param queryable An {@link FSQueryable} that can be used to insert/update/delete records in the database
     * @param selection an {@link FSSelection} that can be used to narrow down the records you're talking about
     * @param emptyRecord an {@link RecordContainer} extension that represents the record prior to being
     *                    inserted/updated or a delete operation is run. It does not matter whether this record
     *                    is empty or not because it will be emptied for you.
     * @param resolver The {@link FSSaveApi FSSaveApi} class for which you would like an object
     * @param <S> The type of an {@link FSSaveApi} which was generated at compile time from an
     * {@link FSGetApi} definition
     * @param <U> The class by which records are located
     * @param <R> An extension of {@link RecordContainer} that holds records prior to their insertion/update
     *           in the database
     * @return An implementation of the {@link FSSaveApi} class object passed in
     * @see FSSaveApi
     */
    public static <S extends FSSaveApi<U>, U, R extends RecordContainer> S create(FSQueryable<U, R> queryable,
                                                                                  FSSelection selection,
                                                                                  R emptyRecord,
                                                                                  Resolver<U, R, ?, S, ?, ?> resolver) {
        return (S) Proxy.newProxyInstance(resolver.setApiClass().getClassLoader(),
                                          new Class<?>[]{resolver.setApiClass()},
                                          new Handler(queryable, selection, emptyRecord, getColumnTypeMapFor(resolver)));
    }

    // lazily create the column type maps for each api so that they are not created each time a new handler is created
    private static <S extends FSSaveApi<U>, U, R extends RecordContainer> Map<Method, ColumnDescriptor> getColumnTypeMapFor(Resolver<U, R, ?, S, ?, ?> resolver) {
        Class<S> setApi = resolver.setApiClass();
        Map<Method, ColumnDescriptor> retMap = API_TO_COLUMNS_MAP.get(setApi);
        if (retMap == null) {
            retMap = createColumnTypeMapFor(resolver);
            API_TO_COLUMNS_MAP.put(setApi, retMap);
        }
        return retMap;
    }

    private static <S extends FSSaveApi<U>, U, R extends RecordContainer> Map<Method, ColumnDescriptor> createColumnTypeMapFor(Resolver<U, R, ?, S, ?, ?> resover) {
        Map<Method, ColumnDescriptor> retMap = new HashMap<>();
        // This means there can be no overloading of methods in Setter interfaces. That is fine because
        // Setter methods must take one and only one argument.
        Map<String, String> methodNameToColumnNameMap = resover.columnNameToMethodNameBiMap().inverse();
        if (methodNameToColumnNameMap == null) {
            return retMap;
        }

        for (Method m : resover.setApiClass().getDeclaredMethods()) {
            String columnName = methodNameToColumnNameMap.get(m.getName());
            if (columnName == null) {
                continue;
            }
            retMap.put(m, new ColumnDescriptor(columnName, m.getGenericParameterTypes()[0]));
        }
        return retMap;
    }

    private static class Handler<U, R extends RecordContainer> implements InvocationHandler {

        private final FSQueryable<U, R> queryable;
        private final FSSelection selection;
        private final R recordContainer;
        private final Map<Method, ColumnDescriptor> columnTypeMap;

        public Handler(FSQueryable<U, R> queryable, FSSelection selection, R recordContainer, Map<Method, ColumnDescriptor> columnTypeMap) {
            this.queryable = queryable;
            this.selection = selection;
            this.recordContainer = recordContainer;
            this.columnTypeMap = columnTypeMap;
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
                    return queryable.delete(selection);
            }

            performSet(columnTypeMap.get(method), args[0]);
            return proxy;
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
                return ResultFactory.create(null, 0, e);
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
                return ResultFactory.create(inserted, inserted == null ? 0 : 1, null);
            } catch (Exception e) {
                return ResultFactory.create(null, 0, e);
            } finally {
                recordContainer.clear();
            }
        }

        private SaveResult<U> performUpdate() {
            int rowsAffected = queryable.update(recordContainer, selection);
            return ResultFactory.create(null, rowsAffected, null);
        }

        private void performSet(ColumnDescriptor columnDescriptor, Object arg) {
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
    }

    private static class ResultFactory {
        public static <U> SaveResult<U> create(final U inserted, final int rowsAffected, final Exception e) {
            return new SaveResult<U>() {
                @Override
                public Exception exception() {
                    return e;
                }

                @Override
                public U inserted() {
                    return inserted;
                }

                @Override
                public int rowsAffected() {
                    return rowsAffected;
                }
            };
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class ColumnDescriptor {
        private final String columnName;
        private final Type type;
    }
}
