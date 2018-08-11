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

import com.fsryan.forsuredb.annotationprocessor.generator.code.*;
import com.fsryan.forsuredb.annotationprocessor.generator.resource.MigrationGenerator;
import com.fsryan.forsuredb.annotationprocessor.util.PropertyRetriever;
import com.fsryan.forsuredb.annotations.FSTable;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.annotationprocessor.util.APLog;
import com.fsryan.forsuredb.annotationprocessor.util.AnnotationTranslatorFactory;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.annotation.processing.SupportedOptions;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import static com.fsryan.forsuredb.annotationprocessor.util.PropertyRetriever.properties;

/**
 * <p>
 *     FSAnnotationProcessor is the guts of the forsuredb project. When you compile, the
 *     {@link #process(Set, RoundEnvironment)} gets called, and the annotation processing begins.
 * </p>
 * @author Ryan Scott
 */
@SupportedAnnotationTypes("com.fsryan.forsuredb.annotations.*")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedOptions({
        "forsuredb.applicationPackageName",
        "forsuredb.resourcesDirectory",
        "forsuredb.resultParameter",
        "forsuredb.recordContainer",
        "forsuredb.migrationDirectory",
        "forsuredb.appProjectDirectory",
        "forsuredb.addGeneratedAnnotation",
        "forsuredb.createMigrations"
})
public class FSAnnotationProcessor extends AbstractProcessor {

    private static final String LOG_TAG = FSAnnotationProcessor.class.getSimpleName();

    private static boolean getterImplementationsCreated = false;
    private static boolean settersCreated = false;          // <-- maintain state so saveapi APIs don't have to be created more than once
    private static boolean migrationsCreated = false;          // <-- maintain state so migrations don't have to be created more than once
    private static boolean tableCreatorClassCreated = false;   // <-- maintain state so TableCreator class does not have to be created more than once
    private static boolean finderClassesCreated = false;       // <-- maintain state so finder classes don't have to be created more than once
    private static boolean orderByClassesCreated = false;      // <-- maintain state so orderby classes don't have to be created more than once
    private static boolean resolverClassesCreated = false;     // <-- maintain state so resolver classes don't have to be created more than once
    private static boolean forSureClassCreated = false;        // <-- maintain state so ForSure doesn't have to be created more than once

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<TypeElement> tableTypes = ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(FSTable.class));
        if (tableTypes.size() == 0) {
            return true;
        }

        APLog.init(processingEnv);
        AnnotationTranslatorFactory.init(processingEnv);
        APLog.i(LOG_TAG, "Running FSAnnotationProcessor");

        PropertyRetriever.init(processingEnv);

        processFSTableAnnotations(tableTypes);

        return true;
    }

    private void processFSTableAnnotations(Set<TypeElement> tableTypes) {
        if (!shouldProcessAnything()) {
            APLog.i(LOG_TAG, "Already processed all resources");
            return;
        }
        ProcessingContext pc = new ProcessingContext(tableTypes);

        if (!getterImplementationsCreated) {
            createGetterApis(pc);
        }

        if (!settersCreated) {
            createSetters(pc);
        }
        if (!migrationsCreated && properties().createMigrations()) {
            createMigrations(pc);
        }
        if (!tableCreatorClassCreated) {
            createTableCreatorClass(pc);
        }
        if (!orderByClassesCreated) {
            createOrderByClasses(pc);
        }
        if (!finderClassesCreated) {
            createFinderClasses(pc);
        }
        if (!resolverClassesCreated) {
            createResolverClasses(pc);
        }
        if (!forSureClassCreated) {
            createForSureClass(pc);
        }
    }

    private void createGetterApis(ProcessingContext pc) {
        APLog.i(LOG_TAG, "creating getter apis");
        for (TableInfo table : pc.allTables()) {
            GetterGenerator.getFor(processingEnv, table).generate();
        }
        getterImplementationsCreated = true;
    }

    private static boolean shouldProcessAnything() {
        return !settersCreated
                || !migrationsCreated
                || !tableCreatorClassCreated
                || !orderByClassesCreated
                || !finderClassesCreated
                || !resolverClassesCreated
                || !forSureClassCreated;
    }

    private void createSetters(ProcessingContext pc) {
        for (TableInfo tableInfo : pc.allTables()) {
            SetterGenerator.getFor(processingEnv, tableInfo).generate();
        }
        settersCreated = true;   // <-- maintain state so saveapi APIs don't have to be created more than once
    }

    private void createMigrations(ProcessingContext pc) {
        APLog.i(LOG_TAG, "got migration directory: " + properties().migrationDirectory());
        new MigrationGenerator(processingEnv, properties().migrationDirectory(), pc).generate();
        migrationsCreated = true;   // <-- maintain state so migrations don't have to be created more than once
    }

    private void createTableCreatorClass(ProcessingContext pc) {
        APLog.i(LOG_TAG, "got applicationPackageName: " + properties().applicationPackage());
        new TableCreatorGenerator(processingEnv, properties().applicationPackage(), pc.allTables()).generate();
        tableCreatorClassCreated = true;    // <-- maintain state so TableCreator class does not have to be created more than once
    }

    private void createOrderByClasses(ProcessingContext pc) {
        for (TableInfo tableInfo : pc.allTables()) {
            OrderByGenerator.create(processingEnv, tableInfo).generate();
        }
        orderByClassesCreated = true;    // <-- maintain state so orderby classes don't have to be created more than once
    }

    private void createFinderClasses(ProcessingContext pc) {
        for (TableInfo tableInfo : pc.allTables()) {
            new FinderGenerator(processingEnv, tableInfo).generate();
        }
        finderClassesCreated = true;    // <-- maintain state so finder classes don't have to be created more than once
    }

    private void createResolverClasses(ProcessingContext pc) {
        for (TableInfo tableInfo : pc.allTables()) {
            new ResolverGenerator(processingEnv, tableInfo, pc).generate();
        }
        resolverClassesCreated = true;
    }

    private void createForSureClass(ProcessingContext pc) {
        new ForSureGenerator(processingEnv, properties().applicationPackage(), pc).generate();
        forSureClassCreated = true; // <-- maintain state so ForSure doesn't have to be created more than once
    }
}
