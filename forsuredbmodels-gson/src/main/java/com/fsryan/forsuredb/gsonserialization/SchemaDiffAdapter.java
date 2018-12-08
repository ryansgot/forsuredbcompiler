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

import com.fsryan.forsuredb.migration.SchemaDiff;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Map;

class SchemaDiffAdapter extends TypeAdapter<SchemaDiff> {

    private static final TypeToken<Map<String, String>> attributesType = new TypeToken<Map<String, String>>() {};

    private final TypeAdapter<Map<String, String>> attributesTypeAdapter;

    SchemaDiffAdapter(Gson gson) {
        attributesTypeAdapter = gson.getAdapter(attributesType);
    }

    @Override
    public void write(JsonWriter jsonWriter, SchemaDiff object) throws IOException {
        if (object == null) {
            jsonWriter.nullValue();
            return;
        }

        jsonWriter.beginObject();
        jsonWriter.name("type");
        jsonWriter.value(object.type());
        jsonWriter.name("sub_type");
        jsonWriter.value(object.subType());
//        jsonWriter.name("category");
//        jsonWriter.value(object.category());
        jsonWriter.name("table_name");
        jsonWriter.value(object.tableName());
        jsonWriter.name("attributes");
        attributesTypeAdapter.write(jsonWriter, object.attributes());
        jsonWriter.endObject();
    }

    @Override
    public SchemaDiff read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }

        jsonReader.beginObject();
        SchemaDiff.Builder builder = SchemaDiff.builder();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                continue;
            }
            switch (name) {
                case "type":
                    builder.type(jsonReader.nextInt());
                    break;
                case "sub_type":
                    builder.replaceSubType(jsonReader.nextLong());
                    break;
//                case "category":
//                    builder.category(jsonReader.nextInt());
//                    break;
                case "table_name":
                    builder.tableName(jsonReader.nextString());
                    break;
                case "attributes":
                    builder.addAllAttributes(attributesTypeAdapter.read(jsonReader));
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        return builder.build();
    }
}