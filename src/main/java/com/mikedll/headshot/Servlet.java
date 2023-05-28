
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

        try {
            String path = request.getRequestURI().toString();
            System.out.println("Path: " + path);
            if(path.equals("/oauth2/authorization/github")) {
                LoginController controller = new LoginController();
                controller.doLogin(request, response);
            } else if(path.equals("/login/oauth2/code/github")) {
                LoginController controller = new LoginController();
                controller.doCodeReceive(request, response);
            } else if(path.equals("/")) {
                LoginController controller = new LoginController();
                controller.getLoginPage(request, response);
            } else {
                throw new RuntimeException("Not Found lol");
            }
        } catch(IOException ex) {
            System.out.println("IOException: " + ex.getMessage());
        }
    }
}
