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

import com.fsryan.forsuredb.info.ForeignKeyInfo;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

@Deprecated
class ForeignKeyInfoAdapter extends TypeAdapter<ForeignKeyInfo> {

    private final TypeAdapter<String> stringAdapter;

    public ForeignKeyInfoAdapter(Gson gson) {
        stringAdapter = gson.getAdapter(String.class);
    }

    @Override
    public void write(JsonWriter jsonWriter, ForeignKeyInfo object) throws IOException {
        if (object == null) {
            jsonWriter.nullValue();
            return;
        }

        jsonWriter.beginObject();
        jsonWriter.name("update_action");
        stringAdapter.write(jsonWriter, object.updateAction());
        jsonWriter.name("delete_action");
        stringAdapter.write(jsonWriter, object.deleteAction());
        jsonWriter.name("foreign_table_name");
        stringAdapter.write(jsonWriter, object.tableName());
        jsonWriter.name("foreign_column_name");
        stringAdapter.write(jsonWriter, object.columnName());
        jsonWriter.name("foreign_api_class_name");
        stringAdapter.write(jsonWriter, object.apiClassName());
        jsonWriter.endObject();
    }

    @Override
    public ForeignKeyInfo read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }

        jsonReader.beginObject();

        ForeignKeyInfo.Builder builder = ForeignKeyInfo.builder();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                continue;
            }
            switch (name) {
                case "update_action":
                    builder.updateAction(stringAdapter.read(jsonReader));
                    break;
                case "delete_action":
                    builder.deleteAction(stringAdapter.read(jsonReader));
                    break;
                case "foreign_table_name":
                    builder.tableName(stringAdapter.read(jsonReader));
                    break;
                case "foreign_column_name":
                    builder.columnName(stringAdapter.read(jsonReader));
                    break;
                case "foreign_api_class_name":
                    builder.apiClassName(stringAdapter.read(jsonReader));
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        return builder.build();
    }
}
