package com.fsryan.forsuredb.jacksonserialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fsryan.forsuredb.migration.MigrationSet;

import java.io.IOException;

public class MigrationSetSerializer extends StdSerializer<MigrationSet> {

    private final ObjectMapper mapper;

    protected MigrationSetSerializer(ObjectMapper mapper) {
        super(MigrationSet.class);
        this.mapper = mapper;
    }

    @Override
    public void serialize(MigrationSet object, JsonGenerator jGen, SerializerProvider provider) throws IOException {
        if (object == null) {
            jGen.writeNull();
            return;
        }

        jGen.writeStartObject();
        jGen.writeFieldName("ordered_migrations");
        mapper.writeValue(jGen, object.orderedMigrations());
        jGen.writeFieldName("target_schema");
        mapper.writeValue(jGen, object.targetSchema());
        jGen.writeNumberField("db_version", object.dbVersion());
        jGen.writeEndObject();
    }
}
