/*
   forsuredbcompiler, an annotation processor and code generator for the forsuredb project

   Copyright 2015 Ryan Scott

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
package com.fsryan.forsuredb.api.migration;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;

import java.util.Map;


// TODO: rethink this. I think these can be simplified and more of the work can be imposed upon the DBMS integration library
// You should not need so many different values.
// Additionally, order need not be imposed on migrations at this level
/**
 * <p>
 *     The Migration class holds all of the information necessary for a {@link QueryGenerator} to generate the
 *     necessary query to apply the migration
 * </p>
 */
@lombok.EqualsAndHashCode
@lombok.ToString
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@lombok.Builder(builderClassName = "Builder")
public class Migration implements Comparable<Migration> {

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
         * <p>
         *     Foreign keys may or may not be marked on columns. If you use this, I'm not sure what will happen.
         *     It is only around for compatibility with previous versions
         *     @deprecated since 0.11.0
         * </p>
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

    @Getter @SerializedName("table_name") private final String tableName;
    @Getter @SerializedName("column_name") private final String columnName;
    @Getter @SerializedName("migration_type") private final Type type;
    @Getter @SerializedName("extras") private final Map<String, String> extras;

    public Migration(String tableName, String columnName, Type type) {
        this(tableName, columnName, type, null);
    }

    public boolean hasExtras() {
        return extras != null;
    }

    @Override
    public int compareTo(Migration o) {
        if (o == null || this.getType() == null) {
            return -1;
        }
        if (type == null) {
            return 1;
        }

        final int priorityDiff = type.getPriority() - o.getType().getPriority();
        // Unambiguously determine order based upon comparing table or column name in the case of equal priority
        return priorityDiff != 0 ? priorityDiff
                : isTableMigration() ? (tableName == null ? "" : tableName).compareTo(o.getTableName())
                : (columnName == null ? "" : columnName).compareTo(o.getColumnName());
    }

    public boolean isTableMigration() {
        switch (type) {
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
