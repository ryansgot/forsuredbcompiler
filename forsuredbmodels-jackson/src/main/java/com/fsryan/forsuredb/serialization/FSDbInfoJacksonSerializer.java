package com.fsryan.forsuredb.serialization;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.migration.MigrationSet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class FSDbInfoJacksonSerializer implements FSDbInfoSerializer {

    private final ObjectMapper mapper;

    public FSDbInfoJacksonSerializer() {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("DbInfoSerializer", new Version(0, 10,0, null, null, null));
        module.addSerializer(TableForeignKeyInfo.class, new TableForeignKeyInfoSerializer(mapper));
        module.addDeserializer(TableForeignKeyInfo.class, new TableForeignKeyInfoDeserializer(mapper));
        mapper.registerModule(module);
    }

    @Override
    public MigrationSet deserializeMigrationSet(InputStream stream) {
        return null;
    }

    @Override
    public MigrationSet deserializeMigrationSet(String json) {
        return null;
    }

    @Override
    public Set<TableForeignKeyInfo> deserializeForeignKeys(String json) {
        try {
            return mapper.readerFor(new TypeReference<Set<TableForeignKeyInfo>>() {}).readValue(json);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @Override
    public String serialize(MigrationSet migrationSet) {
        return null;
    }
}
