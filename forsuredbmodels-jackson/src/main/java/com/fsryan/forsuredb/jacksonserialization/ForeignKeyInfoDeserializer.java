package com.fsryan.forsuredb.jacksonserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fsryan.forsuredb.info.ForeignKeyInfo;

import java.io.IOException;

@Deprecated
public class ForeignKeyInfoDeserializer extends StdDeserializer<ForeignKeyInfo> {

    public ForeignKeyInfoDeserializer() {
        super(ForeignKeyInfo.class);
    }

    @Override
    public ForeignKeyInfo deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        final JsonNode node = jp.getCodec().readTree(jp);
        final String updateAction = node.get("update_action").asText();
        final String deleteAction = node.get("delete_action").asText();
        final String foreignTableName = node.get("foreign_table_name").asText();
        final String foreignColumnName = node.get("foreign_column_name").asText();
        final String foreignApiClassName = node.get("foreign_api_class_name").asText();
        return ForeignKeyInfo.builder()
                .updateAction(updateAction)
                .deleteAction(deleteAction)
                .tableName(foreignTableName)
                .columnName(foreignColumnName)
                .apiClassName(foreignApiClassName)
                .build();
    }
}
