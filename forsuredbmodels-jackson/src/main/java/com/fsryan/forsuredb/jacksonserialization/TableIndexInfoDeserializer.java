package com.fsryan.forsuredb.jacksonserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fsryan.forsuredb.info.TableIndexInfo;

import java.io.IOException;
import java.util.Map;

public class TableIndexInfoDeserializer extends StdDeserializer<TableIndexInfo> {

    private static final TypeReference<Map<String, String>> columnSortOrderMapType = new TypeReference<Map<String, String>>() {};

    private final ObjectMapper mapper;

    public TableIndexInfoDeserializer(ObjectMapper mapper) {
        super(TableIndexInfo.class);
        this.mapper = mapper;
    }

    @Override
    public TableIndexInfo deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        final JsonNode node = jp.getCodec().readTree(jp);
        final String mapJson = node.get("column_sort_order_map").toString();
        final Map<String, String> columnSortOrderMap = mapper.readValue(mapJson, columnSortOrderMapType);
        final boolean unique = node.get("unique").asBoolean();
        return TableIndexInfo.create(columnSortOrderMap, unique);
    }
}
