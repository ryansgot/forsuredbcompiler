/*
   forsuredb, an object relational mapping tool

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
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

            List<Migration> migrations;

            @Override
            public List<Migration> getMigrations() {
                if (migrations == null) {
                    migrations = createMigrations();
                }
                return migrations;
            }

            @Override
            public int latestDbVersion() {
                if (migrations == null) {
                    createMigrations();
                }
                return latestDbVersionFrom(migrations);
            }

            private List<Migration> createMigrations() {
                JsonReader reader = new JsonReader(new InputStreamReader(inputStream));
                try {
                    return gson.fromJson(reader, new TypeToken<List<Migration>>() {}.getType());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        reader.close();
                    } catch (IOException e) {}
                }
                return Collections.EMPTY_LIST;
            }
        };
    }

    private int latestDbVersionFrom(List<Migration> migrations) {
        int latest = 0;
        for (Migration migration : migrations) {
            latest = migration.getDbVersion() > latest ? migration.getDbVersion() : latest;
        }
        return latest;
    }

    public MigrationRetriever fromDirectory(final String directoryName) {
        return new DirectoryMigrationsRetriever(directoryName);
    }

    private class DirectoryMigrationsRetriever implements MigrationRetriever {

        private final File directory;
        private List<Migration> migrations;

        public DirectoryMigrationsRetriever(String directory) {
            this.directory = directory == null ? null : new File(directory);
        }

        @Override
        public List<Migration> getMigrations() {
            if (migrations == null) {
                migrations = createMigrations();
            }
            return migrations;
        }

        @Override
        public int latestDbVersion() {
            if (migrations == null) {
                migrations = createMigrations();
            }
            return latestDbVersionFrom(migrations);
        }

        private List<Migration> createMigrations() {
            if (!validDirectory()) {
                log.w("directory " + directory + " either doesn't exist or isn't a directory");
                return Collections.EMPTY_LIST;
            }

            log.i("Looking for migrations in " + directory.getPath());
            List<Migration> retList = new ArrayList<>();
            final PriorityQueue<File> orderedFiles = new PriorityQueue<>(filterMigrationFiles(directory.listFiles()));
            while (orderedFiles.size() > 0) {
                retList.addAll(migrationsFromFile(orderedFiles.remove()));
            }

            return retList;
        }

        private List<Migration> migrationsFromFile(File file) {
            if (file == null || !file.exists()) {
                return Collections.EMPTY_LIST;
            }

            JsonReader reader = null;
            try {
                reader = new JsonReader(new FileReader(file));
            } catch (FileNotFoundException fnfe) {
                log.e("Could not parse migrations: " + fnfe.getMessage());
                return Collections.EMPTY_LIST;
            }

            try {
                return gson.fromJson(reader, new TypeToken<List<Migration>>() {}.getType());
            } finally {
                try {
                    reader.close();
                } catch (IOException ioe) {}
            }
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
