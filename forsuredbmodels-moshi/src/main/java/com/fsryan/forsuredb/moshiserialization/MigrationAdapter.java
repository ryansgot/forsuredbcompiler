package com.fsryan.forsuredb.moshiserialization;

import com.fsryan.forsuredb.migration.Migration;
import com.squareup.moshi.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;

import static com.fsryan.forsuredb.moshiserialization.FSDbInfoMoshiSerializer.adapterFrom;

final class MigrationAdapter extends JsonAdapter<Migration> {

    private static final JsonReader.Options OPTIONS = JsonReader.Options.of(
            "table_name",
            "column_name",
            "migration_type",
            "extras"
    );

    private final JsonAdapter<Map<String, String>> extrasAdapter;

    public MigrationAdapter(Moshi moshi) {
        this.extrasAdapter = adapterFrom(moshi, Types.newParameterizedType(Map.class, String.class, String.class)).nullSafe();
    }

    @Override
    public Migration fromJson(@Nonnull JsonReader reader) throws IOException {
        if (reader.peek() == JsonReader.Token.NULL) {
            reader.nextNull();
            return null;
        }

        reader.beginObject();
        Migration.Builder builder = Migration.builder();
        while (reader.hasNext()) {
            switch (reader.selectName(OPTIONS)) {
                case 0: {
                    builder.tableName(reader.nextString());
                    break;
                }
                case 1: {
                    builder.columnName(reader.nextString());
                    break;
                }
                case 2: {
                    builder.type(Migration.Type.from(reader.nextString()));
                    break;
                }
                case 3: {
                    Map<String, String> extras = extrasAdapter.fromJson(reader);
                    if (extras != null) {
                        builder.putAllExtras(extras);
                    }
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
    public void toJson(@Nonnull JsonWriter writer, Migration obj) throws IOException {
        if (obj == null) {
            writer.nullValue();
            return;
        }

        writer.beginObject();
        writer.name("table_name");
        writer.value(obj.tableName());

        String columnName = obj.columnName();
        if (columnName != null) {
            writer.name("column_name");
            writer.value(columnName);
        }

        writer.name("migration_type");
        writer.value(obj.type().toString());

        if (obj.hasExtras()) {
            writer.name("extras");
            extrasAdapter.toJson(writer, obj.extras());
        }

        writer.endObject();
    }
}
