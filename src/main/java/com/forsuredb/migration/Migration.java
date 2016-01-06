/*
   forsuredb, an object relational mapping tool

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
package com.forsuredb.migration;

import com.forsuredb.annotationprocessor.info.ForeignKeyInfo;
import com.forsuredb.annotationprocessor.info.TableInfo;
import com.google.gson.annotations.SerializedName;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * <p>
 *     The Migration class holds all of the information necessary for a {@link QueryGenerator} to generate the
 *     necessary query to apply the migration
 * </p>
 */
@EqualsAndHashCode
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(builderClassName = "Builder")
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
        return type == null ? 1 : type.getPriority() - o.getType().getPriority();
    }
}
