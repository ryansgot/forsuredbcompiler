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
package com.fsryan.forsuredb;

import com.fsryan.forsuredb.annotationprocessor.TableContext;
import com.fsryan.forsuredb.info.DBInfoFixtures;
import com.fsryan.forsuredb.info.TableInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static com.fsryan.forsuredb.info.DBInfoFixtures.*;
import static com.fsryan.forsuredb.info.TableInfoUtil.tableMapOf;

public class TestData {

    public static final String TEST_RES = "src/test/resources";

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

    /**
     * <p>DO NOT USE--this was used in legacy tests and is an unsustainable way
     * to actually test this code.
     * @return a specific fixture used in legacy tests
     */
    @Deprecated
    public static TableInfo targetTableWithChildForeignKey() {
        return testTargetContext().getTableByName("test_table_3");
    }

    /**
     * <p>DO NOT USE--this was used in legacy tests and is an unsustainable way
     * to actually test this code.
     * @return a specific fixture used in legacy tests
     */
    @Deprecated
    public static TableInfo targetTableWithParentAndChildForeignKey() {
        return testTargetContext().getTableByName("test_table_2");
    }

    /**
     * <p>DO NOT USE--this was used in legacy tests and is an unsustainable way
     * to actually test this code.
     * @return a specific fixture used in legacy tests
     */
    @Deprecated
    public static TableContext testTargetContext() {
        return TableContext.fromSchema(
                tableMapOf(
                        DBInfoFixtures.tableBuilder("test_table")
                                .qualifiedClassName("com.fsryan.annotationprocessor.generator.code.TestTable")
                                .staticDataAsset("test_table_data.xml")
                                .staticDataRecordName("test_table_data")
                                .addColumn(longCol()
                                        .columnName("test_table_2_id")
                                        .methodName("testTable2Id")
                                        .foreignKeyInfo(cascadeFKI("test_table_2", "_id")
                                                .apiClassName("com.fsryan.annotationprocessor.generator.code.TestTable2")
                                                .build())
                                        .build())
                                .build(),
                        DBInfoFixtures.tableBuilder("test_table_2")
                                .qualifiedClassName("com.fsryan.annotationprocessor.generator.code.TestTable2")
                                .addColumn(longCol().columnName("test_table_3_id")
                                        .methodName("testTable3Id")
                                        .foreignKeyInfo(cascadeFKI("test_table_3", "_id")
                                                .apiClassName("com.fsryan.annotationprocessor.generator.code.TestTable3")
                                                .build())
                                        .build())
                                .build(),
                        DBInfoFixtures.tableBuilder("test_table_3")
                                .qualifiedClassName("com.fsryan.annotationprocessor.generator.code.TestTable3")
                                .addColumn(doubleCol()
                                        .columnName("app_rating")
                                        .methodName("appRating")
                                        .build()
                                ).addColumn(bigDecimalCol()
                                        .columnName("competitor_app_rating")
                                        .methodName("competitorAppRating")
                                        .searchable(false)
                                        .build()
                                ).addColumn(longCol()
                                        .columnName("global_id")
                                        .methodName("globalId")
                                        .orderable(false)
                                        .build()
                                ).addColumn(intCol()
                                        .columnName("login_count")
                                        .methodName("loginCount")
                                        .build()
                                ).build()
                )
        );
//        return newTableContext().addTable(
//                        table("test_table")
//                                .qualifiedClassName("com.fsryan.annotationprocessor.generator.code.TestTable")
//                                .staticDataAsset("test_table_data.xml")
//                                .staticDataRecordName("test_table_data")
//                                .addColumn(longCol().columnName("test_table_2_id")
//                                        .methodName("testTable2Id")
//                                        .foreignKeyInfo(cascadeFKI("test_table_2")
//                                                .columnName("_id")
//                                                .apiClassName("com.fsryan.annotationprocessor.generator.code.TestTable2")
//                                                .build())
//                                        .build())
//                                .build()
//                )
//                .addTable(
//                        table("test_table_2")
//                                .qualifiedClassName("com.fsryan.annotationprocessor.generator.code.TestTable2")
//                                .addColumn(longCol().columnName("test_table_3_id")
//                                        .methodName("testTable3Id")
//                                        .foreignKeyInfo(cascadeFKI("test_table_3")
//                                                .columnName("_id")
//                                                .apiClassName("com.fsryan.annotationprocessor.generator.code.TestTable3")
//                                                .build())
//                                        .build())
//                                .build()
//                )
//                .addTable(table("test_table_3")
//                        .qualifiedClassName("com.fsryan.annotationprocessor.generator.code.TestTable3")
//                        .addColumn(doubleCol().columnName("app_rating").methodName("appRating").build())
//                        .addColumn(bigDecimalCol().columnName("competitor_app_rating").methodName("competitorAppRating").searchable(false).build())
//                        .addColumn(longCol().columnName("global_id").methodName("globalId").orderable(false).build())
//                        .addColumn(intCol().columnName("login_count").methodName("loginCount").build())
//                        .build()
//                )
//                .build();
    }
}
