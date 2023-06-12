
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.*;

import com.mikedll.headshot.controller.Controller;

public class ControllerTests {

    @BeforeAll
    public static void setUp() throws IOException {
        MySuite.setUp();
    }

    @BeforeEach
    public void checkSetup() throws IOException {
        if(!MySuite.setupUpOkay()) {
            Assertions.fail("setup failed");
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
    public void testRootLoggedIn() throws IOException, ServletException {

        Application app = MySuite.getApp();
        UserRepository userRepository = app.dbConf.getRepository(app, UserRepository.class);
        User user = new User();
        user.setName("Randal Johnson");
        user.setGithubId(2000L);
        user.setGithubLogin("randal.johnson");
        user.setUrl("http://api.github.com/randal.johnson");
        user.setHtmlUrl("http://www.github.com/randal.johnson");
        user.setReposUrl("http://api.github.com/randal.johnson/repos");
        user.setAccessToken("asdf");
        userRepository.save(user);
        
        Servlet servlet = new Servlet();

        Map<String, Object> session = new LinkedHashMap<String, Object>();
        session.put("user_id", user.getId());

        Request request = get("/", session);

        servlet.doGet(request.req(), request.res());

        request.printWriter().flush();
        Assertions.assertTrue(request.stringWriter().toString().contains("This is the app"));
        Assertions.assertTrue(request.stringWriter().toString().contains("Randal Johnson"));
    }
    
}
