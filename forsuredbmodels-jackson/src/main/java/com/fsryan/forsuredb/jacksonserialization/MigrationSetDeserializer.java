package com.fsryan.forsuredb.jacksonserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.Migration;
import com.fsryan.forsuredb.migration.MigrationSet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MigrationSetDeserializer extends StdDeserializer<MigrationSet> {

    private static final TypeReference<List<Migration>> orderedMigrationsType = new TypeReference<List<Migration>>() {};
    private static final TypeReference<Map<String, TableInfo>> targetSchemaType = new TypeReference<Map<String, TableInfo>>() {};

    private final ObjectMapper mapper;

    protected MigrationSetDeserializer(ObjectMapper mapper) {
        super(MigrationSet.class);
        this.mapper = mapper;
    }

    /*
        @Override
    public MigrationSet read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }

        jsonReader.beginObject();

        MigrationSet.Builder builder = MigrationSet.builder();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                continue;
            }
            switch (name) {
                case "ordered_migrations":
                    builder.orderedMigrations(orderedMigrationAdapter.read(jsonReader));
                    break;
                case "target_schema":
                    builder.targetSchema(targetSchemaAdapter.read(jsonReader));
                    break;
                case "db_version":
                    builder.dbVersion(dbVersionAdapter.read(jsonReader));
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        return builder.build();
    }
     */

    @Override
    public MigrationSet deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        final List<Migration> orderedMigrations = mapper.readValue(node.get("ordered_migrations").toString(), orderedMigrationsType);
        final Map<String, TableInfo> targetSchema = mapper.readValue(node.get("target_schema").toString(), targetSchemaType);
        final int dbVersion = node.get("db_version").asInt();
        return MigrationSet.builder()
                .orderedMigrations(orderedMigrations)
                .targetSchema(targetSchema)
                .dbVersion(dbVersion)
                .build();
    }
}
