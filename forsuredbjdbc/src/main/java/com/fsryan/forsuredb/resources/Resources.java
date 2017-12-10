package com.fsryan.forsuredb.resources;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Resources {

    public static Set<URL> getResourceURLs() throws IOException, URISyntaxException {
        return getResourceURLs((ResourceURLFilter)null);
    }

    public static Set<URL> getResourceURLs(Class rootClass) throws IOException, URISyntaxException {
        return getResourceURLs(rootClass, (ResourceURLFilter)null);
    }

    public static Set<URL> getResourceURLs(ResourceURLFilter filter)  throws IOException, URISyntaxException {
        Set<URL> collectedURLs = new HashSet<>();
        URLClassLoader ucl = (URLClassLoader)ClassLoader.getSystemClassLoader();
        for (URL url: ucl.getURLs()) {
            iterateEntry(new File(url.toURI()), filter, collectedURLs);
        }
        return collectedURLs;
    }

    public static Set<URL> getResourceURLs(Class rootClass, ResourceURLFilter filter) throws IOException, URISyntaxException {
        Set<URL> collectedURLs = new HashSet<>();
        CodeSource src = rootClass.getProtectionDomain().getCodeSource();
        iterateEntry(new File(src.getLocation().toURI()), filter, collectedURLs);
        return collectedURLs;
    }

    private static void iterateEntry(File entryFile, ResourceURLFilter filter, Set<URL> destination) throws IOException {
        if (entryFile.isDirectory()) {
            iterateFileSystem(entryFile, filter, destination);
        } else if (entryFile.isFile() && entryFile.getName().toLowerCase().endsWith(".jar")) {
            iterateJarFile(entryFile, filter, destination);
        }
    }

    private static void collectURL(ResourceURLFilter filter, Set<URL> destination, URL url) {
        if (filter != null && !filter.accept(url)) {
            return;
        }
        destination.add(url);
    }

    private static void iterateFileSystem(File entryFile, ResourceURLFilter filter, Set<URL> destination) throws IOException {
        File[] files = entryFile.listFiles();
        for (File file: files) {
            if (file.isDirectory()) {
                iterateFileSystem(file, filter, destination);
            } else if (file.isFile()) {
                collectURL(filter, destination, file.toURI().toURL());
            }
        }
    }

    private static void iterateJarFile(File file, ResourceURLFilter filter, Set<URL> destination)  throws IOException {
        JarFile jFile = new JarFile(file);
        for(Enumeration<JarEntry> je = jFile.entries(); je.hasMoreElements();) {
            JarEntry j = je.nextElement();
            if (!j.isDirectory()) {
                final URL url = new URL("jar", "",file.toURI() + "!/" + j.getName());
                collectURL(filter, destination, url);
            }
        }
    }
}
