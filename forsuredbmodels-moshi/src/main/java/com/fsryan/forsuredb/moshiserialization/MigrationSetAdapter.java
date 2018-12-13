package com.fsryan.forsuredb.moshiserialization;

import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.Migration;
import com.fsryan.forsuredb.migration.MigrationSet;
import com.fsryan.forsuredb.migration.SchemaDiff;
import com.squareup.moshi.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.fsryan.forsuredb.moshiserialization.FSDbInfoMoshiSerializer.adapterFrom;

final class MigrationSetAdapter extends JsonAdapter<MigrationSet> {

    private static final JsonReader.Options OPTIONS = JsonReader.Options.of(
            "ordered_migrations",
            "target_schema",
            "db_version",
            "set_version",
            "diff_map"
    );

    private final JsonAdapter<List<Migration>> orderedMigrationsAdapter;
    private final JsonAdapter<Map<String, TableInfo>> targetSchemaAdapter;
    private final JsonAdapter<Map<String, Set<SchemaDiff>>> diffMapAdapter;

    public MigrationSetAdapter(Moshi moshi) {
        orderedMigrationsAdapter = adapterFrom(moshi, Types.newParameterizedType(List.class, Migration.class)).nullSafe();
        targetSchemaAdapter = adapterFrom(moshi, Types.newParameterizedType(Map.class, String.class, TableInfo.class)).nullSafe();
        diffMapAdapter = adapterFrom(moshi, Types.newParameterizedType(Map.class, String.class, Types.newParameterizedType(Set.class, SchemaDiff.class))).nullSafe();
    }

    @Override
    public MigrationSet fromJson(@Nonnull JsonReader reader) throws IOException {
        if (reader.peek() == JsonReader.Token.NULL) {
            reader.nextNull();
            return null;
        }

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
                case 3: {
                    builder.setVersion(reader.nextInt());
                    break;
                }
                case 4: {
                    builder.mergeDiffMap(diffMapAdapter.fromJson(reader));
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
    public void toJson(@Nonnull JsonWriter writer, MigrationSet obj) throws IOException {
        if (obj == null) {
            writer.nullValue();
            return;
        }

        writer.beginObject();
        if (obj.containsMigrations()) {
            writer.name("ordered_migrations");
            orderedMigrationsAdapter.toJson(writer, obj.orderedMigrations());
        }
        if (obj.containsDiffs()) {
            writer.name("diff_map");
            diffMapAdapter.toJson(writer, obj.diffMap());
        }
        writer.name("target_schema");
        targetSchemaAdapter.toJson(writer, obj.targetSchema());
        writer.name("db_version");
        writer.value(obj.dbVersion());
        writer.name("set_version");
        writer.value(obj.setVersion());
        writer.endObject();
    }
}