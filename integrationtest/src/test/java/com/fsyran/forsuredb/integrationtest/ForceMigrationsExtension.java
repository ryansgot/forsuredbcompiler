package com.fsyran.forsuredb.integrationtest;

import com.fsryan.forsuredb.FSDBHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Field;

public class ForceMigrationsExtension implements BeforeAllCallback {

    private static final Logger log = LogManager.getLogger(ForceMigrationsExtension.class);

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        try {
            FSDBHelper.inst().getReadableDatabase().close();
            log.debug("Destroyed in-memory database");
            // resets the helper value to null
            Field f = FSDBHelper.class.getDeclaredField("instance");
            f.setAccessible(true);
            f.set(null, null);
        } catch (IllegalStateException ise) {
            // do nothing
        }
    }
}
