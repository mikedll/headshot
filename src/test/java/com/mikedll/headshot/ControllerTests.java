
package com.mikedll.headshot;

import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.LinkedHashMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.*;

import com.mikedll.headshot.controller.Controller;

public class ControllerTests {

    @BeforeEach
    public void beforeEach() throws IOException {
        DbSuite suite = TestSuite.getSuite(DbSuite.class);
        suite.setUp();

        if(!suite.beforeEach()) {
            Assertions.fail("suite beforeTest");
        }
    }
    
    
    public Request get(String path) throws IOException {
        return get(path, null);
    }
    
    public Request get(String path, Map<String,Object> session) throws IOException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);

        when(req.getRequestURI()).thenReturn(path);
        when(req.getMethod()).thenReturn("GET");
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(res.getWriter()).thenReturn(printWriter);

        if(session != null) {
            CookieManager cookieManager = new CookieManager(Env.cookieSigningKey);
            String cookieString = cookieManager.cookieString(session);
        
            Cookie cookie = mock(Cookie.class);
            when(cookie.getName()).thenReturn(Controller.COOKIE_NAME);
            when(cookie.getValue()).thenReturn(cookieString);
            when(req.getCookies()).thenReturn(new Cookie[] { cookie });
        }
        
        return new Request(req, res, printWriter, stringWriter);
    }

    private record Request(HttpServletRequest req, HttpServletResponse res, PrintWriter printWriter, StringWriter stringWriter) {}
    
    @Test
    public void testRoot() throws IOException, ServletException {
        Servlet servlet = new Servlet();

        Request request = get("/");
        servlet.doGet(request.req(), request.res());

        request.printWriter().flush();
        Assertions.assertTrue(request.stringWriter().toString().contains("This is the app"));
        Assertions.assertTrue(request.stringWriter().toString().contains("Login with Github"));
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
        
        Request request = new Request(req, res, printWriter, stringWriter);
        
        Servlet servlet = new Servlet();

        servlet.doGet(request.req(), request.res());

        verify(res).sendRedirect("http://localhost/");
    }    
    
    @Test
    public void testRootLoggedIn() throws IOException, ServletException {
        User user = Factories.makeUser();
        Servlet servlet = new Servlet();

        Map<String, Object> session = new LinkedHashMap<String, Object>();
        session.put("user_id", user.getId());

        Request request = get("/", session);

        servlet.doGet(request.req(), request.res());

        request.printWriter().flush();
        Assertions.assertTrue(request.stringWriter().toString().contains("This is the app"));
        Assertions.assertTrue(request.stringWriter().toString().contains(user.getName()));
    }

    @Test
    public void testProfile() throws IOException, ServletException {
        User user = Factories.makeUser();
        Servlet servlet = new Servlet();

        Map<String, Object> session = new LinkedHashMap<String, Object>();
        session.put("user_id", user.getId());

        Request request = get("/profile", session);

        servlet.doGet(request.req(), request.res());

        request.printWriter().flush();
        Assertions.assertTrue(request.stringWriter().toString().contains(user.getHtmlUrl()));
    }
    
}
