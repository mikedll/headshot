
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
    protected final void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {

        String path = req.getRequestURI().toString();
        System.out.println("Path: " + path);
        if(path.equals("/oauth2/authorization/github")) {
            LoginController controller = new LoginController();
            controller.oauth2LoginStart(req, res);
        } else if(path.equals("/login/oauth2/code/github")) {
            LoginController controller = new LoginController();
            controller.oauth2CodeReceive(req, res);
        } else if(path.equals("/idle")) {
            RootController controller = new RootController();
            controller.idle(req, res);
        } else if(path.equals("/logged_in")) {
            RootController controller = new RootController();
            controller.loggedIn(req, res);
        } else if(path.equals("/")) {
            RootController controller = new RootController();
            controller.index(req, res);
        } else {
            try {
                res.sendError(HttpServletResponse.SC_NOT_FOUND, "the requested resource was not found");
            } catch (IOException ex) {
                throw new RuntimeException("failed to send 404", ex);
            }                
        }
    }
}
