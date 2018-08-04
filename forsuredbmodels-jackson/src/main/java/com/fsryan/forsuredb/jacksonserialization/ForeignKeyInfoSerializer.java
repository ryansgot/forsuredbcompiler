package com.fsryan.forsuredb.jacksonserialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fsryan.forsuredb.info.ForeignKeyInfo;

import java.io.IOException;

@Deprecated
public class ForeignKeyInfoSerializer extends StdSerializer<ForeignKeyInfo> {

    public ForeignKeyInfoSerializer() {
        super(ForeignKeyInfo.class);
    }

    @Override
    public void serialize(ForeignKeyInfo object, JsonGenerator jGen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        if (object == null) {
            jGen.writeNull();
            return;
        }

        jGen.writeStartObject();
        jGen.writeStringField("update_action", object.updateAction());
        jGen.writeStringField("delete_action", object.deleteAction());
        jGen.writeStringField("foreign_table_name", object.tableName());
        jGen.writeStringField("foreign_column_name", object.columnName());
        jGen.writeStringField("foreign_api_class_name", object.apiClassName());
        jGen.writeEndObject();
    }
}