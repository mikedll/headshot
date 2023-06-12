package com.mikedll.headshot;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.io.FileUtils;

import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SelectPackages;

@Suite
@SelectPackages({"com.mikedll.headshot"})
public class MySuite {

    private static boolean setup;

    private static boolean broken;

    private static Application app;

    /*
     * Returns true on success, false on failure.
     */
    public static boolean setUp() throws IOException {
        if(setup) {
            return true;
        }

        Dotenv dotenv = Dotenv.configure().filename(".env.test").load();
        Env.cookieSigningKey = "eVKgwkis9APaD2o2/suPAv9sgs156+fMTBDDbM1vgwU=";
        Env.dbUrl = dotenv.get("DB_URL");
        Env.env = "test";
        
        app = new Application();

        // This is ridiculous. These changes aren't working unless I close and reopen
        // the data source.
        // Setup schema before Hibernate reads anything
        String schemaSql = FileUtils.readFileToString(new File("./db/schema.sql"), "UTF-8");
        // DataSource ds = app.dbConf.getDataSource();
        HikariDataSource ds = app.dbConf.buildDataSource();
        try(Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DROP SCHEMA public CASCADE; CREATE SCHEMA public;");
            } catch(SQLException ex) {
                System.out.println("SQLException when dropping schema: " + ex.getMessage());
                broken = true;
                return false;
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(schemaSql);
            } catch(SQLException ex) {
                System.out.println("SQLException when loading schema: " + ex.getMessage());
                broken = true;
                return false;
            }
        } catch(SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            broken = true;
            return false;
        }
        ds.close();

        app.markEnvLoaded();
        app.setUp();
        
        setup = true;
        return true;
    }

    public static boolean setupUpOkay() {
        return !broken && setup;
    }

    public static Application getApp() {
        return app;
    }
}