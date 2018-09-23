package com.fsryan.forsuredb.jacksonserialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fsryan.forsuredb.info.*;
import com.fsryan.forsuredb.migration.Migration;
import com.fsryan.forsuredb.migration.MigrationSet;
import com.fsryan.forsuredb.serialization.FSDbInfoSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class FSDbInfoJacksonSerializer implements FSDbInfoSerializer {

    static final TypeReference<Set<TableForeignKeyInfo>> TABLE_FOREIGN_KEY_INFO_SET_TYPE = new TypeReference<Set<TableForeignKeyInfo>>() {};
    static final TypeReference<Set<TableIndexInfo>> TABLE_INDEX_INFO_SET_TYPE = new TypeReference<Set<TableIndexInfo>>() {};
    static final TypeReference<Set<String>> PRIMARY_KEY_TYPE = new TypeReference<Set<String>>() {};
    private static final TypeReference<MigrationSet> migrationSetType = new TypeReference<MigrationSet>() {};

    private final ObjectMapper mapper;

    public FSDbInfoJacksonSerializer() {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("DbInfoSerializer", new Version(0, 14,0, null, null, null));
        module.addSerializer(TableForeignKeyInfo.class, new TableForeignKeyInfoSerializer(mapper));
        module.addDeserializer(TableForeignKeyInfo.class, new TableForeignKeyInfoDeserializer(mapper));
        module.addSerializer(ForeignKeyInfo.class, new ForeignKeyInfoSerializer());
        module.addDeserializer(ForeignKeyInfo.class, new ForeignKeyInfoDeserializer());
        module.addSerializer(ColumnInfo.class, new ColumnInfoSerializer());
        module.addDeserializer(ColumnInfo.class, new ColumnInfoDeserializer(mapper));
        module.addSerializer(TableInfo.class, new TableInfoSerializer(mapper));
        module.addDeserializer(TableInfo.class, new TableInfoDeserializer(mapper));
        module.addSerializer(Migration.class, new MigrationSerializer(mapper));
        module.addDeserializer(Migration.class, new MigrationDeserializer(mapper));
        module.addSerializer(MigrationSet.class, new MigrationSetSerializer(mapper));
        module.addDeserializer(MigrationSet.class, new MigrationSetDeserializer(mapper));
        module.addSerializer(TableIndexInfo.class, new TableIndexInfoSerializer(mapper));
        module.addDeserializer(TableIndexInfo.class, new TableIndexInfoDeserializer(mapper));
        mapper.registerModule(module);
    }

    @Override
    public MigrationSet deserializeMigrationSet(InputStream stream) {
        try {
            return mapper.readerFor(migrationSetType).readValue(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MigrationSet deserializeMigrationSet(String json) {
        try {
            return mapper.readerFor(migrationSetType).readValue(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<TableForeignKeyInfo> deserializeForeignKeys(String json) {
        try {
            return mapper.readerFor(TABLE_FOREIGN_KEY_INFO_SET_TYPE).readValue(json);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @Override
    public Set<TableIndexInfo> deserializeIndices(String json) {
        try {
            return mapper.readerFor(TABLE_INDEX_INFO_SET_TYPE).readValue(json);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @Override
    public String serialize(MigrationSet migrationSet) {
        try {
            return mapper.writerFor(migrationSetType).writeValueAsString(migrationSet);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<String> deserializeColumnNames(String stringSetJson) {
        try {
            return mapper.readerFor(PRIMARY_KEY_TYPE).readValue(stringSetJson);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
