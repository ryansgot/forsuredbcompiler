package com.fsryan.forsuredb.jacksonserialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fsryan.forsuredb.info.TableIndexInfo;

import java.io.IOException;

public class TableIndexInfoSerializer extends StdSerializer<TableIndexInfo> {

    private final ObjectMapper mapper;

    public TableIndexInfoSerializer(ObjectMapper mapper) {
        super(TableIndexInfo.class);
        this.mapper = mapper;
    }

    @Override
    public void serialize(TableIndexInfo object, JsonGenerator jGen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        if (object == null) {
            jGen.writeNull();
            return;
        }

        jGen.writeStartObject();
        jGen.writeFieldName("column_sort_order_map");
        mapper.writeValue(jGen, object.columnSortOrderMap());
        jGen.writeBooleanField("unique", object.unique());
        jGen.writeEndObject();
    }
}
