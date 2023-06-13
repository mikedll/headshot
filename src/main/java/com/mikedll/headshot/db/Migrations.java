package com.mikedll.headshot.db;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Arrays;

public class Migrations {

    private List<String> files = new ArrayList<>();

    private String migrationsRoot = "db";

    public static final String FORWARD = "forward";

    public static final String REVERSE = "reverse";    
    
    public void setMigrationsRoot(String path) {
        this.migrationsRoot = path;
    }

    private static final Pattern filePattern = Pattern.compile("(\\d{14})_\\w+\\.sql");

    public List<File> listFiles(String dir) {
        return Optional.ofNullable(new File(dir).listFiles()).map(Arrays::asList).orElse(Collections.emptyList())
            .stream()
            .filter(f -> !f.isDirectory())
            .collect(Collectors.toList());
    }        

    /*
     * Returns error on failure, null on success.
     */
    public String readMigrations() {
        this.files = new ArrayList<>();
            
        List<File> forwards = listFiles(migrationsRoot + "/" + FORWARD);
        List<File> reverses = listFiles(migrationsRoot + "/" + REVERSE);
        
        if(forwards.size() == 0) {
            return null;
        }
        
        if(forwards.size() != reverses.size()) {
            return "File count mismatch between forward and reverse dirs under " + migrationsRoot;
        }
        
        String formatProblem;
        formatProblem = findFormatProblem(forwards, FORWARD);
        if(formatProblem != null) {
            return formatProblem;
        }
        formatProblem = findFormatProblem(reverses, REVERSE);
        if(formatProblem != null) {
            return formatProblem;
        }

        Collections.sort(forwards, new Comparator());
        Collections.sort(reverses, new Comparator());

        for(int i = 0; i < forwards.size(); i++) {
            if(!forwards.get(i).getName().equals(reverses.get(i).getName())) {
                return "Missing matching forward-reverse pair for " + forwards.get(i).getName();
            }
        }

        this.files = forwards.stream().map(File::getName).collect(Collectors.toList());
        return null;
    }

    private String findFormatProblem(List<File> files, String type) {
        File bad = files.stream().filter(f -> tsOf(f.getName()) == null).findAny().orElse(null);
        if(bad != null) {
            return "Timestamp prefix missing in " + this.migrationsRoot + "/" + type + "/" + bad.getName();
        }
        return null;
    }

    public String get(int index) {
        return this.files.get(index);
    }

    public int size() {
        return this.files.size();
    }

    public static String tsOf(String filename) {
        Matcher matcher = filePattern.matcher(filename);
        if(!matcher.find()) {
            return null;
        }

        return matcher.group(1);
    }

    class Comparator implements java.util.Comparator<File> {
        @Override
        public int compare(File a, File b) {
            return a.getName().compareTo(b.getName());
        }
    }    
}
