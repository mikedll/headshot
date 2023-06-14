package com.mikedll.headshot;

import java.io.IOException;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.Assertions;

public class DbSetup implements BeforeAllCallback, BeforeEachCallback {

    @Override
    public void beforeAll(ExtensionContext context) throws IOException {
        DbSuite.setUp();
    }

    @Override
    public void beforeEach(ExtensionContext context) throws IOException {
        if(!DbSuite.beforeDbTest()) {
            Assertions.fail("setup failed");
        }
    }    

}
