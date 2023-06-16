package com.mikedll.headshot;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.javatuples.Pair;
import org.apache.commons.io.FileUtils;

import com.mikedll.headshot.db.Migrations;
import com.mikedll.headshot.db.SimpleSql;

public class MigrationsTests {

    private MigrationsSuite suite;
    
    @BeforeEach
    public void beforeEach() throws IOException {
        this.suite = TestSuite.getSuite(MigrationsSuite.class);

        suite.setUp();
        
        if(!suite.beforeEach()) {
            Assertions.fail("failed beforeEach");
        }
    }
    
    @Test
    public void testTsOf() {
        Assertions.assertEquals(null, Migrations.tsOf("blah"));
        Assertions.assertEquals("20230613101849", Migrations.tsOf("20230613101849_create_dogs.sql"));
    }

    @Test
    public void testNoFiles() {
        Migrations migrations = new Migrations(suite.dataSource);
        migrations.setMigrationsRoot("src/test/files/non_existent_dir");
        String error = migrations.readMigrations();
        Assertions.assertNull(error);
        Assertions.assertEquals(0, migrations.size());
    }
    
    @Test
    public void testReadMigrations() {
        Migrations migrations = new Migrations(suite.dataSource);
        migrations.setMigrationsRoot("src/test/files/good_migrations");
        String error = migrations.readMigrations();
        
        Assertions.assertNull(error);
        Assertions.assertEquals("20230613101849_create_dogs.sql", migrations.getForward(0));
        Assertions.assertEquals("20230613102201_create_dog_humans.sql", migrations.getForward(2));
    }

    @Test
    public void testReadMigrationsMismatch() {
        Migrations migrations = new Migrations(suite.dataSource);
        migrations.setMigrationsRoot("src/test/files/bad_migrations");
        String error = migrations.readMigrations();
        Assertions.assertEquals("Missing matching forward-reverse pair for 20230613102201_create_dog_humans.sql", error);
    }

    @Test
    public void testReadMigrationsCountMismatch() {
        Migrations migrations = new Migrations(suite.dataSource);
        migrations.setMigrationsRoot("src/test/files/bad_count_migrations");
        String error = migrations.readMigrations();
        Assertions.assertEquals("File count mismatch between forward and reverse dirs under src/test/files/bad_count_migrations", error);
    }

    @Test
    public void testReadMigrationsTsMissing() {
        Migrations migrations = new Migrations(suite.dataSource);
        migrations.setMigrationsRoot("src/test/files/bad_ts_missing_migrations");
        String error = migrations.readMigrations();
        Assertions.assertEquals("Timestamp prefix missing in src/test/files/bad_ts_missing_migrations/reverse/file.txt", error);
    }

    @Test
    public void testMigrateForward() {
        Migrations migrations = new Migrations(suite.dataSource);
        migrations.setSilent(true);
        migrations.setMigrationsRoot("src/test/files/good_migrations");
        String error = migrations.readMigrations();
        Assertions.assertNull(error, "read migrations okay");

        error = migrations.migrateForward();
        Assertions.assertNull(error, "migrate forward okay");

        Pair<String, String> result = SimpleSql.executeQuery(suite.dataSource, "SELECT * FROM dogs WHERE name = 'Rex';", (rs) -> {
                Assertions.assertTrue(rs.next());
                return rs.getString("name");
            });

        Assertions.assertNull(result.getValue1());
        Assertions.assertEquals("Rex", result.getValue0());

        String migrationQuery = "SELECT * FROM schema_migrations WHERE version = '20230613102201';";
        result = SimpleSql.executeQuery(suite.dataSource, migrationQuery, (rs) -> {
                Assertions.assertTrue(rs.next(), "found version row");
                return rs.getString("version");
            });

        Assertions.assertNull(result.getValue1());
        Assertions.assertEquals("20230613102201", result.getValue0());
    }

