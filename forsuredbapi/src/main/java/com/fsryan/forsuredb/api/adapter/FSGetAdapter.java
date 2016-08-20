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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * <p>
 *     Able to create an instance of any {@link FSGetApi} extension.
 * </p>
 * @author Ryan Scott
 */
public class FSGetAdapter {

    /*package*/ static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // does not prefix the column at all
    private static final Map<Class<? extends FSGetApi>, RetrieveHandler> handlerMap = new HashMap<>();

    // This prefixes each column with tableName + "_"
    private static final Map<Class<? extends FSGetApi>, RetrieveHandler> unambiguousHandlerMap = new HashMap<>();

    /*package*/ static final Map<Type, Method> methodMap = new HashMap<>();
    static {
        try {
            methodMap.put(BigDecimal.class, Retriever.class.getDeclaredMethod("getString", String.class));
            methodMap.put(boolean.class, Retriever.class.getDeclaredMethod("getInt", String.class));
            methodMap.put(byte[].class, Retriever.class.getDeclaredMethod("getBlob", String.class));
            methodMap.put(double.class, Retriever.class.getDeclaredMethod("getDouble", String.class));
            methodMap.put(int.class, Retriever.class.getDeclaredMethod("getInt", String.class));
            methodMap.put(long.class, Retriever.class.getDeclaredMethod("getLong", String.class));
            methodMap.put(String.class, Retriever.class.getDeclaredMethod("getString", String.class));
            methodMap.put(Date.class, Retriever.class.getDeclaredMethod("getString", String.class));
        } catch (NoSuchMethodException nsme) {
            nsme.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * <p>
     *     Creates an instance of the table api class passed in that references columns by
     *     their name within the scope of the table. If you use this with a join, beware
     *     that if the joined tables have the same name, then you may not get the result
     *     you want.
     * </p>
     * @param resolver a {@link Resolver} instance that is capable of providing an {@link FSGetApi}
     * @param <G> The {@link FSGetApi} extension's type
     * @return an instance of the {@link FSGetApi} interface class passed in
     */
    public static <G extends FSGetApi> G create(Resolver<?, ?, G, ?, ?, ?> resolver) {
        Class<G> tableApi = resolver.getApiClass();
        GetApiValidator.validateClass(tableApi);
        G proxyInstance = (G) Proxy.newProxyInstance(tableApi.getClassLoader(), InterfaceHelper.getInterfaces(tableApi), getHandlerFor(resolver));
        return proxyInstance;
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
    public static <G extends FSGetApi> G createUnambiguous(Resolver<?, ?, G, ?, ?, ?> resolver) {
        GetApiValidator.validateClass(resolver.getApiClass());
        // TODO: determine whether you should gather the arguments for the Handler or whether the handler should know about resolvers.
        return (G) Proxy.newProxyInstance(resolver.getClass().getClassLoader(), new Class<?>[] {resolver.getApiClass()}, getUnambiguousHandlerFor(resolver));
    }

    private static <G extends FSGetApi> RetrieveHandler getHandlerFor(Resolver<?, ?, G, ?, ?, ?> resolver) {
        RetrieveHandler h = handlerMap.get(resolver.getApiClass());
        if (h == null) {
            h = RetrieveHandler.getFor(resolver.getApiClass(), resolver.tableName(), resolver.columnNameToMethodNameBiMap().inverse(), false);
            handlerMap.put(resolver.getApiClass(), h);
        }
        return h;
    }

    private static <G extends FSGetApi> RetrieveHandler getUnambiguousHandlerFor(Resolver<?, ?, G, ?, ?, ?> resolver) {
        RetrieveHandler h = unambiguousHandlerMap.get(resolver.getApiClass());
        if (h == null) {
            h = RetrieveHandler.getFor(resolver.getApiClass(), resolver.tableName(), resolver.columnNameToMethodNameBiMap().inverse(), true);
            unambiguousHandlerMap.put(resolver.getApiClass(), h);
        }
        return h;
    }
}
