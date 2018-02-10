package com.fsryan.forsuredb.jacksonserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fsryan.forsuredb.migration.Migration;

import java.io.IOException;
import java.util.Map;

public class MigrationDeserializer extends StdDeserializer<Migration> {

    private static final TypeReference<Map<String, String>> extrasType = new TypeReference<Map<String, String>>() {};

    private final ObjectMapper mapper;

    protected MigrationDeserializer(ObjectMapper mapper) {
        super(Migration.class);
        this.mapper = mapper;
    }

    @Override
    public Migration deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonNode node = jp.getCodec().readTree(jp);
        final String tableName = node.get("table_name").asText();
        final String columnName = node.has("column_name")
                ? node.get("column_name").asText()
                : null;
        final Migration.Type type = Migration.Type.from(node.get("migration_type").asText());
        final Map<String, String> extras = node.has("extras")
                ? (Map<String, String>) mapper.readValue(node.get("extras").toString(), extrasType)
                : null;
        return Migration.builder()
                .tableName(tableName)
                .columnName(columnName)
                .type(type)
                .extras(extras)
                .build();
    }
}
