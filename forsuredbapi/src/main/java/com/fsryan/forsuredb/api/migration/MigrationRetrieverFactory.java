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
package com.fsryan.forsuredb.api.migration;

import com.fsryan.forsuredb.api.FSLogger;
import com.fsryan.forsuredb.migration.MigrationSet;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class MigrationRetrieverFactory {

    /*package*/ static final Type migrationSetType = new TypeToken<MigrationSet>() {}.getType();
    /*package*/ static final Gson gson = new Gson();

    private final FSLogger log;

    public MigrationRetrieverFactory() {
        this(null);
    }

    public MigrationRetrieverFactory(FSLogger log) {
        this.log = log == null ? new FSLogger.SilentLog() : log;
    }

    public MigrationRetriever fromStream(final InputStream inputStream) {
        return new RetrieverFromStream(inputStream);
    }

    private int latestDbVersionFrom(List<MigrationSet> migrationSets) {
        if (migrationSets == null) {
            return 0;
        }
        int latest = 0;
        for (MigrationSet migrationSet : migrationSets) {
            latest = migrationSet.dbVersion() > latest ? migrationSet.dbVersion() : latest;
        }
        return latest;
    }

    public MigrationRetriever fromDirectory(final String directoryName) {
        return new DirectoryMigrationsRetriever(directoryName);
    }

    private static class RetrieverFromStream implements MigrationRetriever {

        private InputStream inputStream;

        public RetrieverFromStream(final InputStream inputStream) {
            this.inputStream = inputStream;
        }

        private MigrationSet migrationSet;

        @Override
        public List<MigrationSet> getMigrationSets() {
            List<MigrationSet> ret = new ArrayList<>(1);
            if (migrationSet == null) {
                migrationSet = createMigrationSet();
            }
            ret.add(migrationSet);
            return ret;
        }

        @Override
        public int latestDbVersion() {
            if (migrationSet == null) {
                migrationSet = createMigrationSet();
            }
            return migrationSet.dbVersion();
        }

        private MigrationSet createMigrationSet() {
            JsonReader reader = null;
            try {
                reader = new JsonReader(new InputStreamReader(inputStream));
                return gson.fromJson(reader, migrationSetType);
            } catch (JsonIOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // return a dummy MigrationSet if the error was found
            return MigrationSet.builder().dbVersion(-1).build();
        }
    }

    private class DirectoryMigrationsRetriever implements MigrationRetriever {

        private final File directory;
        private List<MigrationSet> migrationSets;

        public DirectoryMigrationsRetriever(String directory) {
            this.directory = directory == null ? null : new File(directory);
        }

        @Override
        public List<MigrationSet> getMigrationSets() {
            if (migrationSets == null) {
                migrationSets = createMigrationSets();
            }
            return migrationSets;
        }

        @Override
        public int latestDbVersion() {
            if (migrationSets == null) {
                migrationSets = createMigrationSets();
            }
            return latestDbVersionFrom(migrationSets);
        }

        private List<MigrationSet> createMigrationSets() {
            if (!validDirectory()) {
                log.w("directory " + directory + " either doesn't exist or isn't a directory");
                return Collections.emptyList();
            }

            log.i("Looking for migrations in " + directory.getPath());
            List<MigrationSet> retList = new ArrayList<>();
            final PriorityQueue<File> orderedFiles = new PriorityQueue<>(filterMigrationFiles(directory.listFiles()));
            while (orderedFiles.size() > 0) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(orderedFiles.remove());
                    retList.addAll(fromStream(fis).getMigrationSets());
                } catch (FileNotFoundException fnfe) {
                    fnfe.printStackTrace();
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException ioe) {}
                    }
                }
            }

            return retList;
        }

        private List<File> filterMigrationFiles(File[] files) {
            if (files == null) {
                return Collections.emptyList();
            }

            List<File> retList = new LinkedList<>();
            for (File f : files) {
                if (isMigration(f)) {
                    retList.add(f);
                }
            }

            return retList;
        }

        private boolean isMigration(File f) {
            return f != null && f.exists() && f.isFile() && f.getPath().endsWith("migration.json");
        }

        private boolean validDirectory() {
            return directory != null && directory.exists() && directory.isDirectory();
        }
    }
}
