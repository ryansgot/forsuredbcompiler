package com.fsryan.forsuredb.jacksonserialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fsryan.forsuredb.migration.Migration;

import java.io.IOException;

public class MigrationSerializer extends StdSerializer<Migration> {

    private final ObjectMapper mapper;

    protected MigrationSerializer(ObjectMapper mapper) {
        super(Migration.class);
        this.mapper = mapper;
    }

    @Override
    public void serialize(Migration object, JsonGenerator jGen, SerializerProvider provider) throws IOException {
        if (object == null) {
            jGen.writeNull();
            return;
        }

        jGen.writeStartObject();
        jGen.writeStringField("table_name", object.tableName());
        if (object.columnName() != null) {
            jGen.writeStringField("column_name", object.columnName());
        }
        jGen.writeStringField("migration_type", object.type().name());
        if (object.hasExtras()) {
            mapper.writeValue(jGen, object.extras());
        }
        jGen.writeEndObject();
    }
}
