package com.fsryan.forsuredb.moshiserialization;

import com.fsryan.forsuredb.info.TableIndexInfo;
import com.squareup.moshi.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.fsryan.forsuredb.moshiserialization.FSDbInfoMoshiSerializer.adapterFrom;

final class TableIndexInfoAdapter extends JsonAdapter<TableIndexInfo> {

    private static final JsonReader.Options OPTIONS = JsonReader.Options.of("unique", "columns", "column_sort_orders");

    private final JsonAdapter<List<String>> listStringAdapter;

    public TableIndexInfoAdapter(Moshi moshi) {
        listStringAdapter = adapterFrom(moshi, Types.newParameterizedType(List.class, String.class)).nullSafe();
    }

    @Override
    public TableIndexInfo fromJson(JsonReader reader) throws IOException {
        reader.beginObject();
        Boolean unique = null;
        List<String> cols = null;
        List<String> sorts = null;
        while (reader.hasNext()) {
            switch (reader.selectName(OPTIONS)) {
                case 0: {
                    unique = reader.nextBoolean();
                    break;
                }
                case 1: {
                    cols = listStringAdapter.fromJson(reader);
                    break;
                }
                case 2: {
                    sorts = listStringAdapter.fromJson(reader);
                    break;
                }
                case -1: {
                    reader.nextName();
                    reader.skipValue();
                }
            }
        }
        if (cols == null) {
            throw new IllegalStateException("TableIndexInfo must have a columns value");
        }
        if (sorts == null) {
            throw new IllegalStateException("TableIndexInfo must have a column_sort_orders value");
        }
        if (unique == null) {
            throw new IllegalStateException("TableIndexInfo must have a value for unique");
        }
        reader.endObject();
        return TableIndexInfo.create(unique, cols, sorts);
    }

    @Override
    public void toJson(JsonWriter writer, @Nonnull TableIndexInfo obj) throws IOException {
        List<String> cols = obj.columns();
        Map<String, String> sortOrderMap = obj.columnSortOrderMap();
        List<String> sorts = new ArrayList<>(cols.size());
        for (String col : cols) {
            sorts.add(sortOrderMap.get(col));
        }

        writer.beginObject();
        writer.name("unique");
        writer.value(obj.unique());
        writer.name("columns");
        listStringAdapter.toJson(writer, cols);
        writer.name("column_sort_orders");
        listStringAdapter.toJson(writer, sorts);
        writer.endObject();
    }
}