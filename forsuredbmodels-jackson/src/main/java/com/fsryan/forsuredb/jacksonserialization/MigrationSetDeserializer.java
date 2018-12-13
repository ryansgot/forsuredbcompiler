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
import com.fsryan.forsuredb.migration.SchemaDiff;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MigrationSetDeserializer extends StdDeserializer<MigrationSet> {

    private static final TypeReference<List<Migration>> orderedMigrationsType = new TypeReference<List<Migration>>() {};
    private static final TypeReference<Map<String, TableInfo>> targetSchemaType = new TypeReference<Map<String, TableInfo>>() {};
    private static final TypeReference<Map<String, Set<SchemaDiff>>> diffMapType = new TypeReference<Map<String, Set<SchemaDiff>>>() {};

    private final ObjectMapper mapper;

    protected MigrationSetDeserializer(ObjectMapper mapper) {
        super(MigrationSet.class);
        this.mapper = mapper;
    }

    @Override
    public MigrationSet deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        MigrationSet.Builder builder = MigrationSet.builder();

        JsonNode node = jp.getCodec().readTree(jp);
        if (node.hasNonNull("ordered_migrations")) {
            final List<Migration> orderedMigrations = mapper.readValue(node.get("ordered_migrations").toString(), orderedMigrationsType);
            builder.orderedMigrations(orderedMigrations);
        }
        if (node.hasNonNull("set_version")) {
            builder.setVersion(node.get("set_version").asInt());
        }
        if (node.hasNonNull("diff_map")) {
            final Map<String, Set<SchemaDiff>> diffMap = mapper.readValue(node.get("diff_map").toString(), diffMapType);
            builder.mergeDiffMap(diffMap);
        }

        final Map<String, TableInfo> targetSchema = mapper.readValue(node.get("target_schema").toString(), targetSchemaType);
        return MigrationSet.builder()
                .targetSchema(targetSchema)
                .dbVersion(node.get("db_version").asInt())
                .build();
    }
}
