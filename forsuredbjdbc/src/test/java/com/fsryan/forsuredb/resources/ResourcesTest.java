package com.fsryan.forsuredb.resources;

import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ResourcesTest {

    @Test
    public void shouldProperlyFilterAllResources() throws Exception {
        Set<String> filteredMigrationResourceFiles = Resources.getResourceURLs(new ResourceURLFilter() {
            @Override
            public boolean accept(URL resourceUrl) {
                return resourceUrl.getPath().endsWith(".migration.json");
            }
        }).stream().map(url -> {
            final String path = url.getPath();
            final int lastSeparatorIdx = path.lastIndexOf(File.separatorChar);
            return path.substring(lastSeparatorIdx + 1);
        }).collect(toSet());
        assertEquals(2, filteredMigrationResourceFiles.size());
        assertTrue(filteredMigrationResourceFiles.contains("1498420882212.migration.json"));
        assertTrue(filteredMigrationResourceFiles.contains("1498424635239.migration.json"));
    }
}
