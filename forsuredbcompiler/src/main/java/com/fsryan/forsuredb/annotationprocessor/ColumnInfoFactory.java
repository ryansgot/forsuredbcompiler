package com.fsryan.forsuredb.annotationprocessor;

import com.fsryan.forsuredb.annotationprocessor.util.AnnotationTranslatorFactory;
import com.fsryan.forsuredb.annotations.*;
import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.ForeignKeyInfo;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;

public class ColumnInfoFactory {

    public static ColumnInfo create(ExecutableElement ee, boolean isDocStoreColumn) {
        if (ee.getKind() != ElementKind.METHOD) {
            return null;
        }

        ColumnInfo.Builder builder = ColumnInfo.builder();
        for (AnnotationMirror am : ee.getAnnotationMirrors()) {
            appendAnnotationInfo(builder, am);
        }

        if (isDocStoreColumn) {
            builder.index(true);
        }

        return builder.methodName(ee.getSimpleName().toString())
                .qualifiedType(ee.getReturnType().toString())
                .build();
    }

    private static void appendAnnotationInfo(ColumnInfo.Builder builder, AnnotationMirror am) {
        AnnotationTranslatorFactory.AnnotationTranslator at = AnnotationTranslatorFactory.inst().create(am);

        String annotationClass = am.getAnnotationType().toString();
        if (annotationClass.equals(FSColumn.class.getName())) {
            builder.columnName(at.property("value").as(String.class))
                    .searchable(at.property("searchable").castSafe(true))
                    .orderable(at.property("orderable").castSafe(true));
        } else if (annotationClass.equals(ForeignKey.class.getName())) {
            builder.foreignKeyInfo(ForeignKeyInfo.builder().columnName(at.property("columnName").asString())
                    .apiClassName(at.property("apiClass").asString())
                    .deleteAction(ForeignKey.ChangeAction.from(at.property("deleteAction").asString()).name())  // TODO: check this
                    .updateAction(ForeignKey.ChangeAction.from(at.property("updateAction").asString()).name())  // TODO: check this
                    .build());
        } else if (annotationClass.equals(FSPrimaryKey.class.getName())) {
            builder.primaryKey(true);
        } else if (annotationClass.equals(Unique.class.getName())) {
            builder.unique(true);
            if (at.property("index").as(boolean.class)) {
                builder.index(true);
            }
        } else if (annotationClass.equals(Index.class.getName())) {
            builder.index(true);
            if (at.property("unique").as(boolean.class)) {
                builder.unique(true);
            }
        } else if (annotationClass.equals(FSDefault.class.getName())) {
            builder.defaultValue(at.property("value").asString());
        }
    }
}
