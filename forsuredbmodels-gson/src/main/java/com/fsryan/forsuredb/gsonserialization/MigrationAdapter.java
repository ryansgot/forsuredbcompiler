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

import com.fsryan.forsuredb.migration.Migration;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Map;

class MigrationAdapter extends TypeAdapter<Migration> {

    private static final TypeToken<Map<String, String>> extrasType = new TypeToken<Map<String, String>>() {};

    private final TypeAdapter<String> stringAdapter;
    private final TypeAdapter<Map<String, String>> extrasAdapter;

    public MigrationAdapter(Gson gson) {
        stringAdapter = gson.getAdapter(String.class);
        extrasAdapter = gson.getAdapter(extrasType);
    }

    @Override
    public void write(JsonWriter jsonWriter, Migration object) throws IOException {
        if (object == null) {
            jsonWriter.nullValue();
            return;
        }

        jsonWriter.beginObject();
        jsonWriter.name("table_name");
        stringAdapter.write(jsonWriter, object.tableName());
        jsonWriter.name("column_name");
        stringAdapter.write(jsonWriter, object.columnName());
        jsonWriter.name("migration_type");
        stringAdapter.write(jsonWriter, object.type().name());
        jsonWriter.name("extras");
        extrasAdapter.write(jsonWriter, object.extras());
        jsonWriter.endObject();
    }

    @Override
    public Migration read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }

        jsonReader.beginObject();

        Migration.Builder builder = Migration.builder();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                continue;
            }
            switch (name) {
                case "table_name":
                    builder.tableName(stringAdapter.read(jsonReader));
                    break;
                case "column_name":
                    builder.columnName(stringAdapter.read(jsonReader));
                    break;
                case "migration_type":
                    builder.type(Migration.Type.from(stringAdapter.read(jsonReader)));
                    break;
                case "extras":
                    builder.extras(extrasAdapter.read(jsonReader));
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        return builder.build();
    }
}
