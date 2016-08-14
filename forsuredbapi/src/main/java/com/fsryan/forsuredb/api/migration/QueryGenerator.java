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

import java.util.List;

public abstract class QueryGenerator implements Comparable<QueryGenerator> {

    private final Migration.Type type;
    private final String tableName;

    public QueryGenerator(String tableName, Migration.Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("tableName cannot be null or empty");
        }
        this.type = type;
        this.tableName = tableName;
    }

    @Override
    public final int compareTo(QueryGenerator other) {
        if (other == null) {
            return -1;
        }
        return type.getPriority() - other.getMigrationType().getPriority();
    }

    public abstract List<String> generate();

    public final Migration.Type getMigrationType() {
        return type;
    }

    public final String getTableName() {
        return tableName;
    }
}
