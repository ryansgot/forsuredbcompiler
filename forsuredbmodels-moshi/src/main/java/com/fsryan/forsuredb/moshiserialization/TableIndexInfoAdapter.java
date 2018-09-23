package com.fsryan.forsuredb.moshiserialization;

import com.fsryan.forsuredb.info.TableIndexInfo;
import com.squareup.moshi.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;

import static com.fsryan.forsuredb.moshiserialization.FSDbInfoMoshiSerializer.adapterFrom;

final class TableIndexInfoAdapter extends JsonAdapter<TableIndexInfo> {

    private static final JsonReader.Options OPTIONS = JsonReader.Options.of("column_sort_order_map", "unique");

    private final JsonAdapter<Map<String, String>> string2StringMapAdapter;

    public TableIndexInfoAdapter(Moshi moshi) {
        string2StringMapAdapter = adapterFrom(moshi, Types.newParameterizedType(Map.class, String.class, String.class)).nullSafe();
    }

    @Override
    public TableIndexInfo fromJson(JsonReader reader) throws IOException {
        reader.beginObject();
        Map<String, String> columnSortOrderMap = null;
        Boolean unique = null;
        while (reader.hasNext()) {
            switch (reader.selectName(OPTIONS)) {
                case 0: {
                    columnSortOrderMap = string2StringMapAdapter.fromJson(reader);
                    break;
                }
                case 1: {
                    unique = reader.nextBoolean();
                    break;
                }
                case -1: {
                    reader.nextName();
                    reader.skipValue();
                }
            }
        }
        if (columnSortOrderMap == null) {
            throw new IllegalStateException("Cannot build TableIndexInfo with null column_sort_order_map");
        }
        if (unique == null) {
            throw new IllegalStateException("Cannot build TableIndexInfo without unique");
        }
        reader.endObject();
        return TableIndexInfo.create(columnSortOrderMap, unique);
    }

    @Override
    public void toJson(JsonWriter writer, @Nonnull TableIndexInfo value) throws IOException {
        writer.beginObject();

        writer.name("column_sort_order_map");
        string2StringMapAdapter.toJson(writer, value.columnSortOrderMap());

        writer.name("unique");
        writer.value(value.unique());

        writer.endObject();
    }
}