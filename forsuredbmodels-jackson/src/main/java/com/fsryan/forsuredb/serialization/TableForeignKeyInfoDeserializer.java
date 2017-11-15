package com.fsryan.forsuredb.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fsryan.forsuredb.info.TableForeignKeyInfo;

import java.io.IOException;
import java.util.Map;

public class TableForeignKeyInfoDeserializer extends StdDeserializer<TableForeignKeyInfo> {

    private static final TypeReference<Map<String, String>> localToForeignKeyColumnMapType = new TypeReference<Map<String, String>>() {};

    private final ObjectMapper mapper;

    public TableForeignKeyInfoDeserializer(ObjectMapper mapper) {
        this(mapper, null);
    }

    public TableForeignKeyInfoDeserializer(ObjectMapper mapper, Class<?> t) {
        super(t);
        this.mapper = mapper;
    }

    @Override
    public TableForeignKeyInfo deserialize(JsonParser jp, DeserializationContext ctxt)  throws IOException, JsonProcessingException {
        final JsonNode node = jp.getCodec().readTree(jp);
        final String foreignApiClassName = node.get("foreign_table_api_class_name").asText();
        final String foreignTableName = node.get("foreign_table_name").asText();
        final Map<String, String> localToForeignColumnMap = mapper.readValue(node.get("local_to_foreign_column_map").toString(), localToForeignKeyColumnMapType);
        final String updateAction = node.get("update_action").asText();
        final String deleteAction = node.get("delete_action").asText();
        return TableForeignKeyInfo.builder()
                .foreignTableApiClassName(foreignApiClassName)
                .foreignTableName(foreignTableName)
                .localToForeignColumnMap(localToForeignColumnMap)
                .updateChangeAction(updateAction)
                .deleteChangeAction(deleteAction)
                .build();
    }
}
