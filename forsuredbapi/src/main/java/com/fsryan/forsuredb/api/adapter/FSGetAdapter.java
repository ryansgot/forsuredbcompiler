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

import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.api.Resolver;
import com.fsryan.forsuredb.api.Retriever;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * <p>
 *     Able to create an instance of any {@link FSGetApi} extension.
 * </p>
 * @author Ryan Scott
 */
public class FSGetAdapter {

    private static final Map<Class<? extends FSGetApi>, RetrieveHandler> unambiguousHandlerMap = new HashMap<>();

    /*package*/ static final Map<Type, Method> methodMap = new HashMap<>();
    static {
        try {
            methodMap.put(BigDecimal.class, Retriever.class.getDeclaredMethod("getString", String.class));
            methodMap.put(boolean.class, Retriever.class.getDeclaredMethod("getInt", String.class));
            methodMap.put(Boolean.class, Retriever.class.getDeclaredMethod("getInt", String.class));
            methodMap.put(byte[].class, Retriever.class.getDeclaredMethod("getBlob", String.class));
            methodMap.put(double.class, Retriever.class.getDeclaredMethod("getDouble", String.class));
            methodMap.put(Double.class, Retriever.class.getDeclaredMethod("getDouble", String.class));
            methodMap.put(int.class, Retriever.class.getDeclaredMethod("getInt", String.class));
            methodMap.put(Integer.class, Retriever.class.getDeclaredMethod("getInt", String.class));
            methodMap.put(long.class, Retriever.class.getDeclaredMethod("getLong", String.class));
            methodMap.put(Long.class, Retriever.class.getDeclaredMethod("getLong", String.class));
            methodMap.put(String.class, Retriever.class.getDeclaredMethod("getString", String.class));
            methodMap.put(Date.class, Retriever.class.getDeclaredMethod("getString", String.class));
        } catch (NoSuchMethodException nsme) {
            nsme.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * <p>
     *     Creates an instance of the table api class passed in that references columns in a
     *     tableName_columnName format so that it can be unambiguous. This is helpful for joins
     *     because different tables may have the same column name
     * </p>
     * @param resolver a {@link Resolver} instance that is capable of providing an {@link FSGetApi}
     * @param <G> The {@link FSGetApi} extension's type
     * @return an instance of the {@link FSGetApi} interface class passed in
     */
    public static <G extends FSGetApi> G create(Resolver<?, ?, G, ?, ?, ?> resolver) {
        return (G) Proxy.newProxyInstance(resolver.getClass().getClassLoader(), new Class<?>[] {resolver.getApiClass()}, getHandlerFor(resolver));
    }

    private static <G extends FSGetApi> RetrieveHandler getHandlerFor(Resolver<?, ?, G, ?, ?, ?> resolver) {
        RetrieveHandler h = unambiguousHandlerMap.get(resolver.getApiClass());
        if (h == null) {
            h = RetrieveHandler.getFor(resolver.getApiClass(), resolver.tableName(), resolver.columnNameToMethodNameBiMap().inverse());
            unambiguousHandlerMap.put(resolver.getApiClass(), h);
        }
        return h;
    }
}
