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

import com.mikedll.headshot.controller.PathParamTests;
import com.mikedll.headshot.controller.ControllerUtils;
import com.mikedll.headshot.controller.CookieManagerTests;

@Suite
@SelectClasses({PathParamTests.class, CookieManagerTests.class})
public class SimpleSuite extends TestSuite {

    public Application app;
        
    /*
     * Returns true on success, false on failure.
     */
    @Override
    public boolean doSetUp() throws IOException {
        this.app = new Application();
        this.app.setConfig(TestSuite.testConfig);
        this.app.loggingSetup();
        String error = app.webSetup();
        if(error != null) {
            return false;
        }

        return true;
    }

    @Override
    public boolean doBeforeEach() {
        // See DbSuite comment on doBeforeEach
        ControllerUtils.app = Application.current = this.app;

        return true;
    }

    @Override
    public void doTearDown() {
    }
}
