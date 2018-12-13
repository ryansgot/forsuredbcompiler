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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * <p>Contains a list of {@link Migration} as well as a
 * {@link #dbVersion() database version} version and {@link #targetSchema()}.
 * The target schema is represented differently based upon version:
 * <table>
 *   <th>
 *     <td>Version</td>
 *     <td>Version written to file</td>
 *     <td>Schema table key</td>
 *     <td>Schema column key</td>
 *     <td>Notes</td>
 *   </th>
 *   <tr>
 *     <td>1</td>
 *     <td>NO</td>
 *     <td>table name</td>
 *     <td>column name</td>
 *     <td>n/a</td>
 *   </tr>
 *   <tr>
 *     <td>2</td>
 *     <td>YES</td>
 *     <td>table java class name</td>
 *     <td>column method name</td>
 *     <td>Added {@link #diffMap()} and {@link #containsDiffs()}</td>
 *   </tr>
 * </table>
 *
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
@AutoValue
public abstract class MigrationSet implements Comparable<MigrationSet> {

    @AutoValue.Builder
    public static abstract class Builder {

        private final Map<String, Set<SchemaDiff>> diffMap = new HashMap<>();

        public abstract Builder orderedMigrations(@Nullable List<Migration> orderedMigrations); // ordered_migrations
        public abstract Builder targetSchema(Map<String, TableInfo> targetSchema);              // target_schema
        public abstract Builder dbVersion(int dbVersion);                                       // db_version
        public abstract Builder setVersion(int version);                                        // set_version

        public Builder addDiff(@Nonnull String tableClassName, SchemaDiff diff) {
            if (diff != null) {
                Set<SchemaDiff> current = diffMap.get(tableClassName);
                if (current == null) {
                    current = new HashSet<>();
                    diffMap.put(tableClassName, current);
                }
                current.add(diff);
            }
            return this;
        }

        public Builder mergeDiffs(@Nonnull String tableClassName, Set<SchemaDiff> diffs) {
            if (diffs != null) {
                Set<SchemaDiff> current = diffMap.get(tableClassName);
                if (current == null) {
                    current = new HashSet<>();
                    diffMap.put(tableClassName, current);
                }
                current.addAll(diffs);
            }
            return this;
        }

        public Builder mergeDiffMap(Map<String, Set<SchemaDiff>> diffMap) {
            if (diffMap != null) {
                for (Map.Entry<String, Set<SchemaDiff>> entry : diffMap.entrySet()) {
                    mergeDiffs(entry.getKey(), entry.getValue());
                }
            }
            return this;
        }

        public MigrationSet build() {
            if (orderedMigrations() != null && !diffMap.isEmpty()) {
                throw new IllegalStateException("Cannot have both migrations and diffs in the same " + MigrationSet.class);
            }
            return diffMap.isEmpty() ? autoBuild() : diffMap(diffMap).autoBuild();
        }

        @Nullable abstract List<Migration> orderedMigrations();
        abstract Builder diffMap(@Nullable Map<String, Set<SchemaDiff>> diffMap);               // diff_map
        abstract MigrationSet autoBuild();
    }

    public static Builder v2Builder() {
        return builder().setVersion(2);
    }

    public static Builder builder() {
        return new AutoValue_MigrationSet.Builder().setVersion(1);
    }

    @Nullable public abstract List<Migration> orderedMigrations();                              // ordered_migrations
    public abstract Map<String, TableInfo> targetSchema();                                      // target_schema
    public abstract int dbVersion();                                                            // db_version
    public abstract int setVersion();                                                           // set_version

    @Override
    public int compareTo(MigrationSet other) {
        final int v = dbVersion();
        final int otherV = other.dbVersion();
        return v < otherV ? -1 : (v == otherV) ? 0 : 1;
    }

    public boolean containsMigrations() {
        return orderedMigrations() != null && !orderedMigrations().isEmpty();
    }

    public boolean containsDiffs() {
        return diffMap() != null && !diffMap().isEmpty();
    }

    @Nullable
    public TableInfo findTableByName(@Nullable String tableName) {
        if (tableName == null) {
            return null;
        }
        for (TableInfo t : targetSchema().values()) {
            if (t.tableName().equals(tableName)) {
                return t;
            }
        }
        return null;
    }

    public Builder toBuilder() {
        return toAutoBuilder().mergeDiffMap(diffMap());
    }

    @Nullable public abstract Map<String, Set<SchemaDiff>> diffMap();                           // diff_map
    abstract Builder toAutoBuilder();
}
