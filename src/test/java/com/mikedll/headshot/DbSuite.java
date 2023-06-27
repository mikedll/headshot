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
        app.basicSetup();
        
        String schemaSql = FileUtils.readFileToString(new File("./db/schema.sql"), "UTF-8");

        String error = SimpleSql.executeUpdate(app.dbConf.getDataSource(), "DROP SCHEMA public CASCADE; CREATE SCHEMA public;");
        if(error != null) {
            System.out.println("Error when dropping schema: " + error);
            return false;
        }

        error = SimpleSql.execute(app.dbConf.getDataSource(), schemaSql);
        if(error != null) {
            System.out.println("Error when loading schema: " + error);
            return false;
        }
 
        this.app.setUp();
        
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

        DataSource ds = app.dbConf.getDataSource();
        try(Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET search_path TO public; TRUNCATE users, repositories RESTART IDENTITY CASCADE; COMMIT;");
            } catch(SQLException ex) {
                System.out.println("SQLException when truncating database: " + ex.getMessage());
                return false;
            }
        } catch(SQLException ex) {
            System.out.println("SQLException when handling connection: " + ex.getMessage());
            return false;
        }

        return true;
    }
}
