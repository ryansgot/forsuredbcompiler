package com.fsryan.forsuredb.moshiserialization;

import com.fsryan.forsuredb.info.ForeignKeyInfo;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;

import javax.annotation.Nonnull;
import java.io.IOException;

@Deprecated
final class ForeignKeyInfoAdapter extends JsonAdapter<ForeignKeyInfo> {

    private static final JsonReader.Options OPTIONS = JsonReader.Options.of(
            "update_action",
            "delete_action",
            "foreign_table_name",
            "foreign_column_name",
            "foreign_api_class_name"
    );

    public ForeignKeyInfoAdapter() {}

    @Override
    public ForeignKeyInfo fromJson(@Nonnull JsonReader reader) throws IOException {
        if (reader.peek() == JsonReader.Token.NULL) {
            reader.nextNull();
            return null;
        }

        reader.beginObject();
        ForeignKeyInfo.Builder builder = ForeignKeyInfo.builder();
        while (reader.hasNext()) {
            switch (reader.selectName(OPTIONS)) {
                case 0: {
                    builder.updateAction(reader.nextString());
                    break;
                }
                case 1: {
                    builder.deleteAction(reader.nextString());
                    break;
                }
                case 2: {
                    builder.tableName(reader.nextString());
                    break;
                }
                case 3: {
                    builder.columnName(reader.nextString());
                    break;
                }
                case 4: {
                    builder.apiClassName(reader.nextString());
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
    @Override
    public void toJson(@Nonnull JsonWriter writer, ForeignKeyInfo obj) throws IOException {
        if (obj == null) {
            writer.nullValue();
            return;
        }

        writer.beginObject();
        writer.name("update_action");
        writer.value(obj.updateAction());

        writer.name("delete_action");
        writer.value(obj.deleteAction());

        String tableName = obj.tableName();
        if (tableName != null) {
            writer.name("foreign_table_name");
            writer.value(obj.tableName());
        }

        writer.name("foreign_column_name");
        writer.value(obj.columnName());

        writer.name("foreign_api_class_name");
        writer.value(obj.apiClassName());
        writer.endObject();
    }
}
