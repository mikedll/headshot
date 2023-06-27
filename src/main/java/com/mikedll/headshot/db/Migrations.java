package com.mikedll.headshot.db;

import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
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
import org.javatuples.Pair;

public class Migrations {

    private List<String> forwards = new ArrayList<>();

    private List<String> reverses = new ArrayList<>();
    
    private String migrationsRoot = "db";

    public static final String FORWARD = "forward";

    public static final String REVERSE = "reverse";

    public static final String SCHEMA_MIGRATIONS_TABLE = "schema_migrations";

    private DatabaseConfiguration dbConf;

    public Migrations(DatabaseConfiguration dbConf) {
        this.dbConf = dbConf;
    }
    
    public void setMigrationsRoot(String path) {
        this.migrationsRoot = path;
    }

    private static final Pattern tsPattern = Pattern.compile("\\d{14}");
    
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

    public Pair<Set<String>, String> existingMigrations() {
        Pair<Set<String>, String> result = SimpleSql.executeQuery(dbConf, "SELECT * FROM schema_migrations", (rs) -> {
                Set<String> existing = new HashSet<>();
                while(rs.next()) {
                    existing.add(rs.getString("version"));
                }
                return existing;
            });

        if(result.getValue1() != null) {
            return Pair.with(null, result.getValue1());
        }

        return Pair.with(result.getValue0(), null);
    }
    
    public String migrateForward() {
        String error = ensureMigrationsTableExists();
        if(error != null) {
            dbConf.logger.error("Error while migrating forward: " + error);
            return error;
        }

        Pair<Set<String>, String> existingResult = existingMigrations();
        if(existingResult.getValue1() != null) {
            return existingResult.getValue1();
        }
        Set<String> existing = existingResult.getValue0();

        for(String forward : this.forwards) {
            if(existing.contains(tsOf(forward))) {
                continue;
            }
            
            String sql = null;
            try {
                sql = FileUtils.readFileToString(new File(String.format("%s/%s/%s", this.migrationsRoot, FORWARD, forward)), "UTF-8");
            } catch (IOException ex) {
                return "Unable to read " + forward + ": " + ex.getMessage();
            }
            dbConf.logger.info("Executing " + forward);
            String migrationError = SimpleSql.execute(dbConf, sql);
            if(migrationError != null) {
                dbConf.logger.error("SQL Error: " + migrationError);
                return migrationError;
            }

            String insertSql = "INSERT INTO " + SCHEMA_MIGRATIONS_TABLE + " (version) VALUES (?);";
            String versionError = SimpleSql.executeUpdate(dbConf, insertSql, new SqlArg(String.class, tsOf(forward)));
            if(versionError != null) {
                dbConf.logger.error("SQL Error: " + versionError);
                return versionError;
            }
        }

        return null;
    }

    public String reverse(String ts) {
        Matcher matcher = tsPattern.matcher(ts);
        if(!matcher.find()) {
            return String.format("'%s' is not a valid migration timestamp", ts);
        }
        
        String reverse = this.reverses.stream().filter(r -> tsOf(r).equals(ts)).findAny().orElse(null);
        if(reverse == null) {
            return String.format("no migration with id '%s' exists", ts);
        }

        String sql = null;
        try {
            sql = FileUtils.readFileToString(new File(String.format("%s/%s/%s", this.migrationsRoot, REVERSE, reverse)), "UTF-8");
        } catch (IOException ex) {
            return "Unable to read " + reverse + ": " + ex.getMessage();
        }

        dbConf.logger.info("Executing reverse of " + reverse);
        String migrationError = SimpleSql.execute(dbConf, sql);
        if(migrationError != null) {
            dbConf.logger.error("SQL Error: " + migrationError);
            return migrationError;
        }

        String deleteSql = "DELETE FROM " + SCHEMA_MIGRATIONS_TABLE + " WHERE version = ?;";
        String deleteVersionError = SimpleSql.executeUpdate(dbConf, deleteSql, new SqlArg(String.class, ts));
        if(deleteVersionError != null) {
            dbConf.logger.error("SQL Error: " + deleteVersionError);
            return deleteVersionError;
        }

        return null;
    }

    public String ensureMigrationsTableExists() {
        String query = "SELECT table_name FROM information_schema.tables WHERE table_name = '" + SCHEMA_MIGRATIONS_TABLE + "';";
        Pair<String, String> result = SimpleSql.executeQuery(dbConf, query, (rs) -> {
                if(!rs.next()) {
                    return null;
                }

                return rs.getString("table_name");
            });
        
        // SQL error
        if(result.getValue1() != null) {
            return result.getValue1();
        }

        // Table exists
        if(result.getValue0() != null) {
            return null;
        }

        String sql = "CREATE TABLE " + SCHEMA_MIGRATIONS_TABLE + " (id BIGSERIAL PRIMARY KEY, version CHARACTER VARYING);";
        sql += "\n\n";
        sql += "CREATE UNIQUE INDEX schema_migrations_version ON schema_migrations (version);";
        return SimpleSql.execute(dbConf, sql);
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
