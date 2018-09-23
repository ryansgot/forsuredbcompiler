/*
    forsuredbmodels-gson, gson serialization for forsuredb value classes

    Copyright 2017 Ryan Scott

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
package com.fsryan.forsuredb.gsonserialization;

import com.fsryan.forsuredb.info.*;
import com.fsryan.forsuredb.migration.Migration;
import com.fsryan.forsuredb.migration.MigrationSet;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

class DbInfoAdapterFactory implements TypeAdapterFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<T> rawType = (Class<T>) type.getRawType();
        if (ColumnInfo.class.isAssignableFrom(rawType)) {
            return (TypeAdapter<T>) new ColumnInfoAdapter(gson);
        } else if (TableInfo.class.isAssignableFrom(rawType)) {
            return (TypeAdapter<T>) new TableInfoAdapter(gson);
        } else if (Migration.class.isAssignableFrom(rawType)) {
            return (TypeAdapter<T>) new MigrationAdapter(gson);
        } else if (TableForeignKeyInfo.class.isAssignableFrom(rawType)) {
            return (TypeAdapter<T>) new TableForeignKeyInfoAdapter(gson);
        } else if (MigrationSet.class.isAssignableFrom(rawType)) {
            return (TypeAdapter<T>) new MigrationSetAdapter(gson);
        } else if (ForeignKeyInfo.class.isAssignableFrom(rawType)) {
            return (TypeAdapter<T>) new ForeignKeyInfoAdapter(gson);
        } else if (TableIndexInfo.class.isAssignableFrom(rawType)) {
            return (TypeAdapter<T>) new TableIndexInfoAdapter(gson);
        } else {
            return null;
        }
    }
}
