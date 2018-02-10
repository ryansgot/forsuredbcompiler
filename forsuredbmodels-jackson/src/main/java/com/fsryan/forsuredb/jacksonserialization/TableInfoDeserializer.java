package com.fsryan.forsuredb.jacksonserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.info.TableInfo;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static com.fsryan.forsuredb.jacksonserialization.FSDbInfoJacksonSerializer.PRIMARY_KEY_TYPE;
import static com.fsryan.forsuredb.jacksonserialization.FSDbInfoJacksonSerializer.TABLE_FOREIGN_KEY_INFO_TYPE;

public class TableInfoDeserializer extends StdDeserializer<TableInfo> {

    private static final TypeReference<Map<String, ColumnInfo>> columnInfoMapType = new TypeReference<Map<String, ColumnInfo>>() {};

    private final ObjectMapper mapper;

    public TableInfoDeserializer(ObjectMapper mapper) {
        super(TableInfo.class);
        this.mapper = mapper;
    }

    @Override
    public TableInfo deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        final JsonNode node = jp.getCodec().readTree(jp);
        final Map<String, ColumnInfo> columnInfoMap = mapper.readValue(node.get("column_info_map").toString(), columnInfoMapType);
        final String tableName = node.get("table_name").asText();
        final String qualifiedClassName = node.get("qualified_class_name").asText();
        final String staticDataAsset = node.has("static_data_asset")
                ? node.get("static_data_asset").asText()
                : null;
        final String staticDataRecordName = node.has("static_data_record_name")
                ? node.get("static_data_record_name").asText()
                : null;
        final String docStoreParameterization = node.has("doc_store_parameterization")
                ? node.get("doc_store_parameterization").asText()
                : null;
        final Set<String> primaryKey = node.has("primary_key")
                ? (Set<String>) mapper.readValue(node.get("primary_key").toString(), PRIMARY_KEY_TYPE)
                : null;
        final String primaryKeyOnConflict = node.has("primary_key_on_conflict")
                ? node.get("primary_key_on_conflict").asText()
                : null;
        final Set<TableForeignKeyInfo> foreignKeys = node.has("foreign_keys")
                ? (Set<TableForeignKeyInfo>) mapper.readValue(node.get("foreign_keys").toString(), TABLE_FOREIGN_KEY_INFO_TYPE)
                : null;
        return TableInfo.builder()
                .columnMap(columnInfoMap)
                .tableName(tableName)
                .qualifiedClassName(qualifiedClassName)
                .staticDataAsset(staticDataAsset)
                .staticDataRecordName(staticDataRecordName)
                .docStoreParameterization(docStoreParameterization)
                .primaryKey(primaryKey)
                .primaryKeyOnConflict(primaryKeyOnConflict)
                .foreignKeys(foreignKeys)
                .build();
    }
}
