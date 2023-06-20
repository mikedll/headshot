package com.mikedll.headshot;

import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import com.mikedll.headshot.util.PathUtils;
import com.mikedll.headshot.util.PathAncestor;

public class PathUtilsTests {

    @Test
    public void testAncestors() {
        List<PathAncestor> found = PathUtils.pathAncestors("cknife", "bin");
        List<PathAncestor> expected = new ArrayList<>();
        expected.add(new PathAncestor("cknife", ""));
        expected.add(new PathAncestor("bin", "bin"));
        Assertions.assertEquals(expected, found, "single component");

        found = PathUtils.pathAncestors("cknife", "bin/dangerous");
        expected = new ArrayList<>();
        expected.add(new PathAncestor("cknife", ""));
        expected.add(new PathAncestor("bin", "bin"));
        expected.add(new PathAncestor("dangerous", "bin/dangerous"));
        Assertions.assertEquals(expected, found, "two components");

        found = PathUtils.pathAncestors("cknife", "src/util/scissors");
        expected = new ArrayList<>();
        expected.add(new PathAncestor("cknife", ""));
        expected.add(new PathAncestor("src", "src"));
        expected.add(new PathAncestor("util", "src/util"));
        expected.add(new PathAncestor("scissors", "src/util/scissors"));
        Assertions.assertEquals(expected, found, "three components");

        found = PathUtils.pathAncestors("cknife", "");
        expected = new ArrayList<>();
        expected.add(new PathAncestor("cknife", ""));
        Assertions.assertEquals(expected, found, "root");        
    }
    
}
