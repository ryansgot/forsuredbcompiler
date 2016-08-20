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
package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.api.*;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        Class<S> setApiClass = resolver.setApiClass();
        S proxyInstance = (S) Proxy.newProxyInstance(setApiClass.getClassLoader(),
                                          InterfaceHelper.getInterfaces(setApiClass),
                                          new SaveHandler(queryable, selection, emptyRecord, getColumnTypeMapFor(resolver)));
        return proxyInstance;
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
}
