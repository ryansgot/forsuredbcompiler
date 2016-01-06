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
package com.forsuredb.annotationprocessor.generator.resource;

import com.forsuredb.annotationprocessor.ProcessingContext;
import com.forsuredb.annotationprocessor.generator.BaseGenerator;
import com.forsuredb.annotationprocessor.generator.DiffGenerator;
import com.forsuredb.annotationprocessor.util.APLog;
import com.forsuredb.api.FSLogger;
import com.forsuredb.migration.MigrationContext;
import com.forsuredb.migration.MigrationRetriever;
import com.forsuredb.migration.MigrationRetrieverFactory;

import com.forsuredb.migration.MigrationSet;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.velocity.VelocityContext;

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

    public MigrationGenerator(ProcessingContext pContext, String migrationDirectory, ProcessingEnvironment processingEnv)  {
            super("migration_resource.vm", processingEnv);
            date = new Date();
            this.pContext = pContext;
            mr = new MigrationRetrieverFactory(new FSLogger.DefaultFSLogger()).fromDirectory(migrationDirectory);
    }

    @Override
    protected FileObject createFileObject(ProcessingEnvironment processingEnv) throws IOException {
        return new ResourceCreator(getRelativeFileName()).create(processingEnv);
    }

    @Override
    protected VelocityContext createVelocityContext() {
        MigrationSet migrationSet = new DiffGenerator(new MigrationContext(mr), mr.latestDbVersion()).analyzeDiff(pContext);
        APLog.i(LOG_TAG, "Number of migrations in set = " + migrationSet.getOrderedMigrations().size());
        if (migrationSet.getOrderedMigrations().size() == 0) {
            return null;
        }

        final String migrationSetJson = new Gson().toJson(migrationSet, new TypeToken<MigrationSet>() {}.getType());

        VelocityContext vc = new VelocityContext();
        vc.put("migrationJson", migrationSetJson);
        return vc;
    }

    private String getRelativeFileName() {
        return date.getTime() + ".migration";
    }
}
