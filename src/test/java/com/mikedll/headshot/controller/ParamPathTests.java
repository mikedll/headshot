package com.mikedll.headshot.controller;

import java.io.IOException;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import jakarta.servlet.ServletException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.javatuples.Pair;

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
    public void testPattern() {
        Matcher matcher = PathParamMatcher.PATTERN.matcher("/animal");
        Assertions.assertFalse(matcher.find(), "no param");

        matcher = PathParamMatcher.PATTERN.matcher("/animal/{id}");
        Assertions.assertTrue(matcher.find(), "id param");

        matcher = PathParamMatcher.PATTERN.matcher("/animal/{id}/hello");
        Assertions.assertTrue(matcher.find(), "id param with trailing stuff");        
    }
    
    @Test
    public void testBuilder() {
        Optional<PathParamMatcher> result = PathParamMatcher.build("/animals");
        Assertions.assertFalse(result.isPresent(), "no result");

        result = PathParamMatcher.build("/animals/{id}");
        List<String> expectedParamNames = new ArrayList<>();
        expectedParamNames.add("id");
        Assertions.assertTrue(result.isPresent(), "simple id capture");
        Assertions.assertEquals(expectedParamNames, result.get().paramNames(), "param names");

        result = PathParamMatcher.build("/animals/{id}/give/{food}");
        expectedParamNames = new ArrayList<>();
        expectedParamNames.add("id");
        expectedParamNames.add("food");
        Assertions.assertTrue(result.isPresent(), "two param capture");
        Assertions.assertEquals(expectedParamNames, result.get().paramNames(), "param names");

        result = PathParamMatcher.build("{id}/gives/{food}/to/{name}");
        expectedParamNames = new ArrayList<>();
        expectedParamNames.add("id");
        expectedParamNames.add("food");
        expectedParamNames.add("name");
        Assertions.assertTrue(result.isPresent(), "three param capture");
        Assertions.assertEquals(expectedParamNames, result.get().paramNames(), "param names");
    }

    @Test public void testMatching() {
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
