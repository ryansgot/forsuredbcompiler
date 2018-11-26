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
package com.fsryan.forsuredb.annotationprocessor;

import com.fsryan.forsuredb.annotationprocessor.util.AnnotationTranslatorFactory;
import com.fsryan.forsuredb.annotationprocessor.util.AnnotationTranslatorFactory.AnnotationTranslator;
import com.fsryan.forsuredb.annotationprocessor.util.Pair;
import com.fsryan.forsuredb.annotations.*;
import com.fsryan.forsuredb.api.FSDocStoreGetApi;
import com.fsryan.forsuredb.info.*;
import com.fsryan.forsuredb.api.FSGetApi;
import com.google.common.collect.Sets;

import java.util.*;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import static com.fsryan.forsuredb.info.TableInfo.docStoreColumns;
import static javax.lang.model.util.ElementFilter.methodsIn;

/**
 * <p>
 *     This is the TableContext that corresponds to the currently defined extensions of the
 *     {@link FSGetApi FSGetApi} interface annotated with the {@link com.fsryan.forsuredb.annotations.FSTable FSTable} annotation.
 * </p>
 * @author Ryan Scott
 */
public class ProcessingContext implements TableContext {

    private final Set<TypeElement> tableTypes = new HashSet<>();
    private Map<String, TableInfo> tableMap;
    private List<JoinInfo> joins;

    public ProcessingContext(Set<TypeElement> tableTypes) {
        this.tableTypes.addAll(tableTypes);
    }

    @Override
    public Collection<TableInfo> allTables() {
        createTableMapIfNecessary();
        return tableMap.values();
    }

    @Override
    public Map<String, TableInfo> tableMap() {
        createTableMapIfNecessary();
        return tableMap;
    }

    @Override
    public boolean hasTableWithName(String tableName) {
        createTableMapIfNecessary();
        return tableName != null && tableMap.containsKey(tableName);
    }

    @Override
    public TableInfo getTableByName(String tableName) {
        createTableMapIfNecessary();
        return tableName == null ? null : tableMap.get(tableName);
    }

    //TODO: @Override
    public List<JoinInfo> allJoins() {
        createTableMapIfNecessary();
        return joins;
    }

    private void createTableMapIfNecessary() {
        if (tableMap != null) {
            return;
        }
        TableContext.Builder builder = new TableContext.Builder();
        tableTypes.forEach(tableType -> addToBuilder(builder, tableType));
        // TODO: There is no need for ProcessingContext class once the allJoins method is on the TableContext interface
        tableMap = builder.build().tableMap();
        initJoins();
    }

    private void initJoins() {
        joins = new ArrayList<>();
        for (TableInfo table : tableMap.values()) {
            Set<TableForeignKeyInfo> foreignKeys = table.foreignKeys();
            if (foreignKeys == null) {
                continue;
            }

            for (TableForeignKeyInfo foreignKey : foreignKeys) {
                TableInfo parent = tableMap.get(foreignKey.foreignTableName());
                List<ColumnInfo> childColumns = new ArrayList<>();
                List<ColumnInfo> parentColumns = new ArrayList<>();
                for (Map.Entry<String, String> entry : foreignKey.localToForeignColumnMap().entrySet()) {
                    childColumns.add(table.getColumn(entry.getKey()));
                    parentColumns.add(parent.getColumn(entry.getValue()));
                }
                JoinInfo join = JoinInfo.builder().childTable(table)
                        .childColumns(childColumns)
                        .parentTable(parent)
                        .parentColumns(parentColumns)
                        .build();
                joins.add(join);
            }
        }
    }

    private static void addToBuilder(TableContext.Builder builder, TypeElement intf) {
        if (intf == null) {
            throw new IllegalArgumentException("Cannot create TableInfo create null TypeElement");
        }
        if (intf.getKind() != ElementKind.INTERFACE) {
            throw new IllegalArgumentException("Can only create TableInfo create " + ElementKind.INTERFACE.toString() + ", not " + intf.getKind().toString());
        }

        final String tableName = createTableName(intf);
        // docStoreParametrization will be non-null only for doc store tables, so add the doc store columns in this case
        String docStoreParameterization = getDocStoreParametrizationFrom(intf);
        if (docStoreParameterization != null) {
            docStoreColumns().values()
                    .forEach(c -> builder.addColumn(tableName, c.columnName(), c.toBuilder()));
        }
        builder.addTable(tableName, intf.getQualifiedName().toString(), TableInfo.builder()
                .tableName(tableName)
                .qualifiedClassName(intf.getQualifiedName().toString())
                .docStoreParameterization(docStoreParameterization)
                .resetPrimaryKey(primaryKeyFrom(intf))
                .primaryKeyOnConflict(primaryKeyOnConflictFrom(intf))
                .staticDataAsset(createStaticDataAsset(intf)));

        methodsIn(intf.getEnclosedElements()).forEach(ee -> {
            builder.addColumn(tableName, columnNameOf(ee), InfoTransformer.transform(ee));
            if (containsForeignKey(ee)) {
                Pair<String, TableForeignKeyInfo.Builder> p = foreignKeyInfoBuilder(ee);
                builder.addForeignKeyInfo(tableName, p.first, p.second);
            }
        });
    }

