package com.fsyran.forsuredb.integrationtest.singletable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.fsryan.forsuredb.integrationtest.ForSure.allTypesTable;

public class EnsureAllTypesTableEmptyBeforeTest implements BeforeEachCallback {

    private static final Logger log = LogManager.getLogger(EnsureAllTypesTableEmptyBeforeTest.class);

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        log.debug("clearing all_types table");
        allTypesTable().set().hardDelete();
    }
}
