package com.fsyran.forsuredb.integrationtest;

import com.fsryan.forsuredb.api.FSLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log4JFSLogger implements FSLogger {

    private final Logger log;

    public Log4JFSLogger(String name) {
        log = LogManager.getLogger(name);
    }


    @Override
    public void e(String message, Object... replacements) {
        log.error(String.format(message, replacements));
    }

    @Override
    public void i(String message, Object... replacements) {
        log.info(String.format(message, replacements));
    }

    @Override
    public void w(String message, Object... replacements) {
        log.warn(String.format(message, replacements));
    }

    @Override
    public void o(String message, Object... replacements) {
        log.debug(String.format(message, replacements));
    }
}
