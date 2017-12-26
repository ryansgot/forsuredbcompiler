package com.fsryan.forsuredb;

import com.fsryan.forsuredb.api.migration.MigrationRetrieverFactory;
import com.fsryan.forsuredb.migration.MigrationSet;
import com.fsryan.forsuredb.resources.ResourceURLFilter;
import com.fsryan.forsuredb.resources.Resources;
import com.fsryan.forsuredb.serialization.FSDbInfoSerializer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import static com.fsryan.forsuredb.resources.Resources.getResourceURLs;

public class Migrator {

    private static final Comparator<URL> migrationUrlComparator = new Comparator<URL>() {
        @Override
        public int compare(URL url1, URL url2) {
            return filenameFrom(url1).compareTo(filenameFrom(url2));
        }

        private String filenameFrom(URL url) {
            final String path = url.getPath();
            final int lastIdx = path.lastIndexOf(File.separatorChar);
            return path.substring(lastIdx + 1);
        }
    };

    private final FSDbInfoSerializer serializer;
    private List<MigrationSet> migrationSets;

    Migrator(FSDbInfoSerializer serializer) {
        this.serializer = serializer;
    }

    /**
     * @return a sorted list of Migration
     */
    public List<MigrationSet> getMigrationSets() {
        if (migrationSets == null) {
            createMigrationSets();
        }
        return migrationSets;
    }

    private void createMigrationSets() {
        final PriorityQueue<URL> sortedPaths = createSortedMigrationFilenames();

        migrationSets = new LinkedList<>();
        while (sortedPaths.size() > 0) {
            addMigrationsFromResource(sortedPaths.remove());
        }
    }

    private void addMigrationsFromResource(URL migrationResource) {
        InputStream in = null;
        try {
            in = migrationResource.openStream();
            migrationSets.addAll(new MigrationRetrieverFactory(serializer)
                    .fromStream(in)
                    .getMigrationSets());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    // do nothing
                }
            }
        }
    }

    private PriorityQueue<URL> createSortedMigrationFilenames() {
        PriorityQueue<URL> retQueue = new PriorityQueue<>(migrationUrlComparator);
        try {
            for (URL migrationUrl : getResourceURLs(resourceUrl -> resourceUrl.getPath().endsWith(".migration.json"))) {
                retQueue.add(migrationUrl);
            }
        } catch (IOException | URISyntaxException ioe) {
            // do logging
        }

        return retQueue;
    }
}

