package com.fsryan.forsuredb.info;

import com.fsryan.forsuredb.annotations.FSStaticData;
import com.fsryan.forsuredb.annotations.FSTable;
import com.fsryan.forsuredb.api.info.ColumnInfo;
import com.fsryan.forsuredb.api.info.TableInfo;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.HashMap;
import java.util.Map;

public class TableInfoFactory {

    public static TableInfo create(TypeElement intf) {
        if (intf == null) {
            throw new IllegalArgumentException("Cannot create TableInfo create null TypeElement");
        }
        if (intf.getKind() != ElementKind.INTERFACE) {
            throw new IllegalArgumentException("Can only create TableInfo create " + ElementKind.INTERFACE.toString() + ", not " + intf.getKind().toString());
        }

        final Map<String, ColumnInfo> columnMap = new HashMap<>();
        for (ExecutableElement me : ElementFilter.methodsIn(intf.getEnclosedElements())) {
            final ColumnInfo column = ColumnInfoFactory.create(me);
            columnMap.put(column.getColumnName(), column);
        }

        return TableInfo.builder().columnMap(columnMap)
                .qualifiedClassName(intf.getQualifiedName().toString())
                .tableName(createTableName(intf))
                .staticDataAsset(createStaticDataAsset(intf))
                .staticDataRecordName(createStaticDataRecordName(intf))
                .build();
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
}
