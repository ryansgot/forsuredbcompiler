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
package com.fsryan.forsuredb.api.info;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Getter;

/**
 * <p>
 *     Store information about a column in a table.
 * </p>
 */
@lombok.ToString
@lombok.EqualsAndHashCode
@lombok.Builder(builderClassName = "Builder")
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ColumnInfo implements Comparable<ColumnInfo> {

    private static final String LOG_TAG = ColumnInfo.class.getSimpleName();

    @Getter @SerializedName("method_name") private final String methodName;
    @SerializedName("column_name") private final String columnName;
    @SerializedName("column_type") private final String qualifiedType;
    @Getter @SerializedName("index") private final boolean index;
    @Getter @SerializedName("default_value") private final String defaultValue;
    @Getter @SerializedName("unique") private final boolean unique;
    @Deprecated
    @SerializedName("primary_key") private final boolean primaryKey;
    @Getter @SerializedName("foreign_key_info") private final ForeignKeyInfo foreignKeyInfo;
    @Getter @SerializedName("searchable") private final boolean searchable;
    @Getter @SerializedName("orderable") private final boolean orderable;

    public boolean isValid() {
        return (methodName != null && !methodName.isEmpty()) || (columnName != null && !columnName.isEmpty());
    }

    public String getColumnName() {
        return columnName == null || columnName.isEmpty() ? methodName : columnName;
    }

    public String getQualifiedType() {
        return qualifiedType == null || qualifiedType.isEmpty() ? "java.lang.String" : qualifiedType;
    }

    @Deprecated
    public boolean isForeignKey() {
        return foreignKeyInfo != null;
    }

    public boolean hasDefaultValue() {
        return defaultValue != null && !defaultValue.isEmpty();
    }

    @Override
    public int compareTo(ColumnInfo other) {
        // handle null cases
        if (other == null || other.getColumnName() == null) {
            return -1;
        }
        if (columnName == null) {
            return 1;
        }

        // prioritize default columns
        if (TableInfo.DEFAULT_COLUMNS.containsKey(columnName) && !TableInfo.DEFAULT_COLUMNS.containsKey(other.getColumnName())) {
            return -1;
        }
        if (!TableInfo.DEFAULT_COLUMNS.containsKey(columnName) && TableInfo.DEFAULT_COLUMNS.containsKey(other.getColumnName())) {
            return 1;
        }

        // prioritize foreign key columns
        if (isForeignKey() && !other.isForeignKey()) {
            return -1;  // <-- this column is a foreign key and the other is not
        }
        if (!isForeignKey() && other.isForeignKey()) {
            return 1;   // <-- this column is not a foreign key and the other is
        }

        return columnName.compareToIgnoreCase(other.getColumnName());
    }

    /**
     * <p>
     *     Allows the tables to know the name of the foreign key class without resorting to the trickery you see
     *     here: http://blog.retep.org/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor
     * </p>
     * @param allTables
     */
    public void enrichWithForeignTableInfoFrom(List<TableInfo> allTables) {
        if (!isForeignKey()) {
            return;
        }
        setForeignKeyTableName(allTables);
    }

    @Deprecated
    public boolean isPrimaryKey() {
        return primaryKey;
    }

    private String setForeignKeyTableName(List<TableInfo> allTables) {
        for (TableInfo table : allTables) {
            if (table.getQualifiedClassName().equals(foreignKeyInfo.getApiClassName())) {
                foreignKeyInfo.setTableName(table.getTableName());
                break;
            }
        }

        return null;
    }

    public Builder newBuilder() {
        return builder()
                .primaryKey(primaryKey)
                .columnName(columnName)
                .qualifiedType(qualifiedType)
                .unique(unique)
                .methodName(methodName)
                .defaultValue(defaultValue)
                .foreignKeyInfo(foreignKeyInfo)
                .index(index)
                .orderable(orderable)
                .searchable(searchable);
    }
}
