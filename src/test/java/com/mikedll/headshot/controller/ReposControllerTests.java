package com.mikedll.headshot.controller;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.*;

import com.mikedll.headshot.model.User;
import com.mikedll.headshot.model.Repository;
import com.mikedll.headshot.*;

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
}
