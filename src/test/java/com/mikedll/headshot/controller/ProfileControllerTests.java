
package com.mikedll.headshot.controller;

import java.io.IOException;
import java.util.Map;
import java.util.LinkedHashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.*;

import com.mikedll.headshot.model.User;
import com.mikedll.headshot.*;

public class ProfileControllerTests {
    
    @BeforeEach
    public void beforeEach() {
        if(!TestSuite.getSuite(DbSuite.class).beforeEach()) {
            Assertions.fail("suite beforeTest");
        }
    }

    @Test
    public void testBadCookie() throws IOException {
        TestRequest request = ControllerUtils.builder().withCookieString("sillynonsense").build().get("/profile");

        verify(request.res()).sendRedirect("http://localhost/");
    }    
    
    @Test
    public void testProfile() {
        User user = Factories.createUser();

        TestRequest request = ControllerUtils.builder().withUser(user).build().get("/profile");

        Assertions.assertTrue(request.responseBody().contains(user.getHtmlUrl()));
    }
}
