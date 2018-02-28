package com.fsyran.forsuredb.integrationtest.singletable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.fsryan.forsuredb.integrationtest.ForSure.allTypesTable;

public class EnsureAllTypesTableEmptyBeforeClass implements BeforeAllCallback {

    private static final Logger log = LogManager.getLogger(EnsureAllTypesTableEmptyBeforeClass.class);

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        log.debug("clearing all_types table");
        allTypesTable().set().hardDelete();
    }
}
