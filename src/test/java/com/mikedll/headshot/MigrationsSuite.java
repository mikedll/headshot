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
public class MigrationsSuite extends TestSuite {

    private static boolean setup;

    public HikariDataSource dataSource;
        
    /*
     * Returns true on success, false on failure.
     */
    @Override
    public boolean doSetUp() throws IOException {        
        Dotenv dotenv = Dotenv.configure().filename(".env.test").load();
        
        this.dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(dotenv.get("DB_URL"));
				dataSource.setPoolName("default");
        dataSource.setMaximumPoolSize(1);
        
        return true;
    }

    @Override
    public boolean doBeforeEach() {
        try(Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DROP SCHEMA public CASCADE; CREATE SCHEMA public;");
            } catch(SQLException ex) {
                System.out.println("SQLException when dropping schema: " + ex.getMessage());
                return false;
            }
        } catch(SQLException ex) {
            System.out.println("SQLException when handling connection: " + ex.getMessage());
            return false;
        }
        
        return true;
    }

    @Override
    public void doTearDown() {
        if(this.dataSource != null) {
            this.dataSource.close();
        }
    }
}
