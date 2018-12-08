package com.fsryan.forsuredb.jacksonserialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fsryan.forsuredb.migration.Migration;
import com.fsryan.forsuredb.migration.SchemaDiff;

import java.io.IOException;

public class SchemaDiffSerializer extends StdSerializer<SchemaDiff> {

    private final ObjectMapper mapper;

    protected SchemaDiffSerializer(ObjectMapper mapper) {
        super(SchemaDiff.class);
        this.mapper = mapper;
    }

    @Override
    public void serialize(SchemaDiff obj, JsonGenerator jGen, SerializerProvider provider) throws IOException {
        if (obj == null) {
            jGen.writeNull();
            return;
        }

        jGen.writeStartObject();
        jGen.writeNumberField("type", obj.type());
        jGen.writeNumberField("sub_type", obj.subType());
//        jGen.writeNumberField("category", obj.category());
        jGen.writeStringField("table_name", obj.tableName());
        jGen.writeFieldName("attributes");
        mapper.writeValue(jGen, obj.attributes());
        jGen.writeEndObject();
    }
}
