package com.mikedll.headshot.db;

import java.io.IOException;
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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;

public class Migrations {

    private List<String> forwards = new ArrayList<>();

    private List<String> reverses = new ArrayList<>();
    
    private String migrationsRoot = "db";

    public static final String FORWARD = "forward";

    public static final String REVERSE = "reverse";

    private DataSource dataSource;

    private boolean silent;

    public Migrations(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }
    
    public void setMigrationsRoot(String path) {
        this.migrationsRoot = path;
    }

    private static final Pattern filePattern = Pattern.compile("(\\d{14})_\\w+\\.sql");

    private String findFormatProblem(List<File> files, String type) {
        File bad = files.stream().filter(f -> tsOf(f.getName()) == null).findAny().orElse(null);
        if(bad != null) {
            return "Timestamp prefix missing in " + this.migrationsRoot + "/" + type + "/" + bad.getName();
        }
        return null;
    }
    
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
        this.forwards = new ArrayList<>();
        this.reverses = new ArrayList<>();
            
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

        this.forwards = forwards.stream().map(File::getName).collect(Collectors.toList()); 
        this.reverses = reverses.stream().map(File::getName).collect(Collectors.toList());
        return null;
    }

    public String getForward(int index) {
        return this.forwards.get(index);
    }

    public int size() {
        return this.forwards.size();
    }

    public String migrateForward() {
        this.forwards.forEach(forward -> {
                String sql = null;
                try {
                    sql = FileUtils.readFileToString(new File(String.format("%s/%s/%s", this.migrationsRoot, FORWARD, forward)), "UTF-8");
                } catch (IOException ex) {
                    throw new RuntimeException("Unable to read " + forward, ex);
                }
                if(!this.silent) {
                    System.out.println("Executing " + forward);
                    System.out.println(sql);
                }
                execute(sql);
            });
        return null;
    }

    private void execute(String sql) {
        try(Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            } catch(SQLException ex) {
                throw new RuntimeException("Failed to execute sql", ex);
            }
        } catch(SQLException ex) {
            throw new RuntimeException("Failed to get sql connection", ex);
        }
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
