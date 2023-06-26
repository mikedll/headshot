package com.mikedll.headshot.controller;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import com.mikedll.headshot.model.User;
import com.mikedll.headshot.model.Repository;
import com.mikedll.headshot.DbSuite;
import com.mikedll.headshot.Factories;
import com.mikedll.headshot.TestSuite;

public class ReposControllerTests {

    @BeforeEach
    public void beforeEach() {
        if(!TestSuite.getSuite(DbSuite.class).beforeEach()) {
            Assertions.fail("suite beforeTest");
        }
    }

    @Test
    public void testIndex() {
        User user = Factories.createUser();
        Repository repo1 = Factories.createRepository(user);
        Repository repo2 = Factories.createRepository(user);
        TestRequest request = ControllerUtils.builder().withUser(user).build().get("/repos");
        
        Arrays.asList(new Repository[] { repo1, repo2 }).forEach(r -> {
                Assertions.assertTrue(request.responseBody().contains(r.getName()));
            });
    }

    @Test
    public void testShow() {
        User user = Factories.createUser();
        Repository repo1 = Factories.createRepository(user);
        TestRequest request = ControllerUtils.builder().withUser(user).build().get("/repos/" + repo1.getId());

        Assertions.assertTrue(request.responseBody().contains(repo1.getName()));
    }

    @Test
    public void testShowWithPath() {
        User user = Factories.createUser();
        Repository repo1 = Factories.createRepository(user);
        TestRequest request = ControllerUtils.builder().withUser(user).build().get("/repos/" + repo1.getId() + "/lib/cknife");

        Assertions.assertTrue(request.select(".tree-nav").text().contains("lib > cknife"), "breadcrumbs");
    }
    
}
