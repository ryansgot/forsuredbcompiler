package com.fsryan.forsuredb.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fsryan.forsuredb.info.TableForeignKeyInfo;

import java.io.IOException;

public class TableForeignKeyInfoSerializer extends StdSerializer<TableForeignKeyInfo> {

    private final ObjectMapper mapper;

    public TableForeignKeyInfoSerializer(ObjectMapper mapper) {
        this(mapper, null);
    }

    public TableForeignKeyInfoSerializer(ObjectMapper mapper, Class<TableForeignKeyInfo> t) {
        super(t);
        this.mapper = mapper;
    }

    @Override
    public void serialize(TableForeignKeyInfo object, JsonGenerator jGen, SerializerProvider provider) throws IOException, JsonProcessingException {
        if (object == null) {
            jGen.writeNull();
            return;
        }

        jGen.writeStartObject();
        jGen.writeStringField("foreign_table_api_class_name", object.foreignTableApiClassName());
        jGen.writeStringField("foreign_table_name", object.foreignTableName());
        jGen.writeFieldName("local_to_foreign_column_map");
        mapper.writeValue(jGen, object.localToForeignColumnMap());
        jGen.writeStringField("update_action", object.updateChangeAction());
        jGen.writeStringField("delete_action", object.deleteChangeAction());
        jGen.writeEndObject();
    }
}
