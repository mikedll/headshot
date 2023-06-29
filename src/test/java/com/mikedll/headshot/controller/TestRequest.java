package com.mikedll.headshot.controller;

import java.io.PrintWriter;
import java.io.StringWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public record TestRequest(String method, HttpServletRequest req, HttpServletResponse res, PrintWriter printWriter, StringWriter stringWriter) {

    public void execute() {
        try {
            Servlet servlet = new Servlet();
            if(method == "GET") {
                servlet.doGet(req(), res());
            } else if(method == "PUT") {
                servlet.doPut(req(), res());
            } else if(method == "POST") {
                servlet.doPost(req(), res());
            } else {
                throw new RuntimeException("Unexpected method: " + method);
            }
                
        } catch (Throwable ex) {
            throw new RuntimeException("Exception when executing servlet", ex);
        }
        
        printWriter().flush();
    }

    public String responseBody() {
        return stringWriter().toString();
    }

    public Elements select(String cssSelector) {
        Document doc = Jsoup.parse(responseBody());
        return doc.body().select(cssSelector);
    }
}
