package com.fsryan.forsuredb.moshiserialization;

import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.Migration;
import com.fsryan.forsuredb.migration.MigrationSet;
import com.squareup.moshi.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.fsryan.forsuredb.moshiserialization.FSDbInfoMoshiSerializer.adapterFrom;

final class MigrationSetAdapter extends JsonAdapter<MigrationSet> {

    private static final JsonReader.Options OPTIONS = JsonReader.Options.of(
            "ordered_migrations",
            "target_schema",
            "db_version"
    );

    private final JsonAdapter<List<Migration>> orderedMigrationsAdapter;
    private final JsonAdapter<Map<String, TableInfo>> targetSchemaAdapter;

    public MigrationSetAdapter(Moshi moshi) {
        this.orderedMigrationsAdapter = adapterFrom(moshi, Types.newParameterizedType(List.class, Migration.class)).nullSafe();
        this.targetSchemaAdapter = adapterFrom(moshi, Types.newParameterizedType(Map.class, String.class, TableInfo.class)).nullSafe();
    }

    @Override
    public MigrationSet fromJson(JsonReader reader) throws IOException {
        reader.beginObject();

        MigrationSet.Builder builder = MigrationSet.builder();
        while (reader.hasNext()) {
            switch (reader.selectName(OPTIONS)) {
                case 0: {
                    builder.orderedMigrations(orderedMigrationsAdapter.fromJson(reader));
                    break;
                }
                case 1: {
                    builder.targetSchema(targetSchemaAdapter.fromJson(reader));
                    break;
                }
                case 2: {
                    builder.dbVersion(reader.nextInt());
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
    public void toJson(JsonWriter writer, @Nonnull MigrationSet value) throws IOException {
        writer.beginObject();

        writer.name("ordered_migrations");
        orderedMigrationsAdapter.toJson(writer, value.orderedMigrations());

        writer.name("target_schema");
        targetSchemaAdapter.toJson(writer, value.targetSchema());

        writer.name("db_version");
        writer.value(value.dbVersion());

        writer.endObject();
    }
}