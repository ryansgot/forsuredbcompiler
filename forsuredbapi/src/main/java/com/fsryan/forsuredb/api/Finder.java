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
package com.fsryan.forsuredb.api;

import com.fsryan.forsuredb.api.sqlgeneration.Sql;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class Finder {

    public static final int OP_LT = -3;
    public static final int OP_LE = -2;
    public static final int OP_NE = -1;
    public static final int OP_EQ = 0;
    public static final int OP_GE = 1;
    public static final int OP_GT = 2;
    public static final int OP_LIKE = 3;

    protected final String tableName;
    protected final StringBuffer whereBuf = new StringBuffer();
    protected final List<String> replacementsList = new ArrayList<>();

    public Finder(String tableName) {
        this.tableName = tableName;
    }

    public final FSSelection selection() {
        return new FSSelection() {
            String where = whereBuf.toString();
            String[] replacements = replacementsList.toArray(new String[replacementsList.size()]);

            @Override
            public String where() {
                return where;
            }

            @Override
            public String[] replacements() {
                return replacements;
            }
        };
    }

    protected final void addToBuf(String column, int operator, Object value) {
        if (!canAddClause(column, value)) {
            return;
        }

        whereBuf.append(Sql.generator().whereOperation(tableName, column, operator)).append(" ");
        if (operator == OP_LIKE) {
            whereBuf.append(Sql.generator().wildcardKeyword()).append("?").append(Sql.generator().wildcardKeyword());
        } else {
            whereBuf.append("?");
        }

        replacementsList.add(Date.class.equals(value.getClass()) ? Sql.generator().formatDate((Date) value) : value.toString());
    }

    protected void surroundCurrentWhereWithParens() {
        String currentWhere = whereBuf.toString();
        whereBuf.delete(0, whereBuf.length());
        whereBuf.trimToSize();
        whereBuf.append("(").append(currentWhere).append(")");
    }

    private boolean canAddClause(String column, Object value) {
        return !Strings.isNullOrEmpty(column) && value != null && !value.toString().isEmpty();
    }
}
