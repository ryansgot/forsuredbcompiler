package com.fsryan.forsuredb.annotationprocessor;

import com.fsryan.forsuredb.annotationprocessor.util.APLog;
import com.fsryan.forsuredb.annotationprocessor.util.AnnotationTranslatorFactory;
import com.fsryan.forsuredb.annotations.*;
import com.fsryan.forsuredb.api.FSDocStoreGetApi;
import com.fsryan.forsuredb.info.ColumnInfo;
import com.fsryan.forsuredb.info.TableForeignKeyInfo;
import com.fsryan.forsuredb.info.TableInfo;
import com.google.common.collect.Sets;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TableInfoFactory {

    public static TableInfo create(TypeElement intf) {
        if (intf == null) {
            throw new IllegalArgumentException("Cannot create TableInfo create null TypeElement");
        }
        if (intf.getKind() != ElementKind.INTERFACE) {
            throw new IllegalArgumentException("Can only create TableInfo create " + ElementKind.INTERFACE.toString() + ", not " + intf.getKind().toString());
        }

        final Map<String, ColumnInfo> columnMap = new HashMap<>();
        // docStoreParametrization will be non-null only for doc store tables, so add the doc store columns in this case
        String docStoreParameterization = getDocStoreParametrizationFrom(intf);
        if (docStoreParameterization != null) {
            APLog.i(TableInfoFactory.class.getSimpleName(), "Doc store parameterization found (" + docStoreParameterization + ") for table: " + createTableName(intf));
            columnMap.putAll(TableInfo.docStoreColumns());
        }
        for (ExecutableElement me : ElementFilter.methodsIn(intf.getEnclosedElements())) {
            final ColumnInfo column = ColumnInfoFactory.create(me, docStoreParameterization != null);
            columnMap.put(column.getColumnName(), column);
        }

        // TODO: validation check so that you can fail fast during compilation
        return TableInfo.builder().columnMap(columnMap)
                .qualifiedClassName(intf.getQualifiedName().toString())
                .tableName(createTableName(intf))
                .staticDataAsset(createStaticDataAsset(intf))
                .staticDataRecordName(createStaticDataRecordName(intf))
                .docStoreParameterization(docStoreParameterization)
                .primaryKey(primaryKeyFrom(intf))
                .primaryKeyOnConflict(primaryKeyOnConflictFrom(intf))
                .foreignKeys(foreignKeysFrom(intf))
                .build();
    }

    private static String getDocStoreParametrizationFrom(TypeElement intf) {
        for (TypeMirror typeMirror : intf.getInterfaces()) {
            DeclaredType declaredType = (DeclaredType) typeMirror;
            if (typeMirror.toString().startsWith(FSDocStoreGetApi.class.getName())) {
                return declaredType.getTypeArguments().get(0).toString();  // <-- there should be one type argument only
            }
        }
        return null;
    }

    private static String createTableName(TypeElement intf) {
        FSTable table = intf.getAnnotation(FSTable.class);
        return table == null ? intf.getSimpleName().toString() : table.value();
    }

    private static String createStaticDataAsset(TypeElement intf) {
        FSStaticData staticData = intf.getAnnotation(FSStaticData.class);
        return staticData == null ? null : staticData.asset();
    }

    private static String createStaticDataRecordName(TypeElement intf) {
        FSStaticData staticData = intf.getAnnotation(FSStaticData.class);
        return staticData == null ? null : staticData.recordName();
    }

    private static String primaryKeyOnConflictFrom(TypeElement intf) {
        FSPrimaryKey primaryKey = intf.getAnnotation(FSPrimaryKey.class);
        return primaryKey == null ? "" : primaryKey.onConflict();
    }

    private static Set<String> primaryKeyFrom(TypeElement intf) {
        FSPrimaryKey primaryKey = intf.getAnnotation(FSPrimaryKey.class);
        return primaryKey == null || primaryKey.value().length == 0   // <-- do not allow user to specify no primary key
                ? Sets.newHashSet(TableInfo.DEFAULT_PRIMARY_KEY_COLUMN)
                : Sets.newHashSet(primaryKey.value());
    }

    // pretty much screwed up due to transition between ForeignKey and FSForeignKey, which allows for composites
    private static Set<TableForeignKeyInfo> foreignKeysFrom(TypeElement intf) {
        HashMap<String, TableForeignKeyInfo> compositeMap = new HashMap<>();
        Set<TableForeignKeyInfo> ret = new HashSet<>();
        for (ExecutableElement columnElement : ElementFilter.methodsIn(intf.getEnclosedElements())) {
            for (AnnotationMirror am : columnElement.getAnnotationMirrors()) {
                AnnotationTranslatorFactory.AnnotationTranslator at = AnnotationTranslatorFactory.inst().create(am);
                String localColumn = columnNameFrom(columnElement);
                if (ForeignKey.class.getName().equals(am.getAnnotationType().toString())) {
                    Map<String, String> localToForeignColumnMap = new HashMap<>(1);
                    localToForeignColumnMap.put(localColumn, at.property("columnName").asString());
                    ret.add(TableForeignKeyInfo.builder()
                            .deleteChangeAction(at.property("deleteAction").asString())
                            .updateChangeAction(at.property("updateAction").asString())
                            .foreignTableApiClassName(at.property("apiClass").asString())
                            .localToForeignColumnMap(localToForeignColumnMap)
                            .build());
                } else if (FSForeignKey.class.getName().equals(am.getAnnotationType().toString())) {
                    final String compositeId = at.property("compositeId").asString();
                    TableForeignKeyInfo existing = compositeMap.get(compositeId);
                    if (existing == null) {
                        Map<String, String> localToForeignColumnMap = new HashMap<>(1);
                        localToForeignColumnMap.put(localColumn, at.property("columnName").asString());
                        compositeMap.put(compositeId, TableForeignKeyInfo.builder()
                                .foreignTableApiClassName(at.property("apiClass").asString())
                                .deleteChangeAction(at.property("deleteAction").asString())
                                .updateChangeAction(at.property("updateAction").asString())
                                .localToForeignColumnMap(localToForeignColumnMap)
                                .build());
                    } else {
                        validateForeignKeyUpdate(existing, at);
                        Map<String, String> localToForeignColumnMap = new HashMap<>(1);
                        localToForeignColumnMap.put(localColumn, at.property("columnName").asString());
                        compositeMap.put(compositeId, existing.toBuilder()
                                .localToForeignColumnMap(localToForeignColumnMap)
                                .build());
                    }
                }
            }
        }

        ret.addAll(compositeMap.values());
        return ret;
    }

    private static void validateForeignKeyUpdate(TableForeignKeyInfo existing, AnnotationTranslatorFactory.AnnotationTranslator at) {
        final String decalredApiClass = at.property("apiClass").asString();
        final String updateAction = at.property("updateAction").asString();
        final String deleteAction = at.property("deleteAction").asString();
        if (!existing.getForeignTableApiClassName().equals(decalredApiClass)) {
            throw new IllegalArgumentException("apiClass mismatch: expected " + existing.getForeignTableApiClassName() + " but was " + decalredApiClass);
        }
        if (!existing.updateChangeAction().equals(updateAction)) {
            throw new IllegalArgumentException("updateAction mismatch: expected " + existing.updateChangeAction() + " but was " + updateAction);
        }
        if (!existing.deleteChangeAction().equals(deleteAction)) {
            throw new IllegalArgumentException("deleteAction mismatch: expected " + existing.deleteChangeAction() + " but was " + deleteAction);
        }
    }

    private static String columnNameFrom(ExecutableElement columnElement) {
        FSColumn fsColumn = columnElement.getAnnotation(FSColumn.class);
        return fsColumn == null ? columnElement.getSimpleName().toString() : fsColumn.value();
    }
}
