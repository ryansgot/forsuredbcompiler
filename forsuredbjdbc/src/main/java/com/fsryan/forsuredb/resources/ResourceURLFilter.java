package com.fsryan.forsuredb.resources;

import java.net.URL;

public interface ResourceURLFilter {
    boolean accept(URL resourceUrl);
}
