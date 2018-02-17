package com.fsyran.forsuredb.integrationtest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ExecutionLog implements BeforeEachCallback, AfterEachCallback {

    private static final Logger log = LogManager.getLogger(ExecutionLog.class);

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        log.debug("Starting: " + context.getDisplayName());
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        log.debug("Finished: " + context.getDisplayName());
    }
}
