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

import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.Migration;
import com.fsryan.forsuredb.migration.MigrationSet;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

class MigrationSetAdapter extends TypeAdapter<MigrationSet> {

    private static TypeToken<List<Migration>> orderedMigrationType = new TypeToken<List<Migration>>() {};
    private static TypeToken<Map<String, TableInfo>> targetSchemaType = new TypeToken<Map<String, TableInfo>>() {};

    private final TypeAdapter<List<Migration>> orderedMigrationAdapter;
    private final TypeAdapter<Map<String, TableInfo>> targetSchemaAdapter;
    private final TypeAdapter<Integer> dbVersionAdapter;

    MigrationSetAdapter(Gson gson) {
        orderedMigrationAdapter = gson.getAdapter(orderedMigrationType);
        targetSchemaAdapter = gson.getAdapter(targetSchemaType);
        dbVersionAdapter = gson.getAdapter(Integer.class);
    }

    @Override
    public void write(JsonWriter jsonWriter, MigrationSet object) throws IOException {
        if (object == null) {
            jsonWriter.nullValue();
            return;
        }

        jsonWriter.beginObject();
        jsonWriter.name("ordered_migrations");
        orderedMigrationAdapter.write(jsonWriter, object.orderedMigrations());
        jsonWriter.name("target_schema");
        targetSchemaAdapter.write(jsonWriter, object.targetSchema());
        jsonWriter.name("db_version");
        dbVersionAdapter.write(jsonWriter, object.dbVersion());
        jsonWriter.endObject();
    }

    @Override
    public MigrationSet read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }

        jsonReader.beginObject();

        MigrationSet.Builder builder = MigrationSet.builder();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                continue;
            }
            switch (name) {
                case "ordered_migrations":
                    builder.orderedMigrations(orderedMigrationAdapter.read(jsonReader));
                    break;
                case "target_schema":
                    builder.targetSchema(targetSchemaAdapter.read(jsonReader));
                    break;
                case "db_version":
                    builder.dbVersion(dbVersionAdapter.read(jsonReader));
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        return builder.build();
    }
}