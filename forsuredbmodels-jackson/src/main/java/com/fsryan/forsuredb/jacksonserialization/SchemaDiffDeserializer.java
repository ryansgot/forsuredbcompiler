package com.fsryan.forsuredb.jacksonserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fsryan.forsuredb.migration.Migration;
import com.fsryan.forsuredb.migration.SchemaDiff;

import java.io.IOException;
import java.util.Map;

public class SchemaDiffDeserializer extends StdDeserializer<SchemaDiff> {

    private static final TypeReference<Map<String, String>> attributesType = new TypeReference<Map<String, String>>() {};

    private final ObjectMapper mapper;

    protected SchemaDiffDeserializer(ObjectMapper mapper) {
        super(SchemaDiff.class);
        this.mapper = mapper;
    }

    @Override
    public SchemaDiff deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonNode node = jp.getCodec().readTree(jp);

        SchemaDiff.Builder builder = SchemaDiff.builder()
                .type(node.get("type").asInt())
                .replaceSubType(node.get("sub_type").asLong())
//                .category(node.get("category").asInt())
                .tableName(node.get("table_name").asText());
        Map<String, String> attributes = mapper.readValue(node.get("attributes").toString(), attributesType);
        return builder.addAllAttributes(attributes).build();
    }
}
