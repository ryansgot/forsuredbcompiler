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
import java.util.List;
import java.util.Map;

public class TableIndexInfoDeserializer extends StdDeserializer<TableIndexInfo> {

    private static final TypeReference<List<String>> stringListType = new TypeReference<List<String>>() {};

    private final ObjectMapper mapper;

    public TableIndexInfoDeserializer(ObjectMapper mapper) {
        super(TableIndexInfo.class);
        this.mapper = mapper;
    }

    @Override
    public TableIndexInfo deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        final JsonNode node = jp.getCodec().readTree(jp);
        final boolean unique = node.get("unique").asBoolean();
        final String colsJson = node.get("columns").toString();
        final List<String> cols = mapper.readValue(colsJson, stringListType);
        final String sortsJson = node.get("column_sort_orders").toString();
        final List<String> sorts = mapper.readValue(sortsJson, stringListType);
        return TableIndexInfo.create(unique, cols, sorts);
    }
}
