package com.fsryan.forsuredb.moshiserialization;

import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.squareup.moshi.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;

import static com.fsryan.forsuredb.moshiserialization.FSDbInfoMoshiSerializer.adapterFrom;

final class TableForeignKeyInfoAdapter extends JsonAdapter<TableForeignKeyInfo> {

    private static final JsonReader.Options OPTIONS = JsonReader.Options.of(
            "foreign_table_api_class_name",
            "foreign_table_name",
            "local_to_foreign_column_map",
            "update_action",
            "delete_action"
    );

    private final JsonAdapter<Map<String, String>> string2StringMapAdapter;

    public TableForeignKeyInfoAdapter(Moshi moshi) {
        string2StringMapAdapter = adapterFrom(moshi, Types.newParameterizedType(Map.class, String.class, String.class)).nullSafe();
    }

    @Override
    public TableForeignKeyInfo fromJson(@Nonnull JsonReader reader) throws IOException {
        if (reader.peek() == JsonReader.Token.NULL) {
            reader.nextNull();
            return null;
        }

        reader.beginObject();
        TableForeignKeyInfo.Builder builder = TableForeignKeyInfo.builder();
        while (reader.hasNext()) {
            switch (reader.selectName(OPTIONS)) {
                case 0: {
                    builder.foreignTableApiClassName(reader.nextString());
                    break;
                }
                case 1: {
                    builder.foreignTableName(reader.nextString());
                    break;
                }
                case 2: {
                    builder.addAllLocalToForeignColumns(string2StringMapAdapter.fromJson(reader));
                    break;
                }
                case 3: {
                    builder.updateChangeAction(reader.nextString());
                    break;
                }
                case 4: {
                    builder.deleteChangeAction(reader.nextString());
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
    public void toJson(@Nonnull JsonWriter writer, TableForeignKeyInfo obj) throws IOException {
        if (obj == null) {
            writer.nullValue();
            return;
        }

        writer.beginObject();

        writer.name("foreign_table_api_class_name");
        writer.value(obj.foreignTableApiClassName());

        writer.name("foreign_table_name");
        writer.value(obj.foreignTableName());

        writer.name("local_to_foreign_column_map");
        string2StringMapAdapter.toJson(writer, obj.localToForeignColumnMap());

        writer.name("update_action");
        writer.value(obj.updateChangeAction());

        writer.name("delete_action");
        writer.value(obj.deleteChangeAction());
        writer.endObject();
    }
}