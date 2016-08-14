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


import java.lang.reflect.Method;
import java.lang.reflect.Type;

/*package*/ class GetApiValidator {

    public static void validateCall(Object[] args) {
        if (args[0] == null || !(args[0] instanceof Retriever)) {
            throw new IllegalArgumentException("You must pass an object of the Retriever class as the first argument, passed: " + args[0].getClass().getName());
        }
    }

    public static <T> void validateClass(Class<T> tableApi) {
        for (Method m : tableApi.getDeclaredMethods()) {
            validateReturn(m);
            validateParameters(m);
        }
    }

    private static void validateReturn(Method m) {
        if (!FSGetAdapter.methodMap.containsKey(m.getGenericReturnType())) {
            throw new IllegalArgumentException("method " + m.getName() + " has illegal return type, supported types are: " + FSGetAdapter.methodMap.keySet().toString());
        }
    }

    private static void validateParameters(Method m) {
        Type[] types = m.getParameterTypes();
        if (types.length > 1) {
            throw new IllegalArgumentException("method " + m.getName() + " has more than one parameter. Only one Retriever parameter is allowed");
        }
        if (types.length < 1) {
            throw new IllegalArgumentException("method " + m.getName() + " has less than one parameter. A Retriever parameter is required.");
        }
        if (!types[0].equals(Retriever.class)) {
            throw new IllegalArgumentException("method " + m.getName() + " has a " + types[0].getClass().getName() + " parameter. A Retriever parameter is required.");
        }
    }
}
