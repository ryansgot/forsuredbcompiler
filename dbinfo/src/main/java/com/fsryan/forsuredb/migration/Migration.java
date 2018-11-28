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

import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.serialization.FSDbInfoSerializer;
import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// TODO: rethink this. I think these can be simplified and more of the work can be imposed upon the DBMS integration library
// You should not need so many different values.
// Additionally, order need not be imposed on migrations at this level

@AutoValue
public abstract class Migration implements Comparable<Migration> {

    // TODO: these should just be strings.
    public enum Type {
        CREATE_TABLE(0),

        // migrations typically requiring recreation of a table
        UPDATE_PRIMARY_KEY(5),      // SQLite requires recreation of a table for this to work. MySQL does not.
        UPDATE_FOREIGN_KEYS(5),     // SQLite requires recreation of a table for this to work. MySQL does not.
        CHANGE_DEFAULT_VALUE(6),    // SQLite requires recreation of a table for this to work. MySQL does not.

        ALTER_TABLE_ADD_COLUMN(10),
        ALTER_TABLE_ADD_UNIQUE(10),
        /**
         * <p>Foreign keys may or may not be marked on columns. If you use this, I'm not sure what will happen. It is
         * only around for compatibility with previous versions.
         * @deprecated since 0.11.0 and will be removed in 1.0.0
         */
        @Deprecated
        ADD_FOREIGN_KEY_REFERENCE(19),
        ADD_UNIQUE_INDEX(30),
        ADD_INDEX(30),
        MAKE_COLUMN_UNIQUE(30),
        CREATE_TEMP_TABLE_FROM_EXISTING(40),
        DROP_TABLE(100);

        private int priority;

        Type(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }

        public static Type from(String name) {
            for (Type type : Type.values()) {
                if (type.name().equals(name)) {
                    return type;
                }
            }
            return null;
        }
    }

    @AutoValue.Builder
    public static abstract class Builder {

        private final Map<String, String> extras = new HashMap<>();

        public abstract Builder tableName(String tableName);                // table_name
        public abstract Builder columnName(String columnName);              // column_name
        public abstract Builder type(Type type);                            // migration_type

        public Builder putExtra(@Nonnull String key, @Nonnull String val) {
            extras.put(key, val);
            return this;
        }

        public Builder putAllExtras(@Nonnull Map<String, String> extras) {
            this.extras.putAll(extras);
            return this;
        }

        public Migration build() {
            return extras(extras).autoBuild();
        }

        abstract Builder extras(@Nonnull Map<String, String> extras);      // extras
        abstract Migration autoBuild();
    }

    public static Builder builder() {
        return new AutoValue_Migration.Builder();
    }

    /**
     * <p>A utility function to aid DBMS Integrator libraries with respect to getting the extra
     * {@link TableForeignKeyInfo} information off of a {@link Migration}
     * @param migration the {@link Migration}
     * @param serializer the {@link FSDbInfoSerializer} capable of deserializing the information
     * @return a set of all current {@link TableForeignKeyInfo} describing the current foreign
     * keys; an empty set if there are no extras or no current_foreign_keys extra
     */
    @Nonnull
    public static Set<TableForeignKeyInfo> foreignKeysOf(@Nonnull Migration migration, @Nonnull FSDbInfoSerializer serializer) {
        if (!migration.hasExtras()) {
            return Collections.emptySet();
        }

        final String json = migration.extras().get("current_foreign_keys");
        return json == null
                ? Collections.<TableForeignKeyInfo>emptySet()
                : serializer.deserializeForeignKeys(json);
    }

    /**
     * <p>A utility function to aid DBMS Integrator libraries with respect to getting the extra
     * existing column name info off of a {@link Migration}
     * @param migration the {@link Migration}
     * @param serializer
     * @return a set of all current column namess; an empty set if there are no extras or no
     * existing_column_names extra
     */
    @Nonnull
    public static Set<String> existingColumnNamesOf(@Nonnull Migration migration, @Nonnull FSDbInfoSerializer serializer) {
        if (!migration.hasExtras()) {
            return Collections.emptySet();
        }

        final String json = migration.extras().get("existing_column_names");
        return json == null
                ? Collections.<String>emptySet()
                : serializer.deserializeColumnNames(json);
    }

    public static Migration create(String tableName, String columnName, Type type) {
        return builder()
                .tableName(tableName)
                .columnName(columnName)
                .type(type)
                .build();
    }

    public abstract String tableName();                                             // table_name
    @Nullable public abstract String columnName();                                  // column_name
    public abstract Type type();                                                    // migration_type
    @Nonnull public abstract Map<String, String> extras();                          // extras

    public boolean hasExtras() {
        return !extras().isEmpty();
    }

    @Override
    public int compareTo(Migration o) {
        if (o == null || this.type() == null) {
            return -1;
        }
        if (type() == null) {
            return 1;
        }

        final int priorityDiff = type().getPriority() - o.type().getPriority();
        // Unambiguously determine order based upon comparing table or column name in the case of equal priority
        return priorityDiff != 0 ? priorityDiff
                : isTableMigration() ? (tableName() == null ? "" : tableName()).compareTo(o.tableName())
                : (columnName() == null ? "" : columnName()).compareTo(o.columnName());
    }

    public boolean isTableMigration() {
        switch (type()) {
            case CREATE_TABLE:
            case DROP_TABLE:
            case CREATE_TEMP_TABLE_FROM_EXISTING:
            case UPDATE_PRIMARY_KEY:
            case UPDATE_FOREIGN_KEYS:
                return true;
        }
        return false;
    }
}
