package com.mikedll.headshot.controller;

import java.io.IOException;
import java.util.Map;
import java.util.LinkedHashMap;
import jakarta.servlet.ServletException;

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
    public void testRoot() throws IOException, ServletException {
        Servlet servlet = new Servlet();

        TestRequest request = ControllerUtils.get("/");
        servlet.doGet(request.req(), request.res());

        request.printWriter().flush();
        Assertions.assertTrue(request.stringWriter().toString().contains("This is the app"));
        Assertions.assertTrue(request.stringWriter().toString().contains("Login with Github"));
    }
    
    @Test
    public void testRootLoggedIn() throws IOException, ServletException {
        User user = Factories.makeUser();
        Servlet servlet = new Servlet();

        Map<String, Object> session = new LinkedHashMap<String, Object>();
        session.put("user_id", user.getId());

        TestRequest request = ControllerUtils.get("/", session);

        servlet.doGet(request.req(), request.res());

        request.printWriter().flush();
        Assertions.assertTrue(request.stringWriter().toString().contains("This is the app"));
        Assertions.assertTrue(request.stringWriter().toString().contains(user.getName()));
    }
    
}
