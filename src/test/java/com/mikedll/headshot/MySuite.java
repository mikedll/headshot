package com.mikedll.headshot;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import javax.sql.DataSource;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.io.FileUtils;

import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.IncludePackages;

@Suite
@SelectPackages({"com.mikedll.headshot"})
public class MySuite {

    private static boolean setup;
    
    public static void setUp() throws IOException {
        if(setup) {
            return;
        }
        
        Dotenv dotenv = Dotenv.configure().filename(".env.test").load();
        Env.cookieSigningKey = "eVKgwkis9APaD2o2/suPAv9sgs156+fMTBDDbM1vgwU=";
        Env.dbUrl = dotenv.get("DB_URL");
        Env.env = "test";
        
        Application app = new Application();
        app.markEnvLoaded();
        app.setUp();

        String schemaSql = FileUtils.readFileToString(new File("./db/schema.sql"), "UTF-8");
        
        DataSource ds = app.dbConf.getDataSource();
        try(Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DROP SCHEMA public CASCADE; CREATE SCHEMA public;");
            } catch(SQLException ex) {
                System.out.println("SQLException when dropping schema: " + ex.getMessage());
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(schemaSql);
            } catch(SQLException ex) {
                System.out.println("SQLException when loading schema: " + ex.getMessage());
            }
        } catch(SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
        }

        setup = true;
    }
}
