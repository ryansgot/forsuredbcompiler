package com.fsryan.forsuredb.moshiserialization;

import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.migration.MigrationSet;
import com.fsryan.forsuredb.serialization.FSDbInfoSerializer;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import okio.Buffer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Set;

public class FSDbInfoMoshiSerializer implements FSDbInfoSerializer {

    private static final Moshi moshi = new Moshi.Builder()
            .add(new DbInfoAdapterFactory())
            .build();
    private static final JsonAdapter<Set<TableForeignKeyInfo>> tableForeignKeyInfoSetAdapter
            = adapterFrom(moshi, Types.newParameterizedType(Set.class, TableForeignKeyInfo.class));
    private static final JsonAdapter<Set<String>> stringSetAdapter
            = adapterFrom(moshi, Types.newParameterizedType(Set.class, String.class));

    @Override
    public MigrationSet deserializeMigrationSet(InputStream stream) {
        Buffer buffer = new Buffer();
        try {
            buffer.readFrom(stream);
            return moshi.adapter(MigrationSet.class).fromJson(JsonReader.of(buffer));
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @Override
    public MigrationSet deserializeMigrationSet(String json) {
        try {
            return moshi.adapter(MigrationSet.class).fromJson(json);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @Override
    public Set<TableForeignKeyInfo> deserializeForeignKeys(String json) {
        try {
            return tableForeignKeyInfoSetAdapter.fromJson(json);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public String serialize(MigrationSet migrationSet) {
        return moshi.adapter(MigrationSet.class).toJson(migrationSet);
    }

    @Override
    public Set<String> deserializeColumnNames(String stringSetJson) {
        try {
            return stringSetAdapter.fromJson(stringSetJson);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }


    static JsonAdapter adapterFrom(Moshi moshi, Type adapterType) {
        return moshi.adapter(adapterType);
    }
}
