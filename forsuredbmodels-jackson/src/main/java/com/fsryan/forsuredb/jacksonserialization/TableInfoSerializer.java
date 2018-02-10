package com.fsryan.forsuredb.jacksonserialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fsryan.forsuredb.info.TableInfo;

import java.io.IOException;

public class TableInfoSerializer extends StdSerializer<TableInfo> {

    private final ObjectMapper mapper;

    protected TableInfoSerializer(ObjectMapper mapper) {
        super(TableInfo.class);
        this.mapper = mapper;
    }

    @Override
    public void serialize(TableInfo object, JsonGenerator jGen, SerializerProvider provider) throws IOException {
        if (object == null) {
            jGen.writeNull();
            return;
        }

        jGen.writeStartObject();
        jGen.writeFieldName("column_info_map");
        mapper.writeValue(jGen, object.columnMap());
        jGen.writeStringField("table_name", object.tableName());
        jGen.writeStringField("qualified_class_name", object.qualifiedClassName());
        if (object.staticDataAsset() != null) {
            jGen.writeStringField("static_data_asset", object.staticDataAsset());
        }
        if (object.staticDataRecordName() != null) {
            jGen.writeStringField("static_data_record_name", object.staticDataRecordName());
        }
        if (object.docStoreParameterization() != null) {
            jGen.writeStringField("doc_store_parameterization", object.docStoreParameterization());
        }
        if (object.primaryKey() != null) {
            jGen.writeFieldName("primary_key");
            mapper.writeValue(jGen, object.primaryKey());
        }
        if (object.primaryKeyOnConflict() != null) {
            jGen.writeStringField("primary_key_on_conflict", object.primaryKeyOnConflict());
        }
        if (object.foreignKeys() != null) {
            jGen.writeFieldName("foreign_keys");
            mapper.writeValue(jGen, object.foreignKeys());
        }
        jGen.writeEndObject();
    }
}
