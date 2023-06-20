
package com.mikedll.headshot.controller;

import java.io.IOException;
import java.util.Map;
import java.util.LinkedHashMap;
import java.io.StringWriter;
import java.io.PrintWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.*;

import com.mikedll.headshot.model.User;
import com.mikedll.headshot.DbSuite;
import com.mikedll.headshot.TestSuite;
import com.mikedll.headshot.Factories;

public class ProfileControllerTests {
    
    @BeforeEach
    public void beforeEach() throws IOException {
        if(!TestSuite.getSuite(DbSuite.class).beforeEach()) {
            Assertions.fail("suite beforeTest");
        }
    }

    @Test
    public void testBadCookie() throws IOException, ServletException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);

        when(req.getRequestURI()).thenReturn("/profile");
        when(req.getMethod()).thenReturn("GET");
        when(req.getServerName()).thenReturn("localhost");
        when(req.getServerPort()).thenReturn(80);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(res.getWriter()).thenReturn(printWriter);

        String cookieString = "sillynonsense";
        
        Cookie cookie = mock(Cookie.class);
        when(cookie.getName()).thenReturn(Controller.COOKIE_NAME);
        when(cookie.getValue()).thenReturn(cookieString);
        when(req.getCookies()).thenReturn(new Cookie[] { cookie });
        
        TestRequest request = new TestRequest(req, res, printWriter, stringWriter);
        
        Servlet servlet = new Servlet();

        servlet.doGet(request.req(), request.res());

        verify(res).sendRedirect("http://localhost/");
    }    
    
    @Test
    public void testProfile() throws IOException, ServletException {
        User user = Factories.makeUser();
        Servlet servlet = new Servlet();

        Map<String, Object> session = new LinkedHashMap<String, Object>();
        session.put("user_id", user.getId());

        TestRequest request = ControllerUtils.get("/profile", session);

        servlet.doGet(request.req(), request.res());

        request.printWriter().flush();
        Assertions.assertTrue(request.stringWriter().toString().contains(user.getHtmlUrl()));
    }
}
