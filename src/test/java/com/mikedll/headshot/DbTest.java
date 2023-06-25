package com.mikedll.headshot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;

public class DbTest {
    @BeforeEach
    public void beforeEach() {
        if(!TestSuite.getSuite(DbSuite.class).beforeEach()) {
            Assertions.fail("beforeEach failed");
        }
    }
}
