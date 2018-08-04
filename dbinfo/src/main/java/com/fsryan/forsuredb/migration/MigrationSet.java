/*
    forsuredb-dbinfo, value classes for the forsuredb project

    Copyright 2017 Ryan Scott

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
package com.fsryan.forsuredb.migration;

import com.fsryan.forsuredb.info.TableInfo;
import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.Map;

/**
 * <p>Contains an ordered set of {@link Migration} as well as a database version and
 * target schema (modeled as a map, table name -&gt; {@link TableInfo})
 *
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
@AutoValue
public abstract class MigrationSet implements Comparable<MigrationSet> {

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder orderedMigrations(List<Migration> orderedMigrations);    // ordered_migrations
        public abstract Builder targetSchema(Map<String, TableInfo> targetSchema);  // target_schema
        public abstract Builder dbVersion(int dbVersion);    // db_version
        public abstract MigrationSet build();
    }

    public static Builder builder() {
        return new AutoValue_MigrationSet.Builder();
    }

    public abstract List<Migration> orderedMigrations();    // ordered_migrations
    public abstract Map<String, TableInfo> targetSchema();  // target_schema
    public abstract int dbVersion();    // db_version

    @Override
    public int compareTo(MigrationSet other) {
        final int v = dbVersion();
        final int otherV = other.dbVersion();
        return v < otherV ? -1 : (v == otherV) ? 0 : 1;
    }

    public boolean containsMigrations() {
        return orderedMigrations() != null && !orderedMigrations().isEmpty();
    }
}