    @Test
    public void testMigrationsTableExists() {
        Migrations migrations = new Migrations(suite.dataSource);
        migrations.setSilent(true);
        
        migrations.ensureMigrationsTableExists();

        migrations.setMigrationsRoot("src/test/files/good_migrations");
        String error = migrations.readMigrations();

        error = migrations.migrateForward();
        Assertions.assertNull(error);

        String migrationQuery = "SELECT * FROM schema_migrations WHERE version = '20230613102201';";
        Pair<String, String> result = SimpleSql.executeQuery(suite.dataSource, migrationQuery, (rs) -> {
                Assertions.assertTrue(rs.next(), "found version row");
                return rs.getString("version");
            });

        Assertions.assertNull(result.getValue1());
        Assertions.assertEquals("20230613102201", result.getValue0());        
    }

    @Test
    public void testMigrationAlreadyRun() throws IOException {
        Migrations migrations = new Migrations(suite.dataSource);
        migrations.setSilent(true);

        migrations.ensureMigrationsTableExists();
        String root = "src/test/files/good_migrations";
        File file = new File(String.format("%s/%s/%s", root, Migrations.FORWARD, "20230613101849_create_dogs.sql"));
        String sql = FileUtils.readFileToString(file, "UTF-8");
        String error = SimpleSql.execute(suite.dataSource, sql);
        Assertions.assertNull(error, "manual migration okay");        
        error = SimpleSql.execute(suite.dataSource, "INSERT INTO schema_migrations (version) VALUES ('20230613101849');");
        Assertions.assertNull(error, "manual migration tracking okay");        
        
        migrations.setMigrationsRoot("src/test/files/good_migrations");
        error = migrations.readMigrations();
        Assertions.assertNull(error, "read migrations okay");

        error = migrations.migrateForward();
        Assertions.assertNull(error);

        String migrationQuery = "SELECT * FROM schema_migrations WHERE version = '20230613102201';";
        Pair<String, String> result = SimpleSql.executeQuery(suite.dataSource, migrationQuery, (rs) -> {
                Assertions.assertTrue(rs.next(), "found version row");
                return rs.getString("version");
            });

        Assertions.assertNull(result.getValue1());
        Assertions.assertEquals("20230613102201", result.getValue0());
    }

    @Test
    public void testReverseNonexistent() {
        Migrations migrations = new Migrations(suite.dataSource);
        migrations.setSilent(true);
        migrations.setMigrationsRoot("src/test/files/good_migrations");
        migrations.readMigrations();
        migrations.migrateForward();

        String error = migrations.reverse("blah");
        Assertions.assertEquals("'blah' is not a valid migration timestamp", error);

        error = migrations.reverse("20230613109999");
        Assertions.assertEquals("no migration with id '20230613109999' exists", error);
    }

    @Test
    public void testReverse() {
        Migrations migrations = new Migrations(suite.dataSource);
        migrations.setSilent(true);
        migrations.setMigrationsRoot("src/test/files/good_migrations");
        migrations.readMigrations();
        migrations.migrateForward();

        String error = migrations.reverse("20230613102201");
        Assertions.assertNull(error, "reverse okay");

        String migrationQuery = "SELECT * FROM schema_migrations WHERE version = '20230613102201';";
        Pair<String, String> result = SimpleSql.executeQuery(suite.dataSource, migrationQuery, (rs) -> {
                Assertions.assertFalse(rs.next(), "version is gone");
                return null;
            });

        Assertions.assertNull(result.getValue1());

        String verifySql = "SELECT table_name FROM information_schema.tables WHERE table_name = 'dog_humans'";
        Pair<String, String> verifyTableGone = SimpleSql.executeQuery(suite.dataSource, verifySql, (rs) -> {
                Assertions.assertFalse(rs.next(), "table is also gone");
                return null;
            });
        Assertions.assertNull(verifyTableGone.getValue1(), "clean table gone");
    }
}
