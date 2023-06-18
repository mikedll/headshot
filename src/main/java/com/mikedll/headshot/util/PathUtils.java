package com.mikedll.headshot.util;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathUtils {

    public static final String PATH_TRAILING_REGEX = "/[^/]+$";

    public static final Pattern PATH_TRAILING_PATTERN = Pattern.compile(PATH_TRAILING_REGEX);
    
    public static List<PathAncestor> pathAncestors(String repoName, String path) {
        if(path.length() > 0) {
            List<PathAncestor> pathAncestors = new ArrayList<>();
            pathAncestors.add(new PathAncestor(repoName, ""));

            Matcher matcher = PATH_TRAILING_PATTERN.matcher(path);
            if(matcher.find()) {
                String parent = path.replaceFirst(PATH_TRAILING_REGEX, "");
                List<String> parentComponents = Arrays.asList(parent.split("/"));
                for(int i = 0; i < parentComponents.size(); i++) {
                    List<String> ancestorComponents = parentComponents.subList(0, i+1);
                    pathAncestors.add(new PathAncestor(parentComponents.get(i), String.join("/", ancestorComponents)));
                }
            }
            return pathAncestors;
        }
        
        return null;
    }
}
