package com.mikedll.headshot;

import java.io.IOException;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.Assertions;

public class DbSetup implements BeforeAllCallback, BeforeEachCallback {
    
    private DbSuite suite() {
        return TestSuite.getSuite(DbSuite.class);
    }

    @Override
    public void beforeAll(ExtensionContext context) throws IOException {
        suite().setUp();
    }

    @Override
    public void beforeEach(ExtensionContext context) throws IOException {
        if(!suite().beforeTest()) {
            Assertions.fail("suite beforeTest");
        }
    }    

}
