/*
    forsuredbmodels-moshi, moshi serialization for forsuredb value classes

    Copyright 2018 Ryan Scott

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
package com.fsryan.forsuredb.moshiserialization;

import com.fsryan.forsuredb.migration.SchemaDiff;
import com.squareup.moshi.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;

import static com.fsryan.forsuredb.moshiserialization.FSDbInfoMoshiSerializer.adapterFrom;

class SchemaDiffAdapter extends JsonAdapter<SchemaDiff> {

    private static final JsonReader.Options OPTIONS = JsonReader.Options.of(
            "type",
            "sub_type",
            "table_name",
            "attributes"
    );

    private final JsonAdapter<Map<String, String>> attributesTypeAdapter;

    SchemaDiffAdapter(Moshi moshi) {
        attributesTypeAdapter = adapterFrom(moshi, Types.newParameterizedType(Map.class, String.class, String.class)).nullSafe();
    }

    @Override
    public void toJson(@Nonnull JsonWriter writer, SchemaDiff obj) throws IOException {
        if (obj == null) {
            writer.nullValue();
            return;
        }

        writer.beginObject();
        writer.name("type");
        writer.value(obj.type());
        writer.name("sub_type");
        writer.value(obj.subType());
        writer.name("table_name");
        writer.value(obj.tableName());
        writer.name("attributes");
        attributesTypeAdapter.toJson(writer, obj.attributes());
        writer.endObject();
    }

    @Override
    public SchemaDiff fromJson(@Nonnull JsonReader reader) throws IOException {
        if (reader.peek() == JsonReader.Token.NULL) {
            reader.nextNull();
            return null;
        }

        SchemaDiff.Builder builder = SchemaDiff.builder();
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.selectName(OPTIONS)) {
                case 0: {
                    builder.type(reader.nextInt());
                    break;
                }
                case 1: {
                    builder.replaceSubType(reader.nextLong());
                    break;
                }
                case 2: {
                    builder.tableName(reader.nextString());
                    break;
                }
                case 3: {
                    builder.addAllAttributes(attributesTypeAdapter.fromJson(reader));
                    break;
                }
                case -1: {
                    reader.nextName();
                    reader.skipValue();
                }
            }
        }
        reader.endObject();
        return builder.build();
    }
}