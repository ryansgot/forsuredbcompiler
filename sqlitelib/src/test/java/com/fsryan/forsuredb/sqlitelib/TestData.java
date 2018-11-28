/*
   forsuredbsqlitelib, sqlite library for the forsuredb project

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
package com.fsryan.forsuredb.sqlitelib;

import com.fsryan.forsuredb.api.FSProjection;
import com.fsryan.forsuredb.api.FSSelection;
import com.fsryan.forsuredb.api.Limits;
import com.fsryan.forsuredb.info.ColumnInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TestData {

    public static final String TEST_RES = "src" + File.separator + "test" + File.separator + "resources";

    // Convenience constants
    public static final String TABLE_NAME = "test_table";

    public static String resourceText(String resourceName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(TEST_RES + File.separator + resourceName));
        String line;
        StringBuilder out = new StringBuilder();
        while (null != (line = br.readLine())) {
            out.append(line).append("\n");
        }
        br.close();
        return out.toString();
    }

    static FSSelection createSelection(String where, String... replacements) {
        return createSelection(null, where, replacements);
    }

    static FSSelection createSelection(final Limits limits, final String where, final String... replacements) {
        return new FSSelection() {

            @Override
            public String where() {
                return where;
            }

            @Override
            public String[] replacements() {
                return replacements;
            }

            @Override
            public Limits limits() {
                return limits;
            }
        };
    }

    static Limits createLimits(int count) {
        return createLimits(count, 0);
    }

    static Limits createLimits(int count, int offset) {
        return createLimits(count, offset, false);
    }

    static Limits createLimits(int count, boolean isBottom) {
        return createLimits(count, 0, isBottom);
    }

    static Limits createLimits(final int count, final int offset, final boolean isBottom) {
        return new Limits() {
            @Override
            public int count() {
                return count;
            }

            @Override
            public int offset() {
                return offset;
            }

            @Override
            public boolean isBottom() {
                return isBottom;
            }
        };
    }

    static FSProjection createProjection(String table, String... columns) {
        return createProjection(false, table, columns);
    }

    static FSProjection createDistinctProjection(String table, String... columns) {
        return createProjection(true, table, columns);
    }

    private static FSProjection createProjection(final boolean distinct, final String table, final String... columns) {
        return new FSProjection() {
            @Override
            public String tableName() {
                return table;
            }

            @Override
            public String[] columns() {
                return columns;
            }

            @Override
            public boolean isDistinct() {
                return distinct;
            }
        };
    }

    // Helpers for covenience methods

    private static ColumnInfo.Builder columnFrom(TypeTranslator tt) {
        return ColumnInfo.builder().columnName(nameFrom(tt)).qualifiedType(tt.getQualifiedType()).methodName(methodNameFrom(tt));
    }

    private static String nameFrom(TypeTranslator tt) {
        return tt.name().toLowerCase() + "_column";
    }

    private static String methodNameFrom(TypeTranslator tt) {
        return tt.name().toLowerCase() + "Column";
    }
}
