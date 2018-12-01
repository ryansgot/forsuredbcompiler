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
 * <p>Contains a list of {@link Migration} as well as a
 * {@link #dbVersion() database version} version and {@link #targetSchema()}.
 * The target schema is represented differently based upon version:
 * <table>
 *   <th>
 *     <td>Version</td>
 *     <td>Version written to file</td>
 *     <td>Schema table key</td>
 *     <td>Schema column key</td>
 *   </th>
 *   <tr>
 *     <td>1</td>
 *     <td>NO</td>
 *     <td>table name</td>
 *     <td>column name</td>
 *   </tr>
 *   <tr>
 *     <td>2</td>
 *     <td>YES</td>
 *     <td>table java class name</td>
 *     <td>column method name</td>
 *   </tr>
 * </table>
 *
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
@AutoValue
public abstract class MigrationSet implements Comparable<MigrationSet> {

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder orderedMigrations(List<Migration> orderedMigrations);   // ordered_migrations
        public abstract Builder targetSchema(Map<String, TableInfo> targetSchema);      // target_schema
        public abstract Builder dbVersion(int dbVersion);                               // db_version
        public abstract Builder setVersion(int version);                                // set_version
        public abstract MigrationSet build();
    }

    public static Builder v2Builder() {
        return builder().setVersion(2);
    }

    public static Builder builder() {
        return new AutoValue_MigrationSet.Builder().setVersion(1);
    }

    public abstract List<Migration> orderedMigrations();                                // ordered_migrations
    public abstract Map<String, TableInfo> targetSchema();                              // target_schema
    public abstract int dbVersion();                                                    // db_version
    public abstract int setVersion();                                                   // set_version

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
