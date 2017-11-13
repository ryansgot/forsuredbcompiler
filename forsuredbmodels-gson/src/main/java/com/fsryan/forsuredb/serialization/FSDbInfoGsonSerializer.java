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
package com.fsryan.forsuredb.serialization;

import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.migration.MigrationSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * <p>
 *     If you want to use {@link com.google.gson.Gson Gson}, then you can use this default implementation.
 * </p>
 */
public final class FSDbInfoGsonSerializer implements FSDbInfoSerializer {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(new DbInfoAdapterFactory())
            .create();
    private static final Type foreignKeysType = new TypeToken<Set<TableForeignKeyInfo>>() {}.getType();

    @Override
    public MigrationSet deserializeMigrationSet(InputStream stream) {
        return gson.fromJson(new JsonReader(new InputStreamReader(stream)), MigrationSet.class);
    }

    @Override
    public MigrationSet deserializeMigrationSet(String json) {
        return gson.fromJson(json, MigrationSet.class);
    }

    @Override
    public Set<TableForeignKeyInfo> deserializeForeignKeys(String json) {
        return gson.fromJson(json, foreignKeysType);
    }

    public String serialize(MigrationSet migrationSet) {
        return gson.toJson(migrationSet);
    }
}
