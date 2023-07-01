package com.mikedll.headshot.controller;

import java.util.Map;
import java.io.PrintWriter;
import java.io.StringWriter;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mockito.Mockito;
import org.mockito.ArgumentCaptor;
import org.mockito.exceptions.base.MockitoException;
import org.javatuples.Pair;

import com.mikedll.headshot.Application;

public record TestRequest(Application app, String method, HttpServletRequest req,
                          HttpServletResponse res, PrintWriter printWriter,
                          StringWriter stringWriter, ArgumentCaptor<Cookie> cookieArg,
                          Map<String,Object> session) {

    public void execute() {
        try {
            Servlet servlet = new Servlet();
            if(method == "GET") {
                servlet.doGet(req(), res());
            } else if(method == "PUT") {
                servlet.doPut(req(), res());
            } else if(method == "POST") {
                servlet.doPost(req(), res());
            } else if(method == "DELETE") {
                servlet.doDelete(req(), res());
            } else {
                throw new RuntimeException("Unexpected method: " + method);
            }                
        } catch (Throwable ex) {
            throw new RuntimeException("Exception when executing servlet", ex);
        }
        
        printWriter().flush();

        try {
            CookieManager cookieManager = new CookieManager(app.config.cookieSigningKey);
            Pair<Map<String,Object>,String> verifyResult = cookieManager.verify(app.jsonObjectMapper, cookieArg.getValue().getValue());
            session.putAll(verifyResult.getValue0());
        } catch (MockitoException ex) {
            if(!ex.getMessage().contains("No argument value was captured!")) {
                throw ex;
            }
        }
    }

    public String responseBody() {
        return stringWriter().toString();
    }

    public Elements select(String cssSelector) {
        Document doc = Jsoup.parse(responseBody());
        return doc.body().select(cssSelector);
    }
}
