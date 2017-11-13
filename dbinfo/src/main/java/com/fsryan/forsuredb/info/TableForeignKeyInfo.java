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
import java.util.Map;

/**
 * <p>
 *     An alternative means of serializing foreign keys on the table. The old way,
 *     {@link ForeignKeyInfo} gets serialized on columns. The old way was inflexible
 *     because it did not allow for composite keys--at least not in any sensible way.
 * </p>
 */
@AutoValue
public abstract class TableForeignKeyInfo {

    public static Builder builder() {
        return new AutoValue_TableForeignKeyInfo.Builder()
                .foreignTableName("");
    }

    // not known until later on . . . so hack to set this value on an otherwise immutable class
    public abstract String foreignTableApiClassName();    // foreign_table_api_class_name
    public abstract String foreignTableName();  // foreign_table_name
    public abstract Map<String, String> localToForeignColumnMap();    // local_to_foreign_column_map
    public abstract String updateChangeAction();    // update_action
    public abstract String deleteChangeAction();    // delete_action
    public abstract Builder toBuilder();

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder foreignTableApiClassName(String foreignTableApiClassName);    // foreign_table_api_class_name
        public abstract Builder foreignTableName(@Nullable String foreignTableName);  // foreign_table_name
        public abstract Builder localToForeignColumnMap(Map<String, String> localToForeignColumnMap);    // local_to_foreign_column_map
        public abstract Builder updateChangeAction(String updateAction);    // update_action
        public abstract Builder deleteChangeAction(String deleteAction);    // delete_action
        public abstract TableForeignKeyInfo build();
    }
}
