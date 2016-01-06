package com.forsuredb.migration;

import com.forsuredb.annotationprocessor.info.TableInfo;
import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(builderClassName = "Builder")
public class MigrationSet {
    @Getter @SerializedName("ordered_migrations") private List<Migration> orderedMigrations;
    @Getter @SerializedName("target_schema") private Map<String, TableInfo> targetSchema;
    @Getter @SerializedName("db_version") private int dbVersion;

    public boolean containsMigrations() {
        return orderedMigrations != null && orderedMigrations.size() > 0;
    }
}
