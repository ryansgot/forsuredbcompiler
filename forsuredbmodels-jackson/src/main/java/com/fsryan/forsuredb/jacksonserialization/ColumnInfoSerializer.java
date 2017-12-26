package com.fsryan.forsuredb.jacksonserialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.ForeignKeyInfo;

import java.io.IOException;

public class ColumnInfoSerializer extends StdSerializer<ColumnInfo> {

    public ColumnInfoSerializer() {
        super(ColumnInfo.class);
    }

    @Override
    public void serialize(ColumnInfo object, JsonGenerator jGen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        if (object == null) {
            jGen.writeNull();
            return;
        }

        jGen.writeStartObject();
        jGen.writeStringField("method_name", object.methodName());
        jGen.writeStringField("column_name", object.columnName());
        jGen.writeStringField("column_type", object.qualifiedType());
        jGen.writeBooleanField("index", object.index());
        if (object.defaultValue() != null) {
            jGen.writeStringField("default_value", object.defaultValue());
        }
        jGen.writeBooleanField("unique", object.unique());
        jGen.writeBooleanField("primary_key", object.primaryKey());
        if (object.foreignKeyInfo() != null) {
            jGen.writeObjectField("foreign_key_info", object.foreignKeyInfo());
        }
        jGen.writeBooleanField("searchable", object.searchable());
        jGen.writeBooleanField("orderable", object.orderable());
        jGen.writeEndObject();
    }
}