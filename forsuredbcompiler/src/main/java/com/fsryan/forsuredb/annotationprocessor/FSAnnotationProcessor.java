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

import com.fsryan.forsuredb.annotationprocessor.generator.code.OrderByGenerator;
import com.fsryan.forsuredb.annotationprocessor.generator.code.saveapi.SaveApiGenerator;
import com.fsryan.forsuredb.annotationprocessor.generator.resource.MigrationGenerator;
import com.fsryan.forsuredb.annotationprocessor.generator.code.FinderGenerator;
import com.fsryan.forsuredb.annotationprocessor.generator.code.ForSureGenerator;
import com.fsryan.forsuredb.annotationprocessor.generator.code.resolver.ResolverGenerator;
import com.fsryan.forsuredb.annotationprocessor.generator.code.TableCreatorGenerator;
import com.fsryan.forsuredb.annotations.FSTable;
import com.fsryan.forsuredb.api.info.TableInfo;
import com.fsryan.forsuredb.annotationprocessor.util.APLog;
import com.fsryan.forsuredb.annotationprocessor.util.AnnotationTranslatorFactory;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

/**
 * <p>
 *     FSAnnotationProcessor is the guts of the forsuredb project. When you compile, the
 *     {@link #process(Set, RoundEnvironment)} gets called, and the annotation processing begins.
 * </p>
 * @author Ryan Scott
 */
@SupportedAnnotationTypes("com.fsryan.forsuredb.annotations.*")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class FSAnnotationProcessor extends AbstractProcessor {

    private static final String LOG_TAG = FSAnnotationProcessor.class.getSimpleName();

    private static boolean setterApisCreated = false;          // <-- maintain state so saveapi APIs don't have to be created more than once
    private static boolean migrationsCreated = false;          // <-- maintain state so migrations don't have to be created more than once
    private static boolean tableCreatorClassCreated = false;   // <-- maintain state so TableCreator class does not have to be created more than once
    private static boolean finderClassesCreated = false;       // <-- maintain state so finder classes don't have to be created more than once
    private static boolean orderByClassesCreated = false;      // <-- maintain state so orderby classes don't have to be created more than once
    private static boolean resolverClassesCreated = false;     // <-- maintain state so resolver classes don't have to be created more than once
    private static boolean forSureClassCreated = false;        // <-- maintain state so ForSure doesn't have to be created more than once

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<TypeElement> tableTypes = ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(FSTable.class));
        if (tableTypes == null || tableTypes.size() == 0) {
            return true;
        }

        APLog.init(processingEnv);
        AnnotationTranslatorFactory.init(processingEnv);

        APLog.i(LOG_TAG, "Running FSAnnotationProcessor");
        processFSTableAnnotations(tableTypes);

        return true;
    }

    private void processFSTableAnnotations(Set<TypeElement> tableTypes) {
        ProcessingContext pc = new ProcessingContext(tableTypes);

        if (!setterApisCreated) {
            createSetterApis(pc);
        }
        if (!migrationsCreated && Boolean.getBoolean("createMigrations")) {
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

    private void createSetterApis(ProcessingContext pc) {
        for (TableInfo tableInfo : pc.allTables()) {
            SaveApiGenerator.getFor(processingEnv, tableInfo).generate();
        }
        setterApisCreated = true;   // <-- maintain state so saveapi APIs don't have to be created more than once
    }

    private void createMigrations(ProcessingContext pc) {
        String migrationDirectory = System.getProperty("migrationDirectory");
        APLog.i(LOG_TAG, "got migration directory: " + migrationDirectory);
        new MigrationGenerator(processingEnv, migrationDirectory, pc).generate();
        migrationsCreated = true;   // <-- maintain state so migrations don't have to be created more than once
    }

    private void createTableCreatorClass(ProcessingContext pc) {
        String applicationPackageName = System.getProperty("applicationPackageName");
        APLog.i(LOG_TAG, "got applicationPackageName: " + applicationPackageName);
        new TableCreatorGenerator(processingEnv, applicationPackageName, pc.allTables()).generate();
        tableCreatorClassCreated = true;    // <-- maintain state so TableCreator class does not have to be created more than once
    }

    private void createOrderByClasses(ProcessingContext pc) {
        for (TableInfo tableInfo : pc.allTables()) {
            new OrderByGenerator(processingEnv, tableInfo).generate();
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
            ResolverGenerator.getFor(processingEnv, tableInfo, pc).generate();
        }
        resolverClassesCreated = true;
    }

    private void createForSureClass(ProcessingContext pc) {
        String applicationPackageName = System.getProperty("applicationPackageName");
        new ForSureGenerator(processingEnv, applicationPackageName, pc).generate();
        forSureClassCreated = true; // <-- maintain state so ForSure doesn't have to be created more than once
    }
}
