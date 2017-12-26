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

import java.util.List;

@AutoValue
public abstract class JoinInfo {

    public static Builder builder() {
        return new AutoValue_JoinInfo.Builder();
    }

    public abstract TableInfo parentTable();    // parent_table
    public abstract List<ColumnInfo> parentColumns(); // parent_columns
    public abstract TableInfo childTable(); // child_table
    public abstract List<ColumnInfo> childColumns();    // child_columns

    public boolean isValid() {
        return parentTable() != null
                && parentColumns() != null
                && !parentColumns().isEmpty()
                && childTable() != null
                && childColumns() != null
                && !childColumns().isEmpty();
    }

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder parentTable(TableInfo parentTable);    // parent_table
        public abstract Builder parentColumns(List<ColumnInfo> parentColumns); // parent_columns
        public abstract Builder childTable(TableInfo childTable); // child_table
        public abstract Builder childColumns(List<ColumnInfo> childColumns);    // child_columns
        public abstract JoinInfo build();
    }
}
