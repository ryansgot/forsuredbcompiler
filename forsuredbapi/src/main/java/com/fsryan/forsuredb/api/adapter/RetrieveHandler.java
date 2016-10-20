package com.fsryan.forsuredb.api.adapter;

import com.fsryan.forsuredb.api.FSDocStoreGetApi;
import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.api.Retriever;
import com.fsryan.forsuredb.api.sqlgeneration.Sql;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;

/*package*/ abstract class RetrieveHandler implements InvocationHandler {

    private static final Map<String, Map<Method, String>> methodToColumnNameMapCache = new HashMap<>();

    protected final String tableName;
    private final Map<Method, String> methodToColumnNameMap;

    public RetrieveHandler(Class<? extends FSGetApi> tableApi, String tableName, Map<String, String> methodNameToColumnNameMap) {
        this.tableName = tableName;
        methodToColumnNameMap = getOrCreateMethodToColumnNameMap(tableName, tableApi, methodNameToColumnNameMap);
    }

    public static RetrieveHandler getFor(Class<? extends FSGetApi> tableApi, String tableName, Map<String, String> methodNameToColumnNameMap) {
        if (!FSDocStoreGetApi.class.isAssignableFrom(tableApi)) {
            return new RelationalRetrieveHandler(tableApi, tableName, methodNameToColumnNameMap);
        }
        Class baseClass = Object.class;
        try {
            baseClass = (Class) tableApi.getDeclaredField("BASE_CLASS").get(tableApi);
        } catch (Exception e) {
            System.out.println("could not get BASE_CLASS for : " + tableApi.getName());
            e.printStackTrace();
        }
        return new DocStoreRetrieveHandler<>(baseClass, tableApi, tableName, methodNameToColumnNameMap);
    }

    /**
     * <p>
     *     Generates a Proxy for the {@link FSGetApi} interface created by the client.
     * </p>
     * @param proxy The {@link FSGetApi} interface proxy
     * @param method not actually ever called, rather, it stores the meta-data associated with a
     *               call to one of the Cursor class methods. The {@link FSGetAdapter} pre-stores
     *               the appropriate retriever methods to call based upon the column's associated
     *               Java type
     * @param args The Retriever object on which one of the get methods will be called
     *
     * @return An object of the type guaranteed by the {@link FSGetApi} extension this proxy is implementing
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }
        return callRetrieverMethod((Retriever) args[0], methodToColumnNameMap.get(method), method.getGenericReturnType());
    }

    protected Object callRetrieverMethod(Retriever retriever, String column, Type type) throws InvocationTargetException, IllegalAccessException {
        // TODO: find out a better solution for translation methods
        final Method cursorMethod = FSGetAdapter.methodMap.get(type);
        if (type.equals(BigDecimal.class)) {
            return getBigDecimalFrom(cursorMethod, retriever, column);
        } else if (type.equals(boolean.class)) {
            final Object o = cursorMethod.invoke(retriever, column);
            return o != null && (Integer) o == 1;
        } else if (type.equals(Date.class)) {
            return getDateFrom(cursorMethod, retriever, column);
        }
        return cursorMethod.invoke(retriever, column);
    }

    private Date getDateFrom(Method cursorMethod, Retriever retriever, String column)
            throws InvocationTargetException, IllegalAccessException {
        final Object returned = cursorMethod.invoke(retriever, column);
        return returned == null ? null : Sql.generator().parseDate((String) returned);
    }

    private BigDecimal getBigDecimalFrom(Method retrieverMethod, Retriever retriever, String column)
            throws InvocationTargetException, IllegalAccessException {
        try {
            final Object returned = retrieverMethod.invoke(retriever, column);
            return returned == null ? null : new BigDecimal((String) returned);
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }
        return null;
    }

    private Map<Method, String> getOrCreateMethodToColumnNameMap(String tableName, Class<?> tableApi, Map<String, String> methodNameToColumnNameMap) {
        Map<Method, String> ret = methodToColumnNameMapCache.get(tableName);
        if (ret == null) {
            ret = createMethodToColumnNameMap(tableName, tableApi, methodNameToColumnNameMap);
            methodToColumnNameMapCache.put(tableName, ret);
        }
        return ret;
    }

    private Map<Method, String> createMethodToColumnNameMap(String tableName, Class<?> tableApi, Map<String, String> methodNameToColumnNameMap) {
        Map<Method, String> ret = new HashMap<>();
        for (Method m : tableApi.getDeclaredMethods()) {
            String columnName = methodNameToColumnNameMap.get(m.getName());
            if (isNullOrEmpty(columnName)) {
                continue;
            }
            ret.put(m, Sql.generator().unambiguousRetrievalColumn(tableName, columnName));
        }
        for (Class<?> superTableApi : tableApi.getInterfaces()) {
            ret.putAll(createMethodToColumnNameMap(tableName, superTableApi, methodNameToColumnNameMap));
        }
        return ret;
    }
}
