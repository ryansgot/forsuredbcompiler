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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// TODO: More than any other class, this class highlights the need to separate SQL generation concerns from the forsuredbapi
public abstract class Finder{

    public enum Operator {
        EQ("="), NE("!="), LE("<="), LT("<"), GE(">="), GT(">"), LIKE("LIKE");

        private String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }
    }

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

    protected final void addToBuf(String column, Operator operator, Object value) {
        if (!canAddClause(column, operator, value)) {
            return;
        }

        column = Sql.generator().unambiguousColumn(tableName, column);
        whereBuf.append(column)
                .append(" ").append(operator.getSymbol())
                .append(" ").append(operator == Operator.LIKE ? "%?%" : "?");
        replacementsList.add(Date.class.equals(value.getClass()) ? dateFormat.format((Date) value) : value.toString());
    }

    private boolean canAddClause(String column, Operator operator, Object value) {
        return !Strings.isNullOrEmpty(column) && operator != null && value != null && !value.toString().isEmpty();
    }

    protected void surroundCurrentWhereWithParens() {
        String currentWhere = whereBuf.toString();
        whereBuf.delete(0, whereBuf.length());
        whereBuf.trimToSize();
        whereBuf.append("(").append(currentWhere).append(")");
    }
}
