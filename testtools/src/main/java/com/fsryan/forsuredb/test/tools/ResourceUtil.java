package com.fsryan.forsuredb.test.tools;

import java.io.*;

public class ResourceUtil {

    private static final String DEFAULT_RESOURCE_DIR = "src" + File.separator + "test" + File.separator + "resources";

    public static String resourceText(String resourceName) throws IOException {
        return resourceText(DEFAULT_RESOURCE_DIR, resourceName);
    }

    public static String resourceText(String path, String name) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path + File.separator + name));
        String line;
        StringBuilder out = new StringBuilder();
        while (null != (line = br.readLine())) {
            out.append(line).append("\n");
        }
        br.close();
        return out.toString();
    }
}
