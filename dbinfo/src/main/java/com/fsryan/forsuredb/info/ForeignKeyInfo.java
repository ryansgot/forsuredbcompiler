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

@Deprecated
@AutoValue
public abstract class ForeignKeyInfo {

    public static Builder builder() {
        return new AutoValue_ForeignKeyInfo.Builder();
    }

    public abstract String updateAction();  // update_action
    public abstract String deleteAction();  // delete_action
    @Nullable public abstract String tableName();     // foreign_table_name <-- may not be known on creation
    public abstract String columnName();    // foreign_column_name
    public abstract String apiClassName();  // foreign_api_class_name

    public boolean isValid() {
        return columnName() != null
                && !columnName().isEmpty()
                && updateAction() != null
                && !updateAction().isEmpty()
                && deleteAction() != null
                && !deleteAction().isEmpty()
                && (apiClassName() != null && !apiClassName().isEmpty())
                || (tableName() != null && !tableName().isEmpty());
    }

    @Deprecated
    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder updateAction(String updateAction);  // update_action
        public abstract Builder deleteAction(String deleteAction);  // delete_action
        public abstract Builder tableName(String tableName);     // foreign_table_name <-- may not be known on creation
        public abstract Builder columnName(String columnName);    // foreign_column_name
        public abstract Builder apiClassName(String apiClassName);  // foreign_api_class_name
        public abstract ForeignKeyInfo build();
    }
}
