package com.fsryan.forsuredb.jacksonserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.ForeignKeyInfo;

import java.io.IOException;

public class ColumnInfoDeserializer extends StdDeserializer<ColumnInfo> {

    private static TypeReference<ForeignKeyInfo> legacyForeignKeyInfoType = new TypeReference<ForeignKeyInfo>() {};

    private final ObjectMapper mapper;

    public ColumnInfoDeserializer(ObjectMapper mapper) {
        super(ColumnInfo.class);
        this.mapper = mapper;
    }

    @Override
    public ColumnInfo deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        final JsonNode node = jp.getCodec().readTree(jp);
        final String methodName = node.get("method_name").asText();
        final String columnName = node.get("column_name").asText();
        final String columnType = node.get("column_type").asText();
        final boolean index = node.has("index") && node.get("index").asBoolean();
        final String defaultValue = node.has("default_value") ? node.get("default_value").asText() : null;
        final boolean unique = node.has("unique") && node.get("unique").asBoolean();
        final boolean primaryKey = node.has("primary_key") && node.get("primary_key").asBoolean();
        final ForeignKeyInfo foreignKeyInfo = node.has("foreign_key_info")
                ? (ForeignKeyInfo) mapper.readValue(node.get("foreign_key_info").toString(), legacyForeignKeyInfoType)
                : null;
        final boolean searchable = node.has("searchable") && node.get("searchable").asBoolean();
        final boolean orderable = node.has("orderable") && node.get("orderable").asBoolean();
        return ColumnInfo.builder()
                .methodName(methodName)
                .columnName(columnName)
                .qualifiedType(columnType)
                .index(index)
                .defaultValue(defaultValue)
                .unique(unique)
                .primaryKey(primaryKey)
                .foreignKeyInfo(foreignKeyInfo)
                .searchable(searchable)
                .orderable(orderable)
                .build();
    }
}
