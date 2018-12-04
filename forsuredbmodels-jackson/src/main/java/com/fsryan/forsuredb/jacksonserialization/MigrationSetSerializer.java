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
    public void serialize(MigrationSet obj, JsonGenerator jGen, SerializerProvider provider) throws IOException {
        if (obj == null) {
            jGen.writeNull();
            return;
        }

        jGen.writeStartObject();
        if (obj.containsMigrations()) {
            jGen.writeFieldName("ordered_migrations");
            mapper.writeValue(jGen, obj.orderedMigrations());
        }
        if (obj.containsDiffs()) {
            jGen.writeFieldName("diff_map");
            mapper.writeValue(jGen, obj.diffMap());
        }
        jGen.writeFieldName("target_schema");
        mapper.writeValue(jGen, obj.targetSchema());
        jGen.writeNumberField("db_version", obj.dbVersion());
        jGen.writeNumberField("set_version", obj.setVersion());
        jGen.writeEndObject();
    }
}
