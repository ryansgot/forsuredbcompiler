package com.fsryan.forsuredb.info;

import com.fsryan.forsuredb.annotationprocessor.util.APLog;
import com.fsryan.forsuredb.annotations.FSStaticData;
import com.fsryan.forsuredb.annotations.FSTable;
import com.fsryan.forsuredb.annotations.PrimaryKey;
import com.fsryan.forsuredb.api.FSDocStoreGetApi;
import com.fsryan.forsuredb.api.info.ColumnInfo;
import com.fsryan.forsuredb.api.info.TableInfo;
import com.google.common.collect.Sets;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.HashMap;
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
            columnMap.putAll(TableInfo.DOC_STORE_COLUMNS);
        }
        for (ExecutableElement me : ElementFilter.methodsIn(intf.getEnclosedElements())) {
            final ColumnInfo column = ColumnInfoFactory.create(me, docStoreParameterization != null);
            columnMap.put(column.getColumnName(), column);
        }

        return TableInfo.builder().columnMap(columnMap)
                .qualifiedClassName(intf.getQualifiedName().toString())
                .tableName(createTableName(intf))
                .staticDataAsset(createStaticDataAsset(intf))
                .staticDataRecordName(createStaticDataRecordName(intf))
                .docStoreParameterization(docStoreParameterization)
                .primaryKey(primaryKeyFrom(intf))
                .primaryKeyOnConflict(primaryKeyOnConflictFrom(intf))
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
        PrimaryKey primaryKey = intf.getAnnotation(PrimaryKey.class);
        return primaryKey == null ? "" : primaryKey.onConflict();
    }

    private static Set<String> primaryKeyFrom(TypeElement intf) {
        PrimaryKey primaryKey = intf.getAnnotation(PrimaryKey.class);
        return primaryKey == null || primaryKey.columns().length == 0   // <-- do not allow user to specify no primary key
                ? Sets.newHashSet(TableInfo.DEFAULT_PRIMARY_KEY_COLUMN)
                : Sets.newHashSet(primaryKey.columns());
    }
}
