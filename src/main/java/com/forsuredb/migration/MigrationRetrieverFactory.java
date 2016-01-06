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
package com.forsuredb.migration;

import com.forsuredb.api.FSLogger;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class MigrationRetrieverFactory {

    private static final Gson gson = new Gson();

    private final FSLogger log;

    public MigrationRetrieverFactory() {
        this(null);
    }

    public MigrationRetrieverFactory(FSLogger log) {
        this.log = log == null ? new FSLogger.SilentLog() : log;
    }

    public MigrationRetriever fromStream(final InputStream inputStream) {
        return new MigrationRetriever() {

            private MigrationSet migrationSet;

            @Override
            public List<MigrationSet> getMigrationSets() {
                if (migrationSet == null) {
                    migrationSet = createMigrationSet();
                }
                return Lists.newArrayList(migrationSet);
            }

            @Override
            public int latestDbVersion() {
                return migrationSet.getDbVersion();
            }

            private MigrationSet createMigrationSet() {
                JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
                try {
                    return gson.fromJson(reader, new TypeToken<MigrationSet>() {}.getType());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        reader.close();
                    } catch (IOException e) {}
                }

                // return a dummy MigrationSet if the error was found
                return MigrationSet.builder().dbVersion(-1).build();
            }
        };
    }

    private int latestDbVersionFrom(List<MigrationSet> migrationSets) {
        if (migrationSets == null) {
            return 0;
        }
        int latest = 0;
        for (MigrationSet migrationSet : migrationSets) {
            latest = migrationSet.getDbVersion() > latest ? migrationSet.getDbVersion() : latest;
        }
        return latest;
    }

    public MigrationRetriever fromDirectory(final String directoryName) {
        return new DirectoryMigrationsRetriever(directoryName);
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
            return latestDbVersionFrom(migrationSets);
        }

        private List<MigrationSet> createMigrationSets() {
            if (!validDirectory()) {
                log.w("directory " + directory + " either doesn't exist or isn't a directory");
                return Collections.EMPTY_LIST;
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
                return Collections.EMPTY_LIST;
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
