package com.mikedll.headshot.controller;

import java.io.PrintWriter;
import java.io.StringWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;

public record TestRequest(String method, HttpServletRequest req, HttpServletResponse res, PrintWriter printWriter, StringWriter stringWriter) {

    public void execute() {
        try {
            Servlet servlet = new Servlet();
            if(method == "GET") {
                servlet.doGet(req(), res());
            } else if(method == "PUT") {
                servlet.doPut(req(), res());
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
}
