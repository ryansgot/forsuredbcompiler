package com.fsyran.forsuredb.integrationtest;

import com.fsryan.forsuredb.FSDBHelper;
import com.fsryan.forsuredb.ForSureJdbcInfoFactory;
import com.fsryan.forsuredb.gsonserialization.FSDbInfoGsonSerializer;
import com.fsryan.forsuredb.integrationtest.ForSure;
import com.fsryan.forsuredb.integrationtest.TableGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Field;

public class DBSetup implements BeforeAllCallback, AfterAllCallback {

    private static final Logger log = LogManager.getLogger(DBSetup.class);

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        log.debug("Creating in-memory database");

        try {
            FSDBHelper.inst();
        } catch (IllegalStateException ise) {
            FSDBHelper.initDebugSQLite("jdbc:sqlite::memory:", null, TableGenerator.generate(), new FSDbInfoGsonSerializer());
            ForSure.init(ForSureJdbcInfoFactory.inst());
        }

    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        log.debug("Destroying in-memory database");
        FSDBHelper.inst().getReadableDatabase().close();

        // resets the helper value to null
        Field f = FSDBHelper.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(null, null);
    }
}
