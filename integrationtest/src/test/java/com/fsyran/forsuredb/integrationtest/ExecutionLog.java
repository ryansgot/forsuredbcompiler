package com.fsyran.forsuredb.integrationtest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.*;

public class ExecutionLog implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private static final Logger log = LogManager.getLogger(ExecutionLog.class);

    @Override
    public void afterTestExecution(ExtensionContext context) {
        long finished = System.nanoTime();
        long started = context.getStore(ExtensionContext.Namespace.GLOBAL).get("current_time", Long.class);
        long amount = finished - started;
        long millis = amount / 1000000;
        long nanos = amount % 1000000;
        log.debug(context.getDisplayName() + ": " + millis + "." + nanos + " ms");
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        context.getStore(ExtensionContext.Namespace.GLOBAL).put("current_time", System.nanoTime());
    }
}
