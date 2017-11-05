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
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Map;

class TableForeignKeyInfoAdapter extends TypeAdapter<TableForeignKeyInfo> {

    private static final TypeToken<Map<String, String>> localToForeignColumnMapType = new TypeToken<Map<String, String>>() {};

    private final TypeAdapter<Map<String, String>> localToForeignColumnMapAdapter;
    private final TypeAdapter<String> stringAdapter;

    public TableForeignKeyInfoAdapter(Gson gson) {
        localToForeignColumnMapAdapter = gson.getAdapter(localToForeignColumnMapType);
        stringAdapter = gson.getAdapter(String.class);
    }

    @Override
    public void write(JsonWriter jsonWriter, TableForeignKeyInfo object) throws IOException {
        if (object == null) {
            jsonWriter.nullValue();
            return;
        }

        jsonWriter.beginObject();
        jsonWriter.name("foreign_table_api_class_name");
        stringAdapter.write(jsonWriter, object.getForeignTableApiClassName());
        jsonWriter.name("foreign_table_name");
        stringAdapter.write(jsonWriter, object.foreignTableName());
        jsonWriter.name("local_to_foreign_column_map");
        localToForeignColumnMapAdapter.write(jsonWriter, object.localToForeignColumnMap());
        jsonWriter.name("update_action");
        stringAdapter.write(jsonWriter, object.updateChangeAction());
        jsonWriter.name("delete_action");
        stringAdapter.write(jsonWriter, object.deleteChangeAction());
        jsonWriter.endObject();
    }

    @Override
    public TableForeignKeyInfo read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }
        
        jsonReader.beginObject();

        TableForeignKeyInfo.Builder builder = TableForeignKeyInfo.builder();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                continue;
            }
            switch (name) {
                case "foreign_table_api_class_name":
                    builder.foreignTableApiClassName(stringAdapter.read(jsonReader));
                    break;
                case "foreign_table_name":
                    builder.foreignTableName(stringAdapter.read(jsonReader));
                    break;
                case "local_to_foreign_column_map":
                    builder.localToForeignColumnMap(localToForeignColumnMapAdapter.read(jsonReader));
                    break;
                case "update_action":
                    builder.updateChangeAction(stringAdapter.read(jsonReader));
                    break;
                case "delete_action":
                    builder.deleteChangeAction(stringAdapter.read(jsonReader));
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        return builder.build();
    }
}