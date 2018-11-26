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

import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.info.TableInfo;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

class TableInfoAdapter extends TypeAdapter<TableInfo> {

    private static TypeToken<Map<String, ColumnInfo>> columnMapType = new TypeToken<Map<String, ColumnInfo>>() {};
    private static TypeToken<Set<String>> primaryKeyType = new TypeToken<Set<String>>() {};
    private static TypeToken<Set<TableForeignKeyInfo>> foreignKeysType = new TypeToken<Set<TableForeignKeyInfo>>() {};

    private final TypeAdapter<Map<String, ColumnInfo>> columnMapAdapter;
    private final TypeAdapter<String> stringAdapter;
    private final TypeAdapter<Set<String>> primaryKeyAdapter;
    private final TypeAdapter<Set<TableForeignKeyInfo>> foreignKeysAdapter;

    TableInfoAdapter(Gson gson) {
        columnMapAdapter = gson.getAdapter(columnMapType);
        stringAdapter = gson.getAdapter(String.class);
        primaryKeyAdapter = gson.getAdapter(primaryKeyType);
        foreignKeysAdapter = gson.getAdapter(foreignKeysType);
    }

    @Override
    public void write(JsonWriter jsonWriter, TableInfo object) throws IOException {
        if (object == null) {
            jsonWriter.nullValue();
            return;
        }

        jsonWriter.beginObject();
        jsonWriter.name("column_info_map");
        columnMapAdapter.write(jsonWriter, object.columnMap());
        jsonWriter.name("table_name");
        stringAdapter.write(jsonWriter, object.tableName());
        jsonWriter.name("qualified_class_name");
        stringAdapter.write(jsonWriter, object.qualifiedClassName());
        jsonWriter.name("static_data_asset");
        stringAdapter.write(jsonWriter, object.staticDataAsset());
        jsonWriter.name("static_data_record_name");
        stringAdapter.write(jsonWriter, object.staticDataRecordName());
        jsonWriter.name("doc_store_parameterization");
        stringAdapter.write(jsonWriter, object.docStoreParameterization());
        jsonWriter.name("primary_key");
        primaryKeyAdapter.write(jsonWriter, object.getPrimaryKey());
        jsonWriter.name("primary_key_on_conflict");
        stringAdapter.write(jsonWriter, object.primaryKeyOnConflict());
        jsonWriter.name("foreign_keys");
        foreignKeysAdapter.write(jsonWriter, object.foreignKeys());
        jsonWriter.endObject();
    }

    @Override
    public TableInfo read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }
        
        jsonReader.beginObject();

        TableInfo.Builder builder = TableInfo.builder();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                continue;
            }
            switch (name) {
                case "column_info_map":
                    builder.addAllColumns(columnMapAdapter.read(jsonReader).values());
                    break;
                case "table_name":
                    builder.tableName(stringAdapter.read(jsonReader));
                    break;
                case "qualified_class_name":
                    builder.qualifiedClassName(stringAdapter.read(jsonReader));
                    break;
                case "static_data_asset":
                    builder.staticDataAsset(stringAdapter.read(jsonReader));
                    break;
                case "static_data_record_name":
                    builder.staticDataRecordName(stringAdapter.read(jsonReader));
                    break;
                case "doc_store_parameterization":
                    builder.docStoreParameterization(stringAdapter.read(jsonReader));
                    break;
                case "primary_key":
                    builder.resetPrimaryKey(primaryKeyAdapter.read(jsonReader));
                    break;
                case "primary_key_on_conflict":
                    builder.primaryKeyOnConflict(stringAdapter.read(jsonReader));
                    break;
                case "foreign_keys":
                    builder.addAllForeignKeys(foreignKeysAdapter.read(jsonReader));
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        return builder.build();
    }
}