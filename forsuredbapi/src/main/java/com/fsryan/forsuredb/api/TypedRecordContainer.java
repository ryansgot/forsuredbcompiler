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

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 *     A RecordContainer backed by Map&lt;String, String&gt; that remembers the original type and
 *     offers a convenience method of {@link #typedGet(String)}.
 * </p>
 * @author Ryan Scott
 */
public final class TypedRecordContainer implements RecordContainer {

    private final Map<String, Object> columnToValueMap = new HashMap<>();
    private final Map<String, Type> columnToTypeMap = new HashMap<>();

    @Override
    public Object get(String column) {
        if (!columnToValueMap.containsKey(column)) {
            return null;
        }

        return columnToValueMap.get(column);
    }

    @Override
    public void put(String column, String value) {
        columnToValueMap.put(column, value);
        columnToTypeMap.put(column, String.class);
    }

    @Override
    public void put(String column, long value) {
        columnToValueMap.put(column, value);
        columnToTypeMap.put(column, long.class);
    }

    @Override
    public void put(String column, int value) {
        columnToValueMap.put(column, value);
        columnToTypeMap.put(column, int.class);
    }

    @Override
    public void put(String column, double value) {
        columnToValueMap.put(column, value);
        columnToTypeMap.put(column, double.class);
    }

    @Override
    public void put(String column, float value) {
        columnToValueMap.put(column, value);
        columnToTypeMap.put(column, float.class);
    }

    @Override
    public void put(String column, byte[] value) {
        columnToValueMap.put(column, value);
        columnToTypeMap.put(column, byte[].class);
    }

    @Override
    public void clear() {
        columnToValueMap.clear();
        columnToTypeMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return columnToValueMap.keySet();
    }

    public Type getType(String column) {
        return columnToTypeMap.get(column);
    }

    /**
     * <p>
     *     Either incurs precision loss or throws {@link ClassCastException} if parameter is wrong
     * </p>
     * @param column The name of the column for which data is stored in this container
     * @param <T> The class of the stored column
     * @return a T or null if the column was not previously stored.
     */
    public <T> T typedGet(String column) {
        if (!columnToValueMap.containsKey(column)) {
            return null;
        }
        return (T) get(column);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(TypedRecordContainer.class.getSimpleName())
                .append("{columnToValueMap={");
        for (String key : keySet()) {
            buf.append(key).append('=');
            if (byte[].class == columnToTypeMap.get(key)) {
                byte[] ba = typedGet(key);
                buf.append(Arrays.toString(ba));
            } else {
                buf.append(String.valueOf(columnToValueMap.get(key)));
            }
            buf.append(", ");
        }
        return buf.delete(buf.length() - 2, buf.length())
                .append("}, columnToTypeMap=")
                .append(columnToTypeMap)
                .append('}').toString();
    }
}
