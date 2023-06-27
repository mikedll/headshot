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
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.javatuples.Pair;

import com.mikedll.headshot.controller.ControllerUtils;
import com.mikedll.headshot.db.SimpleSql;

@Suite
@SelectPackages({"com.mikedll.headshot"})
@ExcludeClassNamePatterns({".*MigrationsTests", ".*PathParamTests"})
public class DbSuite extends TestSuite {

    private Application app;

    public Application getApp() {
        return app;
    }
    
    /*
     * Returns true on success, false on failure.
     */
    @Override
    public boolean doSetUp() throws IOException {
        this.app = new Application();
        app.setConfig(TestSuite.testConfig);
        app.loggingSetup();
        app.dbSetup();
        
        String schemaSql = FileUtils.readFileToString(new File("./db/schema.sql"), "UTF-8");

        String error = SimpleSql.executeUpdate(app.dbConf, "DROP SCHEMA public CASCADE; CREATE SCHEMA public;");
        if(error != null) {
            System.out.println("Error when dropping schema: " + error);
            return false;
        }

        error = SimpleSql.execute(app.dbConf, schemaSql);
        if(error != null) {
            System.out.println("Error when loading schema: " + error);
            return false;
        }
 
        this.app.webSetup();
        
        return true;
    }

    @Override
    public void doTearDown() {
        app.shutdown();
    }

    /*
     * Returns true on success, false on failure.
     */
    @Override
    public boolean doBeforeEach() {
        // Servlet depends on Application.current. If we can get tomcat to instantiate servlet with
        // an Application of our choosing, we can just use ControllerUtils's static
        // Application instance (it can give it to the TestRequest for use in
        // TestRequest#execute).
        ControllerUtils.app = Application.current = this.app;

        return truncateDatabase();        
    }

    /*
     * Returns true on success, false on failure.
     */ 
    private boolean truncateDatabase() {
        if(this.app.config.env == "production") {
            throw new RuntimeException("can't truncate database in production");
        }

        String error = SimpleSql.execute(app.dbConf, "SET search_path TO public; " +
                                         "TRUNCATE users, repositories, tours, pages RESTART IDENTITY CASCADE; COMMIT;");
        if(error != null) {
            System.out.println("Error when truncating database: " + error);
            return false;
        }

        return true;
    }
}
