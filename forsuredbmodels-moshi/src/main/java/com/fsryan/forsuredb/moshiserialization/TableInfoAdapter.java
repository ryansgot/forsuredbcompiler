package com.fsryan.forsuredb.moshiserialization;

import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.info.TableInfo;
import com.squareup.moshi.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static com.fsryan.forsuredb.moshiserialization.FSDbInfoMoshiSerializer.adapterFrom;

final class TableInfoAdapter extends JsonAdapter<TableInfo> {

    private static final JsonReader.Options OPTIONS = JsonReader.Options.of(
            "column_info_map",
            "table_name",
            "qualified_class_name",
            "static_data_asset",
            "static_data_record_name",
            "doc_store_parameterization",
            "primary_key",
            "primary_key_on_conflict",
            "foreign_keys"
    );
    private final JsonAdapter<Map<String, ColumnInfo>> columnMapAdapter;
    private final JsonAdapter<Set<String>> primaryKeyAdapter;
    private final JsonAdapter<Set<TableForeignKeyInfo>> foreignKeysAdapter;

    public TableInfoAdapter(Moshi moshi) {
        this.columnMapAdapter = adapterFrom(moshi, Types.newParameterizedType(Map.class, String.class, ColumnInfo.class)).nullSafe();
        this.primaryKeyAdapter = adapterFrom(moshi, Types.newParameterizedType(Set.class, String.class)).nullSafe();
        this.foreignKeysAdapter = adapterFrom(moshi, Types.newParameterizedType(Set.class, TableForeignKeyInfo.class)).nullSafe();
    }
    @Override
    public TableInfo fromJson(JsonReader reader) throws IOException {
        reader.beginObject();

        TableInfo.Builder builder = TableInfo.builder();
        while (reader.hasNext()) {
            switch (reader.selectName(OPTIONS)) {
                case 0: {
                    builder.addAllColumns(columnMapAdapter.fromJson(reader).values());
                    break;
                }
                case 1: {
                    builder.tableName(reader.nextString());
                    break;
                }
                case 2: {
                    builder.qualifiedClassName(reader.nextString());
                    break;
                }
                case 3: {
                    builder.staticDataAsset(reader.nextString());
                    break;
                }
                case 4: {
                    builder.staticDataRecordName(reader.nextString());
                    break;
                }
                case 5: {
                    builder.docStoreParameterization(reader.nextString());
                    break;
                }
                case 6: {
                    builder.resetPrimaryKey(primaryKeyAdapter.fromJson(reader));
                    break;
                }
                case 7: {
                    builder.primaryKeyOnConflict(reader.nextString());
                    break;
                }
                case 8: {
                    builder.addAllForeignKeys(foreignKeysAdapter.fromJson(reader));
                    break;
                }
                case -1: {
                    reader.nextName();
                    reader.skipValue();
                }
            }
        }
        reader.endObject();

        return builder.build();
    }
    @Override
    public void toJson(JsonWriter writer, @Nonnull TableInfo value) throws IOException {
        writer.beginObject();

        Map<String, ColumnInfo> columnMap = value.columnMap();
        writer.name("column_info_map");
        columnMapAdapter.toJson(writer, columnMap);

        writer.name("table_name");
        writer.value(value.tableName());

        writer.name("qualified_class_name");
        writer.value(value.qualifiedClassName());

        String staticDataAsset = value.staticDataAsset();
        if (staticDataAsset != null) {
            writer.name("static_data_asset");
            writer.value(value.staticDataAsset());
        }

        String staticDataRecordName = value.staticDataRecordName();
        if (staticDataRecordName != null) {
            writer.name("static_data_record_name");
            writer.value(value.staticDataRecordName());
        }

        String docStoreParameterization = value.docStoreParameterization();
        if (docStoreParameterization != null) {
            writer.name("doc_store_parameterization");
            writer.value(value.docStoreParameterization());
        }

        writer.name("primary_key");
        primaryKeyAdapter.toJson(writer, value.primaryKey());

        String primaryKeyOnConflict = value.primaryKeyOnConflict();
        if (primaryKeyOnConflict != null) {
            writer.name("primary_key_on_conflict");
            writer.value(primaryKeyOnConflict);
        }

        Set<TableForeignKeyInfo> foreignKeys = value.foreignKeys();
        if (foreignKeys != null) {
            writer.name("foreign_keys");
            foreignKeysAdapter.toJson(writer, foreignKeys);
        }

        writer.endObject();
    }
}