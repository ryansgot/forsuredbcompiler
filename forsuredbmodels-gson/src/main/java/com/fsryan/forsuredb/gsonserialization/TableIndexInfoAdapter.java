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

import com.fsryan.forsuredb.info.TableIndexInfo;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Map;
//TODO: test this
class TableIndexInfoAdapter extends TypeAdapter<TableIndexInfo> {

    private static final TypeToken<Map<String, String>> columnSortOrderMapType = new TypeToken<Map<String, String>>() {};

    private final TypeAdapter<Map<String, String>> columnSortOrderMapAdapter;

    public TableIndexInfoAdapter(Gson gson) {
        columnSortOrderMapAdapter = gson.getAdapter(columnSortOrderMapType);
    }

    @Override
    public void write(JsonWriter jsonWriter, TableIndexInfo object) throws IOException {
        if (object == null) {
            jsonWriter.nullValue();
            return;
        }

        jsonWriter.beginObject();
        jsonWriter.name("column_sort_order_map");
        columnSortOrderMapAdapter.write(jsonWriter, object.columnSortOrderMap());
        jsonWriter.name("unique");
        jsonWriter.value(object.unique());
        jsonWriter.endObject();
    }

    @Override
    public TableIndexInfo read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }
        
        jsonReader.beginObject();

        Map<String, String> columnSortOrderMap = null;
        Boolean unique = null;
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                continue;
            }
            switch (name) {
                case "column_sort_order_map":
                    columnSortOrderMap = columnSortOrderMapAdapter.read(jsonReader);
                    break;
                case "unique":
                    unique = jsonReader.nextBoolean();
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();

        if (columnSortOrderMap == null) {
            throw new IllegalStateException("TableIndexInfo must have a columnSortOrderMap");
        }
        if (unique == null) {
            throw new IllegalStateException("TableIndexInfo must have a value for unique");
        }
        return TableIndexInfo.create(columnSortOrderMap, unique);
    }
}