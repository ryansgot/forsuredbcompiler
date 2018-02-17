package com.fsyran.forsuredb.integrationtest;

import com.fsryan.forsuredb.integrationtest.Something;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({ExecutionLog.class, DBSetup.class})
public class SomethingTest {

    private static final Logger log = LogManager.getLogger(SomethingTest.class);

    @Test
    public void should(TestInfo testInfo) {
        log.debug("TestInfo: " + testInfo);
        Something.doSomething();
        assertEquals("should", testInfo.getTestMethod().get().getName());
    }
}
