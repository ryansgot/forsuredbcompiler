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

import static com.google.common.base.Strings.nullToEmpty;

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
        ALTER_TABLE_ADD_COLUMN(1),
        ALTER_TABLE_ADD_UNIQUE(1),
        ADD_FOREIGN_KEY_REFERENCE(2),
        ADD_UNIQUE_INDEX(3),
        CREATE_TEMP_TABLE_FROM_EXISTING(4),
        DROP_TABLE(5);

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
        return isTableMigration() ? nullToEmpty(tableName).compareTo(o.getTableName()) : nullToEmpty(columnName).compareTo(o.getColumnName());
    }

    public boolean isTableMigration() {
        return type == Type.CREATE_TABLE || type == Type.DROP_TABLE || type == Type.CREATE_TEMP_TABLE_FROM_EXISTING;
    }
}
