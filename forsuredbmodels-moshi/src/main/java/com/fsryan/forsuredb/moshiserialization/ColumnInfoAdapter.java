package com.fsryan.forsuredb.moshiserialization;

import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.ForeignKeyInfo;
import com.squareup.moshi.*;

import javax.annotation.Nonnull;
import java.io.IOException;

import static com.fsryan.forsuredb.moshiserialization.FSDbInfoMoshiSerializer.adapterFrom;

final class ColumnInfoAdapter extends JsonAdapter<ColumnInfo> {

    private static final JsonReader.Options OPTIONS = JsonReader.Options.of(
            "method_name",
            "column_name",
            "column_type",
            "index",
            "default_value",
            "unique",
            "primary_key",
            "foreign_key_info",
            "searchable",
            "orderable"
    );

    private final JsonAdapter<ForeignKeyInfo> foreignKeyInfoAdapter;

    public ColumnInfoAdapter(Moshi moshi) {
        this.foreignKeyInfoAdapter = adapterFrom(moshi, ForeignKeyInfo.class).nullSafe();
    }

    @Override
    public ColumnInfo fromJson(@Nonnull JsonReader reader) throws IOException {
        if (reader.peek() == JsonReader.Token.NULL) {
            reader.nextNull();
            return null;
        }

        ColumnInfo.Builder builder = ColumnInfo.builder();
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.selectName(OPTIONS)) {
                case 0: {
                    builder.methodName(reader.nextString());
                    break;
                }
                case 1: {
                    builder.columnName(reader.nextString());
                    break;
                }
                case 2: {
                    builder.qualifiedType(reader.nextString());
                    break;
                }
                case 3: {
                    builder.index(reader.nextBoolean());
                    break;
                }
                case 4: {
                    builder.defaultValue(reader.nextString());
                    break;
                }
                case 5: {
                    builder.unique(reader.nextBoolean());
                    break;
                }
                case 6: {
                    builder.primaryKey(reader.nextBoolean());
                    break;
                }
                case 7: {
                    builder.foreignKeyInfo(foreignKeyInfoAdapter.fromJson(reader));
                    break;
                }
                case 8: {
                    builder.searchable(reader.nextBoolean());
                    break;
                }
                case 9: {
                    builder.orderable(reader.nextBoolean());
                    break;
                }
                case -1: {
                    // Unknown name, skip it
                    reader.nextName();
                    reader.skipValue();
                }
            }
        }
        reader.endObject();
        return builder.build();
    }

    @Override
    public void toJson(@Nonnull JsonWriter writer, ColumnInfo obj) throws IOException {
        if (obj == null) {
            writer.nullValue();
            return;
        }

        writer.beginObject();

        writer.name("method_name");
        writer.value(obj.methodName());

        writer.name("column_name");
        writer.value(obj.getColumnName());

        String qualifiedType = obj.qualifiedType();
        if (qualifiedType != null) {
            writer.name("column_type");
            writer.value(qualifiedType);
        }

        writer.name("index");
        writer.value(obj.index());

        String defaultValue = obj.defaultValue();
        if (defaultValue != null) {
            writer.name("default_value");
            writer.value(obj.defaultValue());
        }

        writer.name("unique");
        writer.value(obj.unique());

        writer.name("primary_key");
        writer.value(obj.primaryKey());

        ForeignKeyInfo foreignKeyInfo = obj.foreignKeyInfo();
        if (foreignKeyInfo != null) {
            writer.name("foreign_key_info");
            foreignKeyInfoAdapter.toJson(writer, foreignKeyInfo);
        }

        writer.name("searchable");
        writer.value(obj.searchable());

        writer.name("orderable");
        writer.value(obj.orderable());

        writer.endObject();
    }
}