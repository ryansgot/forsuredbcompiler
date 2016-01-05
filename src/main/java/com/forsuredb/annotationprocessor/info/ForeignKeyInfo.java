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
package com.forsuredb.annotationprocessor.info;

import com.forsuredb.annotation.ForeignKey;
import com.google.gson.annotations.SerializedName;

import javax.lang.model.element.ExecutableElement;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Builder(builderClassName = "Builder")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ForeignKeyInfo {

    @Getter @SerializedName("update_action") private final ForeignKey.ChangeAction updateAction;
    @Getter @SerializedName("delete_action") private final ForeignKey.ChangeAction deleteAction;
    @Getter @Setter @SerializedName("foreign_table_name") private String tableName;     // <-- may not be known on creation
    @Getter @SerializedName("foreign_column_name")private final String columnName;
    @Getter @SerializedName("foreign_api_class_name") private final String apiClassName;

    public static ForeignKeyInfo from(ExecutableElement ee) {
        // TODO
        return null;
    }

    public boolean isValid() {
        return columnName != null
                && !columnName.isEmpty()
                && updateAction != null
                && deleteAction != null
                && (apiClassName != null && !apiClassName.isEmpty())
                || (tableName != null && !tableName.isEmpty());
    }
}
