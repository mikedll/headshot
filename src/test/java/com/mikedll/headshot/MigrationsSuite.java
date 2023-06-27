package com.mikedll.headshot;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.io.FileUtils;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SelectClasses;

import com.mikedll.headshot.db.DatabaseConfiguration;
import com.mikedll.headshot.db.MigrationsTests;
import com.mikedll.headshot.db.SimpleSql;

@Suite
@SelectClasses({MigrationsTests.class})
public class MigrationsSuite extends TestSuite {

    public DatabaseConfiguration dbConf;
        
    /*
     * Returns true on success, false on failure.
     */
    @Override
    public boolean doSetUp() throws IOException {
        this.dbConf = new DatabaseConfiguration(TestSuite.testConfig);
        
        return true;
    }

    @Override
    public boolean doBeforeEach() {
        String error = SimpleSql.executeUpdate(dbConf, "DROP SCHEMA public CASCADE; CREATE SCHEMA public;");
        if(error != null) {
            System.out.println("Error when dropping schema: " + error);
            return false;
        }
        
        return true;
    }

    @Override
    public void doTearDown() {
        this.dbConf.shutdown();
    }
}
