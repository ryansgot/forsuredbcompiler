package com.fsryan.forsuredb.jacksonserialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fsryan.forsuredb.info.TableIndexInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TableIndexInfoSerializer extends StdSerializer<TableIndexInfo> {

    private final ObjectMapper mapper;

    public TableIndexInfoSerializer(ObjectMapper mapper) {
        super(TableIndexInfo.class);
        this.mapper = mapper;
    }

    @Override
    public void serialize(TableIndexInfo obj, JsonGenerator jGen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        if (obj == null) {
            jGen.writeNull();
            return;
        }

        List<String> cols = obj.columns();
        Map<String, String> sortOrderMap = obj.columnSortOrderMap();
        List<String> sorts = new ArrayList<>(cols.size());
        for (String col : cols) {
            sorts.add(sortOrderMap.get(col));
        }

        jGen.writeStartObject();
        jGen.writeBooleanField("unique", obj.unique());
        jGen.writeFieldName("columns");
        mapper.writeValue(jGen, cols);
        jGen.writeFieldName("column_sort_orders");
        mapper.writeValue(jGen, sorts);
        jGen.writeEndObject();
    }
}
