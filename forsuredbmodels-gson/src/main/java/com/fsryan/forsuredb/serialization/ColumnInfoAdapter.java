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

import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.ForeignKeyInfo;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

class ColumnInfoAdapter extends TypeAdapter<ColumnInfo> {

    private final TypeAdapter<ForeignKeyInfo> foreignKeyInfoAdapter;
    private final TypeAdapter<String> stringAdapter;
    private final TypeAdapter<Boolean> booleanAdapter;

    public ColumnInfoAdapter(Gson gson) {
        foreignKeyInfoAdapter = gson.getAdapter(ForeignKeyInfo.class);
        stringAdapter = gson.getAdapter(String.class);
        booleanAdapter = gson.getAdapter(Boolean.class);
    }

    @Override
    public void write(JsonWriter jsonWriter, ColumnInfo object) throws IOException {
        if (object == null) {
            jsonWriter.nullValue();
            return;
        }

        jsonWriter.beginObject();
        jsonWriter.name("method_name");
        stringAdapter.write(jsonWriter, object.methodName());
        jsonWriter.name("column_name");
        stringAdapter.write(jsonWriter, object.getColumnName());
        jsonWriter.name("column_type");
        stringAdapter.write(jsonWriter, object.getQualifiedType());
        jsonWriter.name("index");
        booleanAdapter.write(jsonWriter, object.index());
        jsonWriter.name("default_value");
        stringAdapter.write(jsonWriter, object.defaultValue());
        jsonWriter.name("unique");
        booleanAdapter.write(jsonWriter, object.unique());
        jsonWriter.name("primary_key");
        booleanAdapter.write(jsonWriter, object.primaryKey());
        jsonWriter.name("foreign_key_info");
        foreignKeyInfoAdapter.write(jsonWriter, object.foreignKeyInfo());
        jsonWriter.name("searchable");
        booleanAdapter.write(jsonWriter, object.searchable());
        jsonWriter.name("orderable");
        booleanAdapter.write(jsonWriter, object.orderable());
        jsonWriter.endObject();
    }

    @Override
    public ColumnInfo read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }
        
        jsonReader.beginObject();

        ColumnInfo.Builder builder = ColumnInfo.builder();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                continue;
            }
            switch (name) {
                case "method_name":
                    builder.methodName(stringAdapter.read(jsonReader));
                    break;
                case "column_name":
                    builder.columnName(stringAdapter.read(jsonReader));
                    break;
                case "column_type":
                    builder.qualifiedType(stringAdapter.read(jsonReader));
                    break;
                case "index":
                    builder.index(booleanAdapter.read(jsonReader));
                    break;
                case "default_value":
                    builder.defaultValue(stringAdapter.read(jsonReader));
                    break;
                case "unique":
                    builder.unique(booleanAdapter.read(jsonReader));
                    break;
                case "primary_key":
                    builder.primaryKey(booleanAdapter.read(jsonReader));
                    break;
                case "foreign_key_info":
                    builder.foreignKeyInfo(foreignKeyInfoAdapter.read(jsonReader));
                    break;
                case "searchable":
                    builder.searchable(booleanAdapter.read(jsonReader));
                    break;
                case "orderable":
                    builder.orderable(booleanAdapter.read(jsonReader));
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        return builder.build();
    }
}