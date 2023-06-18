package com.mikedll.headshot.controller;

import java.io.IOException;
import jakarta.servlet.ServletException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import com.mikedll.headshot.SimpleSuite;
import com.mikedll.headshot.TestSuite;

public class ParamPathTests {

    @BeforeEach
    public void beforeEach() throws IOException {
        SimpleSuite suite = TestSuite.getSuite(SimpleSuite.class);
        suite.setUp();

        if(!suite.beforeEach()) {
            Assertions.fail("suite beforeTest");
        }
    }
    
    @Test
    public void testPathParams() throws IOException, ServletException {
        Servlet servlet = new Servlet();
        
        TestRequest request = ControllerUtils.get("/animals/giraffe");

        servlet.doGet(request.req(), request.res());

        request.printWriter().flush();
        Assertions.assertTrue(request.stringWriter().toString().contains("Mike is here"), "found basic output");
    }

}
