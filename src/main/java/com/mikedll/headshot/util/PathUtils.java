package com.mikedll.headshot.util;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathUtils {

    public static List<PathAncestor> pathAncestors(String repoName, String path) {
        List<PathAncestor> pathAncestors = new ArrayList<>();
        pathAncestors.add(new PathAncestor(repoName, ""));

        if(path.length() > 0) {
            if(path.length() > 0) {
                List<String> parentComponents = Arrays.asList(path.split("/"));
                for(int i = 0; i < parentComponents.size(); i++) {
                    List<String> ancestorComponents = parentComponents.subList(0, i+1);
                    pathAncestors.add(new PathAncestor(parentComponents.get(i), String.join("/", ancestorComponents)));
                }
            }
        }
        
        return pathAncestors;
    }
}
