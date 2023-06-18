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
    public void testTrailingRegex() {
        Matcher matcher = PathUtils.PATH_TRAILING_PATTERN.matcher("bin");
        Assertions.assertFalse(matcher.find(), "no slash");
        
        matcher = PathUtils.PATH_TRAILING_PATTERN.matcher("tests/bin");
        Assertions.assertTrue(matcher.find(), "found trailing path component");
        Assertions.assertEquals("/bin", matcher.group(0));

        matcher = PathUtils.PATH_TRAILING_PATTERN.matcher("src/utils/scissors");
        Assertions.assertTrue(matcher.find(), "found trailing path component");
        Assertions.assertEquals("/scissors", matcher.group(0));
    }

    @Test
    public void testAncestors() {
        List<PathAncestor> found = PathUtils.pathAncestors("cknife", "bin");
        List<PathAncestor> expected = new ArrayList<>();
        expected.add(new PathAncestor("cknife", ""));
        Assertions.assertEquals(expected, found, "single component");

        found = PathUtils.pathAncestors("cknife", "bin/dangerous");
        expected = new ArrayList<>();
        expected.add(new PathAncestor("cknife", ""));
        expected.add(new PathAncestor("bin", "bin"));
        Assertions.assertEquals(expected, found, "two components");

        found = PathUtils.pathAncestors("cknife", "src/util/scissors");
        expected = new ArrayList<>();
        expected.add(new PathAncestor("cknife", ""));
        expected.add(new PathAncestor("src", "src"));
        expected.add(new PathAncestor("util", "src/util"));
        Assertions.assertEquals(expected, found, "three components");
        
    }
    
}
