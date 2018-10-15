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
package com.fsryan.forsuredb.annotationprocessor.generator.resource;

import com.fsryan.forsuredb.annotationprocessor.ProcessingContext;
import com.fsryan.forsuredb.annotationprocessor.TableContextFactory;
import com.fsryan.forsuredb.annotationprocessor.generator.BaseGenerator;
import com.fsryan.forsuredb.annotationprocessor.generator.DiffGenerator;
import com.fsryan.forsuredb.annotationprocessor.util.APLog;
import com.fsryan.forsuredb.api.FSLogger;
import com.fsryan.forsuredb.api.migration.MigrationRetriever;
import com.fsryan.forsuredb.api.migration.MigrationRetrieverFactory;
import com.fsryan.forsuredb.migration.MigrationContext;
import com.fsryan.forsuredb.migration.MigrationSet;

import com.fsryan.forsuredb.gsonserialization.FSDbInfoGsonSerializer;

import java.io.IOException;
import java.util.Date;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;

/**
 * <p>
 *     Used for generating the appropriate migration XML resource corresponding to the difference
 *     between the accumulated migrations in the migration directory used to instantiate this object
 *     and the {@link ProcessingContext ProcessingContext} used to instantiate the object.
 * </p>
 * @author Ryan Scott
 */
public class MigrationGenerator extends BaseGenerator<FileObject> {

    private static final String LOG_TAG = MigrationGenerator.class.getSimpleName();

    private final Date date;
    private final ProcessingContext pContext;
    private final MigrationRetriever mr;

    public MigrationGenerator(ProcessingEnvironment processingEnv, String migrationDirectory, ProcessingContext pContext)  {
        super(processingEnv);
        date = new Date();
        this.pContext = pContext;
        mr = new MigrationRetrieverFactory(new FSDbInfoGsonSerializer(), FSLogger.TO_SYSTEM_OUT).fromDirectory(migrationDirectory);
    }

    @Override
    protected FileObject createFileObject(ProcessingEnvironment processingEnv) throws IOException {
        return new ResourceCreator(getRelativeFileName()).create(processingEnv);
    }

    @Override
    protected String getCode() {
        // TODO: TableContextFactory.createFromMigrationRetriever(mr)
        MigrationSet migrationSet = new DiffGenerator(new MigrationContext(mr), mr.latestDbVersion()).analyzeDiff(pContext);
        APLog.i(LOG_TAG, "Number of migrations in set = " + migrationSet.orderedMigrations().size());
        if (migrationSet.orderedMigrations().size() == 0) {
            return null;
        }

        return new FSDbInfoGsonSerializer().serialize(migrationSet);
    }

    private String getRelativeFileName() {
        return date.getTime() + ".migration";
    }
}
