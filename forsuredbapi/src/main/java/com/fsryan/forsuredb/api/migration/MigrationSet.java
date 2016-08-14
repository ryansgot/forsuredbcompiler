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

import com.fsryan.forsuredb.api.info.TableInfo;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@lombok.ToString
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@lombok.Builder(builderClassName = "Builder")
public class MigrationSet {
    @Getter @SerializedName("ordered_migrations") private List<Migration> orderedMigrations;
    @Getter @SerializedName("target_schema") private Map<String, TableInfo> targetSchema;
    @Getter @SerializedName("db_version") private int dbVersion;

    public boolean containsMigrations() {
        return orderedMigrations != null && orderedMigrations.size() > 0;
    }
}
