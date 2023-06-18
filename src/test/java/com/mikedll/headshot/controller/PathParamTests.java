package com.mikedll.headshot.controller;

import java.io.IOException;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import jakarta.servlet.ServletException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.javatuples.Pair;

import com.mikedll.headshot.SimpleSuite;
import com.mikedll.headshot.TestSuite;

public class PathParamTests {

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

    @Test
    public void testMatching() {
        PathParamMatcher result = PathParamMatcher.build("/{id}/gives/{food}/to/{name}").orElse(null);
        Map<String,String> expected = new HashMap<>();
        expected.put("id", "mike");
        expected.put("food", "banana");
        expected.put("name", "jane");
        PathMatch pathMatch = result.match("/mike/gives/banana/to/jane").orElse(null);
        Assertions.assertEquals("/mike/gives/banana/to/jane", pathMatch.matched(), "basic test path");
        Assertions.assertEquals(expected, pathMatch.extractedParams(), "basic test");

        pathMatch = result.match("/mike/doesnotgive/banana/to/jane").orElse(null);
        Assertions.assertNull(pathMatch, "no match");

        result = PathParamMatcher.build("/animals/{id}").orElse(null);
        Assertions.assertNotNull(result, "simpler match");
        pathMatch = result.match("/animals/35").orElse(null);
        expected = new HashMap<>();
        expected.put("id", "35");
        Assertions.assertEquals(expected, pathMatch.extractedParams(), "simple match params");
        Assertions.assertEquals("/animals/35", pathMatch.matched(), "simple match path");

        Assertions.assertNotNull(result.match("/animals/35/").orElse(null), "trailing slash match");

        Assertions.assertNull(result.match("/animals/").orElse(null), "not exact match");
    }
    
    @Test
    public void testHandler() throws IOException, ServletException {
        Servlet servlet = new Servlet();
        
        TestRequest request = ControllerUtils.get("/animals/giraffe");

        servlet.doGet(request.req(), request.res());

        request.printWriter().flush();
        Assertions.assertTrue(request.stringWriter().toString().contains("This animal's name is: giraffe"), "found basic output");
    }

}