    // compositeId -> Builder
    private static Pair<String, TableForeignKeyInfo.Builder> foreignKeyInfoBuilder(ExecutableElement ee) {
        return ee.getAnnotationMirrors().stream().filter(am -> {
            final String type = am.getAnnotationType().toString();
            return type.equals(FSForeignKey.class.getName()) || type.equals(ForeignKey.class.getName());
        }).map(am -> new Pair<>(am.getAnnotationType().toString(), AnnotationTranslatorFactory.inst().create(am)))
        .map(p -> {
            String type = p.first;
            AnnotationTranslator at = p.second;
            if (type.equals(ForeignKey.class.getName())) {
                return new Pair<>("", tableForeignKeyInfoBuilderFromLegacy(columnNameOf(ee), at));
            }
            final String compositeId = at.property("compositeId").asString();
            return new Pair<>(compositeId, tableForeignKeyInfoBuilderSet(columnNameOf(ee), at));
        })
        .findFirst()
        .get();
    }

    private static TableForeignKeyInfo.Builder tableForeignKeyInfoBuilderSet(String columnName, AnnotationTranslator at) {
        Map<String, String> localToForeignColumnMap = new HashMap<>(1);
        localToForeignColumnMap.put(columnName, at.property("columnName").asString());
        return TableForeignKeyInfo.builder()
                .foreignTableApiClassName(at.property("apiClass").asString())
                .deleteChangeAction(at.property("deleteAction").asString())
                .updateChangeAction(at.property("updateAction").asString())
                .localToForeignColumnMap(localToForeignColumnMap);
    }

    private static TableForeignKeyInfo.Builder tableForeignKeyInfoBuilderFromLegacy(String columnName, AnnotationTranslator at) {
        Map<String, String> localToForeignColumnMap = new HashMap<>(1);
        localToForeignColumnMap.put(columnName, at.property("columnName").asString());
        return TableForeignKeyInfo.builder()
                .deleteChangeAction(ForeignKey.ChangeAction.from(at.property("deleteAction").asString()).name())
                .updateChangeAction(ForeignKey.ChangeAction.from(at.property("updateAction").asString()).name())
                .localToForeignColumnMap(localToForeignColumnMap)
                .foreignTableApiClassName(at.property("apiClass").asString());
    }

    private static boolean containsForeignKey(ExecutableElement ee) {
        FSForeignKey fsfk = ee.getAnnotation(FSForeignKey.class);
        ForeignKey legacyForeignKey = ee.getAnnotation(ForeignKey.class);
        return fsfk != null || legacyForeignKey != null;
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

    private static String columnNameOf(ExecutableElement ee) {
        for (AnnotationMirror annotationMirror : ee.getAnnotationMirrors()) {
            if (annotationMirror.getAnnotationType().toString().equals(FSColumn.class.getName())) {
                return AnnotationTranslatorFactory.inst().create(annotationMirror).property("value").asString();
            }
        }
        return ee.getSimpleName().toString();
    }

    private static Set<String> primaryKeyFrom(TypeElement intf) {
        FSPrimaryKey primaryKey = intf.getAnnotation(FSPrimaryKey.class);
        return primaryKey == null || primaryKey.value().length == 0   // <-- do not allow user to specify no primary key
                ? Sets.newHashSet(TableInfo.DEFAULT_PRIMARY_KEY_COLUMN)
                : Sets.newHashSet(primaryKey.value());
    }

    private static String createStaticDataAsset(TypeElement intf) {
        FSStaticData staticData = intf.getAnnotation(FSStaticData.class);
        return staticData == null ? null : staticData.value();
    }

    private static String primaryKeyOnConflictFrom(TypeElement intf) {
        FSPrimaryKey primaryKey = intf.getAnnotation(FSPrimaryKey.class);
        return primaryKey == null ? "" : primaryKey.onConflict();
    }
}
