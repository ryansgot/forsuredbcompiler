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
package com.fsryan.forsuredb.info;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * <p>
 *     Store information about a column in a table.
 * </p>
 */

@AutoValue
public abstract class ColumnInfo implements Comparable<ColumnInfo> {

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder methodName(String methodName);                              // method_name
        public abstract Builder columnName(@Nullable String columnName);                    // column_name
        public abstract Builder qualifiedType(@Nullable String qualifiedType);              // column_type
        /**
         * <p>Instead set indices on {@link TableInfo.Builder#indices(Set)}
         * @param index whether this column is an index column
         * @return this {@link Builder}
         */
        @Deprecated
        public abstract Builder index(boolean index);                                       // index
        public abstract Builder defaultValue(String defaultValue);                          // default_value
        public abstract Builder unique(boolean unique);                                     // unique
        /**
         * <p>Instead set primary key on {@link TableInfo.Builder#primaryKey(Set)}}
         * @param primaryKey whether this column is <i>THE</i> primary key of
         *                   the table
         * @return this {@link Builder}
         */
        @Deprecated
        public abstract Builder primaryKey(boolean primaryKey);                             // primary_key
        /**
         * <p>Instead set indices on {@link TableInfo.Builder#foreignKeys(Set)}
         * @param foreignKeyInfo the {@link ForeignKeyInfo} for this column
         * @return this {@link Builder}
         */
        @Deprecated
        public abstract Builder foreignKeyInfo(@Nullable ForeignKeyInfo foreignKeyInfo);    // foreign_key_info
        public abstract Builder searchable(boolean searchable);                             // searchable
        public abstract Builder orderable(boolean orderable);                               // orderable
        public abstract Builder valueAccess(@Nullable List<String> access);                 // NOT SERIALIZED
        public abstract ColumnInfo build();
    }

    public static Builder builder() {
        return new AutoValue_ColumnInfo.Builder()
                .primaryKey(false)
                .searchable(true)
                .orderable(true)
                .unique(false)
                .index(false);
    }

    public abstract String methodName();                                                    // method_name
    @Nullable public abstract String columnName();                                          // column_name
    @Nullable public abstract String qualifiedType();                                       // column_type
    public abstract boolean index();                                                        // index
    @Nullable public abstract String defaultValue();                                        // default_value
    public abstract boolean unique();                                                       // unique

    /**
     * <p>This is a remnant of a time in which you could only set one column as
     * the primary key for a table. The framework has since moved on such that
     * {@link TableInfo#primaryKey()} is the real primary key, supporting
     * composites.
     * @return whether this column is <i>THE</i> primary key of the table
     */
    @Deprecated
    public abstract boolean primaryKey();                                                   // primary_key

    /**
     * <p>This is a remnant of a time in which composite foreign keys were not
     * supported. Instead of this, see {@link TableInfo#foreignKeys()} for the
     * real foreign key information for the table.
     * @return whether this column is <i>THE</i> primary key of the table
     */
    @Deprecated
    @Nullable
    public abstract ForeignKeyInfo foreignKeyInfo();    // foreign_key_info

    /**
     * <p>If the column is searchable, the relevant finder methods for this
     * column will be generated on the associated Finder class.
     * @return whether finder methods will be generated for this column
     */
    public abstract boolean searchable();                                                   // searchable

    /**
     * <p>If the column is orderable, the relevant order by methods for this
     * column will be generated on the associated OrderBy class
     * @return whether order by methods will be generated for this column
     */
    public abstract boolean orderable();                                                    // orderable

    /**
     * <p><i>NOT SERIALIZED</i>--only useful in code generation
     * @return the method access path on an object for accessing a possibly-nested value at runtime.
     */
    @Nullable
    public abstract List<String> valueAccess();                                             // NOT SERIALIZED
    public abstract Builder toBuilder();

    public boolean isValid() {
        final String method = methodName();
        final String column = columnName();
        return (method != null && !method.isEmpty()) || (column != null && !column.isEmpty());
    }

    public String getColumnName() {
        final String column = columnName();
        return column == null || column.isEmpty() ? methodName(): column;
    }

    public String getQualifiedType() {
        final String type = qualifiedType();
        return type == null || type.isEmpty() ? "java.lang.String" : type;
    }

    @Deprecated
    public boolean isForeignKey() {
        return foreignKeyInfo() != null;
    }

    public boolean hasDefaultValue() {
        return defaultValue() != null && !defaultValue().isEmpty();
    }

    public boolean hasPrimitiveType() {
        switch (getQualifiedType()) {
            case "boolean":
            case "byte":
            case "char":
            case "short":
            case "int":
            case "long":
            case "float":
            case "double":
                return true;
        }
        return false;
    }

    @Override
    public int compareTo(ColumnInfo other) {
        // handle null cases
        if (other == null || other.getColumnName() == null) {
            return -1;
        }

        final String actualColumn = getColumnName();
        if (actualColumn == null) {
            return 1;
        }

        // prioritize default columns

        Set<String> defaultColumnNames = TableInfo.defaultColumnNames();
        if (defaultColumnNames.contains(actualColumn) && !defaultColumnNames.contains(other.getColumnName())) {
            return -1;
        }
        if (!defaultColumnNames.contains(actualColumn) && defaultColumnNames.contains(other.getColumnName())) {
            return 1;
        }

        // prioritize foreign key columns
        if (isForeignKey() && !other.isForeignKey()) {
            return -1;  // <-- this column is a foreign key and the other is not
        }
        if (!isForeignKey() && other.isForeignKey()) {
            return 1;   // <-- this column is not a foreign key and the other is
        }

        return actualColumn.compareToIgnoreCase(other.getColumnName());
    }
}
