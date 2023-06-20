package com.mikedll.headshot.controller;

import java.io.IOException;
import java.util.Map;
import java.util.LinkedHashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.*;

import com.mikedll.headshot.model.User;
import com.mikedll.headshot.DbSuite;
import com.mikedll.headshot.TestSuite;
import com.mikedll.headshot.Factories;

public class RootControllerTests {

    @BeforeEach
    public void beforeEach() throws IOException {
        if(!TestSuite.getSuite(DbSuite.class).beforeEach()) {
            Assertions.fail("suite beforeTest");
        }
    }
    
    @Test
    public void testRoot() throws IOException {
        TestRequest request = new ControllerUtils().get("/");
        
        Assertions.assertTrue(request.responseBody().contains("This is the app"));
        Assertions.assertTrue(request.responseBody().contains("Login with Github"));
    }
    
    @Test
    public void testRootLoggedIn() throws IOException {
        User user = Factories.createUser();

        TestRequest request = ControllerUtils.builder().withUser(user).build().get("/");
        
        Assertions.assertTrue(request.responseBody().contains("This is the app"));
        Assertions.assertTrue(request.responseBody().contains(user.getName()));
    }
    
}
