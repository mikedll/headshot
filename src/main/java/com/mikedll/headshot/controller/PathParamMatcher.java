package com.mikedll.headshot.controller;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.javatuples.Pair;

public record PathParamMatcher(Pattern pattern, List<String> paramNames) {

    public Optional<Map<String,String>> match(String path) {
        Map<String,String> matches = null;
        Matcher matcher = pattern.matcher(path);
        if(matcher.find()) {
            matches = new HashMap<>();
            for(int i = 0; i < paramNames.size(); i++) {
                matches.puth(paramNames[i], matcher.group(i+1));
            }
        }
        return Optional.ofNullable(matches);
    }
    
    public static final Pattern PATTERN = Pattern.compile("(^[^{}]*)\\{(\\w+)\\}([^{}]*)");
    
    public static Optional<PathParamMatcher> build(String path) {
        System.out.println("***** new call to build");
        String buildRegexStr = null;
        String remaining = path;
        Matcher matcher = PATTERN.matcher(remaining);
        List<String> paramNames = new ArrayList<>();
        while(matcher.find()) {
            if(buildRegexStr == null) {
                buildRegexStr = "";
            }
            String part = matcher.group(0);
            String beforeParam = matcher.group(1);
            String paramName = matcher.group(2);
            paramNames.add(paramName);
            String afterParam = matcher.group(3);
            remaining = remaining.substring(part.length(), remaining.length());
            System.out.println("part: " + part + ", remaining: " + remaining);
            buildRegexStr = buildRegexStr + Pattern.quote(beforeParam) + "(\\w+)" + Pattern.quote(afterParam);
            matcher = PATTERN.matcher(remaining);
        }

        if(buildRegexStr != null) {
            if(paramNames.size() == 0) {
                throw new RuntimeException("PathParamMatcher param names list is empty but retRegeString is not null");
            }
            return Optional.ofNullable(new PathParamMatcher(Pattern.compile(buildRegexStr), paramNames));
        }
        
        return Optional.ofNullable(null);
    }
}
