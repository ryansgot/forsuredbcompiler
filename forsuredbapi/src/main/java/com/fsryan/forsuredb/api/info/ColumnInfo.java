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
package com.fsryan.forsuredb.api.info;

import com.fsryan.forsuredb.annotations.FSColumn;
import com.fsryan.forsuredb.annotations.ForeignKey;
import com.fsryan.forsuredb.annotations.PrimaryKey;
import com.fsryan.forsuredb.annotations.Unique;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;

import lombok.Getter;

/**
 * <p>
 *     Store information about a column in a table.
 * </p>
 */
@lombok.ToString
@lombok.EqualsAndHashCode
@lombok.Builder(builderClassName = "Builder")
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ColumnInfo implements Comparable<ColumnInfo> {

    private static final String LOG_TAG = ColumnInfo.class.getSimpleName();

    @Getter @SerializedName("method_name") private final String methodName;
    @SerializedName("column_name") private final String columnName;
    @SerializedName("column_type") private final String qualifiedType;
    @Getter @SerializedName("default_value") private final String defaultValue;
    @Getter @SerializedName("unique") private final boolean unique;
    @Getter @SerializedName("primary_key") private final boolean primaryKey;
    @Getter @SerializedName("foreign_key_info") private final ForeignKeyInfo foreignKeyInfo;

    public static ColumnInfo from(ExecutableElement ee) {
        if (ee.getKind() != ElementKind.METHOD) {
            return null;
        }

        Builder builder = builder();
        for (AnnotationMirror am : ee.getAnnotationMirrors()) {
            appendAnnotationInfo(builder, am);
        }

        return builder.methodName(ee.getSimpleName().toString())
                      .qualifiedType(ee.getReturnType().toString())
                      .build();
    }

    public boolean isValid() {
        return (methodName != null && !methodName.isEmpty()) || (columnName != null && !columnName.isEmpty());
    }

    public String getColumnName() {
        return columnName == null || columnName.isEmpty() ? methodName : columnName;
    }

    public String getQualifiedType() {
        return qualifiedType == null || qualifiedType.isEmpty() ? "java.lang.String" : qualifiedType;
    }

    public boolean isForeignKey() {
        return foreignKeyInfo != null;
    }

    public boolean hasDefaultValue() {
        return defaultValue != null && !defaultValue.isEmpty();
    }

    @Override
    public int compareTo(ColumnInfo other) {
        // handle null cases
        if (other == null || other.getColumnName() == null) {
            return -1;
        }
        if (columnName == null) {
            return 1;
        }

        // prioritize default columns
        if (TableInfo.DEFAULT_COLUMNS.containsKey(columnName) && !TableInfo.DEFAULT_COLUMNS.containsKey(other.getColumnName())) {
            return -1;
        }
        if (!TableInfo.DEFAULT_COLUMNS.containsKey(columnName) && TableInfo.DEFAULT_COLUMNS.containsKey(other.getColumnName())) {
            return 1;
        }

        // prioritize foreign key columns
        if (isForeignKey() && !other.isForeignKey()) {
            return -1;  // <-- this column is a foreign key and the other is not
        }
        if (!isForeignKey() && other.isForeignKey()) {
            return 1;   // <-- this column is not a foreign key and the other is
        }

        return columnName.compareToIgnoreCase(other.getColumnName());
    }

    /**
     * <p>
     *     Allows the tables to know the name of the foreign key class without resorting to the trickery you see
     *     here: http://blog.retep.org/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor
     * </p>
     * @param allTables
     */
    /*package*/
    public void enrichWithForeignTableInfoFrom(List<TableInfo> allTables) {
        if (!isForeignKey()) {
            return;
        }
        setForeignKeyTableName(allTables);
    }

    private String setForeignKeyTableName(List<TableInfo> allTables) {
        for (TableInfo table : allTables) {
            if (table.getQualifiedClassName().equals(foreignKeyInfo.getApiClassName())) {
                foreignKeyInfo.setTableName(table.getTableName());
                break;
            }
        }

        return null;
    }

    private static void appendAnnotationInfo(Builder builder, AnnotationMirror am) {
        // TODO: figure out the dependency stuff here
//        AnnotationTranslator at = AnnotationTranslatorFactory.inst().create(am);
//
//        String annotationClass = am.getAnnotationType().toString();
//        if (annotationClass.equals(FSColumn.class.getName())) {
//            builder.columnName(at.property("value").as(String.class));
//        } else if (annotationClass.equals(ForeignKey.class.getName())) {
//            builder.foreignKeyInfo(ForeignKeyInfo.builder().columnName(at.property("columnName").asString())
//                    .apiClassName(at.property("apiClass").asString())
//                    .deleteAction(ForeignKey.ChangeAction.from(at.property("deleteAction").asString()))
//                    .updateAction(ForeignKey.ChangeAction.from(at.property("updateAction").asString()))
//                    .build());
//        } else if (annotationClass.equals(PrimaryKey.class.getName())) {
//            builder.primaryKey(true);
//        } else if (annotationClass.equals(Unique.class.getName())) {
//            builder.unique(true);
//        }
    }
}
