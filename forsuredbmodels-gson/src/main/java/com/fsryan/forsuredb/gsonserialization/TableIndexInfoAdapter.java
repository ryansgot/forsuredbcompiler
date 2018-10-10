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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
//TODO: test this
class TableIndexInfoAdapter extends TypeAdapter<TableIndexInfo> {

    private static final TypeToken<List<String>> listStringToken = new TypeToken<List<String>>() {};

    private final TypeAdapter<List<String>> listStringAdapter;

    public TableIndexInfoAdapter(Gson gson) {
        listStringAdapter = gson.getAdapter(listStringToken);
    }

    @Override
    public void write(JsonWriter jsonWriter, TableIndexInfo obj) throws IOException {
        if (obj == null) {
            jsonWriter.nullValue();
            return;
        }

        List<String> cols = obj.columns();
        Map<String, String> sortOrderMap = obj.columnSortOrderMap();
        List<String> sorts = new ArrayList<>(cols.size());
        for (String col : cols) {
            sorts.add(sortOrderMap.get(col));
        }

        jsonWriter.beginObject();
        jsonWriter.name("unique");
        jsonWriter.value(obj.unique());
        jsonWriter.name("columns");
        listStringAdapter.write(jsonWriter, cols);
        jsonWriter.name("column_sort_orders");
        listStringAdapter.write(jsonWriter, sorts);
        jsonWriter.endObject();
    }

    @Override
    public TableIndexInfo read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }
        
        jsonReader.beginObject();

        List<String> cols = null;
        List<String> sorts = null;
        Boolean unique = null;
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                continue;
            }
            switch (name) {
                case "unique":
                    unique = jsonReader.nextBoolean();
                    break;
                case "columns":
                    cols = listStringAdapter.read(jsonReader);
                    break;
                case "column_sort_orders":
                    sorts = listStringAdapter.read(jsonReader);
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();

        if (cols == null) {
            throw new IllegalStateException("TableIndexInfo must have a columns value");
        }
        if (sorts == null) {
            throw new IllegalStateException("TableIndexInfo must have a column_sort_orders value");
        }
        if (unique == null) {
            throw new IllegalStateException("TableIndexInfo must have a value for unique");
        }
        return TableIndexInfo.create(unique, cols, sorts);
    }
}