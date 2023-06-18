package com.mikedll.headshot.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;

import static org.mockito.Mockito.*;

import com.mikedll.headshot.Env;

public class ControllerUtils {

    public static TestRequest get(String path) throws IOException {
        return get(path, null);
    }
    
    public static TestRequest get(String path, Map<String,Object> session) throws IOException {
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
        
        return new TestRequest(req, res, printWriter, stringWriter);
    }    
}
