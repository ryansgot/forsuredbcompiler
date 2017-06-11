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

    public enum Type {
        CREATE_TABLE(0),
        ALTER_TABLE_ADD_COLUMN(10),
        ALTER_TABLE_ADD_UNIQUE(10),
        ADD_FOREIGN_KEY_REFERENCE(19),
        ADD_UNIQUE_INDEX(30),
        ADD_INDEX(30),
        MAKE_COLUMN_UNIQUE(30),
        CREATE_TEMP_TABLE_FROM_EXISTING(40),
        UPDATE_PRIMARY_KEY(70),
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

    @Override
    public int compareTo(Migration o) {
        if (o == null || this.getType() == null) {
            return -1;
        }
        if (type == null) {
            return 1;
        }

        int priorityDiff = type.getPriority() - o.getType().getPriority();
        if (priorityDiff != 0) {
            return priorityDiff;
        }
        // Unambiguously determine order based upon comparing table or column name in the case of equal priority
        return isTableMigration()
                ? (tableName == null ? "" : tableName).compareTo(o.getTableName())
                : (columnName == null ? "" : columnName).compareTo(o.getColumnName());
    }

    public boolean isTableMigration() {
        return type == Type.CREATE_TABLE || type == Type.DROP_TABLE || type == Type.CREATE_TEMP_TABLE_FROM_EXISTING;
    }
}
