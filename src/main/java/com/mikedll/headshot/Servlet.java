
package com.mikedll.headshot;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import java.lang.StackTraceElement;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletContext;
    
public class Servlet extends HttpServlet {
    
    @Override
    protected final void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        String path = request.getRequestURI().toString();
        System.out.println("Path: " + path);
        if(path.equals("/oauth2/authorization/github")) {
            LoginController controller = new LoginController();
            controller.oauth2LoginStart(request, response);
        } else if(path.equals("/login/oauth2/code/github")) {
            LoginController controller = new LoginController();
            controller.oauth2CodeReceive(request, response);
        } else if(path.equals("/idle")) {
            RootController controller = new RootController();
            controller.idle(request, response);
        } else if(path.equals("/")) {
            RootController controller = new RootController();
            controller.index(request, response);
        } else {
            throw new RuntimeException("Not Found lol");
        }
    }
}
