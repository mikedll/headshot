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
import org.junit.platform.suite.api.SelectClasses;

@Suite
@SelectClasses({MigrationsTests.class})
public class MigrationsSuite {

    private static boolean setup;

    public static void handleLaunchers() {
    }
    
    /*
     * Returns true on success, false on failure.
     */
    public static boolean setUp() throws IOException {
        if(setup) {
            return true;
        }

        System.out.println("Calling setUp in MigrationSuite");

        /*
        Dotenv dotenv = Dotenv.configure().filename(".env.test").load();

        HikariDataSource ds = buildDataSource();
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
            System.out.println("SQLException when handling connection: " + ex.getMessage());
            broken = true;
            return false;
        }
        */
        
        setup = true;
        return true;
    }
}
