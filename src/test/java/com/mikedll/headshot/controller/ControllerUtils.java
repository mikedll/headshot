package com.mikedll.headshot.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.LinkedHashMap;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;

import org.mockito.Mockito;

import com.mikedll.headshot.model.User;
import com.mikedll.headshot.Factories;
import com.mikedll.headshot.Application;

public class ControllerUtils {

    public static Application app;
    
    private Map<String,Object> session = null;

    private String cookieString;

    public void setSession(Map<String,Object> session) {
        this.session = session;
    }

    public void setCookieString(String cookieString) {
        this.cookieString = cookieString;
    }

    public TestRequest makeRequest(String path, String method) {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse res = Mockito.mock(HttpServletResponse.class);

        String defaultAccept = "text/html, application/xhtml+xml, application/xml;q=0.9, */*;q=0.8";
        Mockito.when(req.getHeader("Accept")).thenReturn(defaultAccept); 
        Mockito.when(req.getServerName()).thenReturn("localhost");
        Mockito.when(req.getServerPort()).thenReturn(80);
        Mockito.when(req.getRequestURI()).thenReturn(path);
        Mockito.when(req.getMethod()).thenReturn(method);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        try {
            Mockito.when(res.getWriter()).thenReturn(printWriter);
        } catch (IOException ex) {
            throw new RuntimeException("IOException when mocking response getWriter()", ex);
        }

        if(session != null && cookieString != null) {
            throw new RuntimeException("cookieString and session can't both be null");
        }

        if(session != null) {
            CookieManager cookieManager = new CookieManager(app.config.cookieSigningKey);
            String cookieString = null;
            try {
                cookieString = cookieManager.cookieString(session);
            } catch (Throwable ex) {
                throw new RuntimeException("Exception in cookieString(...)", ex);
            }

            Cookie cookie = Mockito.mock(Cookie.class);
            Mockito.when(cookie.getName()).thenReturn(Controller.COOKIE_NAME);
            Mockito.when(cookie.getValue()).thenReturn(cookieString);
            Mockito.when(req.getCookies()).thenReturn(new Cookie[] { cookie });
        } else if(cookieString != null) {
            Cookie cookie = Mockito.mock(Cookie.class);
            Mockito.when(cookie.getName()).thenReturn(Controller.COOKIE_NAME);
            Mockito.when(cookie.getValue()).thenReturn(cookieString);
            Mockito.when(req.getCookies()).thenReturn(new Cookie[] { cookie });
            
        }

        return new TestRequest(method, req, res, printWriter, stringWriter);
        
    }
    
    public TestRequest get(String path) {
        TestRequest request = makeRequest(path, "GET");
        request.execute();
        return request;
    }

    public TestRequest put(String path) {
        TestRequest request = makeRequest(path, "PUT");
        request.execute();
        return request;
    }
    
    public static Builder builder() {
        return new Builder();
    }

    public static <T> T getRepository(Class<T> clazz) {
        return app.dbConf.getRepository(app, clazz);
    }

    public static class Builder {
        private Map<String,Object> session;

        private String cookieString;

        public Builder withUser() {
            return withUser(Factories.createUser());
        }

        public Builder withUser(User user) {
            if(this.session == null) {
                this.session = new LinkedHashMap<String, Object>();
            }
            session.put("user_id", user.getId());
            return this;
        }

        public Builder withCookieString(String cookieString) {
            this.cookieString = cookieString;
            return this;
        }
        
        public ControllerUtils build() {
            ControllerUtils controllerUtils = new ControllerUtils();
            if(session != null) {
                controllerUtils.setSession(session);
            }
            if(cookieString != null) {
                controllerUtils.setCookieString(cookieString);
            }

            return controllerUtils;
        }        
    }
}
