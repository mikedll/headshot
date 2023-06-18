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

import com.mikedll.headshot.controller.ParamPathTests;

@Suite
@SelectClasses({ParamPathTests.class})
public class SimpleSuite extends TestSuite {

    private Application app;
        
    /*
     * Returns true on success, false on failure.
     */
    @Override
    public boolean doSetUp() throws IOException {        
        Dotenv dotenv = Dotenv.configure().filename(".env.test").load();

        app = new Application();
        String error = app.postDbSetup();
        if(error != null) {
            return false;
        }
        
        return true;
    }

    @Override
    public boolean doBeforeEach() {
        return true;
    }

    @Override
    public void doTearDown() {
    }
}
