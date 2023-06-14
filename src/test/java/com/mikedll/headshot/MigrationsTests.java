package com.mikedll.headshot;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import com.mikedll.headshot.db.Migrations;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

// @ExtendWith(DbSetup.class)
public class MigrationsTests {

    @BeforeAll
    public static void setUp() throws IOException {
        MigrationsSuite.setUp();
    }
    
    @Test
    public void testTsOf() {
        Assertions.assertEquals(null, Migrations.tsOf("blah"));
        Assertions.assertEquals("20230613101849", Migrations.tsOf("20230613101849_create_dogs.sql"));
    }

    @Test
    public void testNoFiles() {
        Migrations migrations = new Migrations();
        migrations.setMigrationsRoot("src/test/files/non_existent_dir");
        String error = migrations.readMigrations();
        Assertions.assertNull(error);
        Assertions.assertEquals(0, migrations.size());
    }
    
    @Test
    public void testReadMigrations() {
        Migrations migrations = new Migrations();
        migrations.setMigrationsRoot("src/test/files/good_migrations");
        String error = migrations.readMigrations();
        
        Assertions.assertNull(error);
        Assertions.assertEquals("20230613101849_create_dogs.sql", migrations.get(0));
        Assertions.assertEquals("20230613102201_create_dog_humans.sql", migrations.get(2));
    }

    @Test
    public void testReadMigrationsMismatch() {
        Migrations migrations = new Migrations();
        migrations.setMigrationsRoot("src/test/files/bad_migrations");
        String error = migrations.readMigrations();
        Assertions.assertEquals("Missing matching forward-reverse pair for 20230613102201_create_dog_humans.sql", error);
    }

    @Test
    public void testReadMigrationsCountMismatch() {
        Migrations migrations = new Migrations();
        migrations.setMigrationsRoot("src/test/files/bad_count_migrations");
        String error = migrations.readMigrations();
        Assertions.assertEquals("File count mismatch between forward and reverse dirs under src/test/files/bad_count_migrations", error);
    }

    @Test
    public void testReadMigrationsTsMissing() {
        Migrations migrations = new Migrations();
        migrations.setMigrationsRoot("src/test/files/bad_ts_missing_migrations");
        String error = migrations.readMigrations();
        Assertions.assertEquals("Timestamp prefix missing in src/test/files/bad_ts_missing_migrations/reverse/file.txt", error);
    }

    @Test
    public void testMigrateForward() {
        Migrations migrations = new Migrations();
        migrations.setMigrationsRoot("src/test/files/good_migrations");
        String error = migrations.readMigrations();

        error = migrations.migrateForward();
    }
}
