package com.fsryan.forsuredb.resources;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Resources {

    public static List<URL> getResourceURLs() throws IOException, URISyntaxException {
        return getResourceURLs((ResourceURLFilter)null);
    }

    public static List<URL> getResourceURLs(Class rootClass) throws IOException, URISyntaxException {
        return getResourceURLs(rootClass, (ResourceURLFilter)null);
    }

    public static List<URL> getResourceURLs(ResourceURLFilter filter)  throws IOException, URISyntaxException {
        List<URL> collectedURLs = new ArrayList<>();
        URLClassLoader ucl = (URLClassLoader)ClassLoader.getSystemClassLoader();
        for (URL url: ucl.getURLs()) {
            iterateEntry(new File(url.toURI()), filter, collectedURLs);
        }
        return collectedURLs;
    }

    public static List<URL> getResourceURLs(Class rootClass, ResourceURLFilter filter) throws IOException, URISyntaxException {
        List<URL> collectedURLs = new ArrayList<>();
        CodeSource src = rootClass.getProtectionDomain().getCodeSource();
        iterateEntry(new File(src.getLocation().toURI()), filter, collectedURLs);
        return collectedURLs;
    }

    private static void iterateEntry(File entryFile, ResourceURLFilter filter, List<URL> destination) throws IOException {
        if (entryFile.isDirectory()) {
            iterateFileSystem(entryFile, filter, destination);
        } else if (entryFile.isFile() && entryFile.getName().toLowerCase().endsWith(".jar")) {
            iterateJarFile(entryFile, filter, destination);
        }
    }

    private static void collectURL(ResourceURLFilter filter, List<URL> destination, URL url) {
        if (filter != null && !filter.accept(url)) {
            return;
        }
        destination.add(url);
    }

    private static void iterateFileSystem(File entryFile, ResourceURLFilter filter, List<URL> destination) throws IOException {
        File[] files = entryFile.listFiles();
        if (files == null) {
            return;
        }

        for (File file: files) {
            if (file.isDirectory()) {
                iterateFileSystem(file, filter, destination);
            } else if (file.isFile()) {
                collectURL(filter, destination, file.toURI().toURL());
            }
        }
    }

    private static void iterateJarFile(File file, ResourceURLFilter filter, List<URL> destination)  throws IOException {
        try (JarFile jFile = new JarFile(file)) {
            Collections.list(jFile.entries()).stream()
                    .filter(jarEntry -> !jarEntry.isDirectory())
                    .map(jarEntryURLMapper(file))
                    .forEach(url -> collectURL(filter, destination, url));
        }
    }

    private static Function<JarEntry, URL> jarEntryURLMapper(File parent) {
        return (JarEntry je) -> {
            try {
                return new URL("jar", "", parent.toURI() + "!/" + je.getName());
            } catch (MalformedURLException mfe) {
                throw new RuntimeException(mfe);
            }
        };
    }
}
