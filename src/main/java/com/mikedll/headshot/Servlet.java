
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

import org.javatuples.Pair;

import com.mikedll.headshot.controller.*;

public class Servlet extends HttpServlet {
    
    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {

        String path = req.getRequestURI().toString();
        System.out.println("Path: " + path);

        HttpMethod method = HttpMethod.fromServletReq(req.getMethod());
        RequestHandler matchingHandler = Application.requestHandlers
            .stream()
            .filter(rh -> rh.path.equals(path) && rh.method.equals(method))
            .findAny()
            .orElse(null);

        if(matchingHandler == null) {
            try {
                res.sendError(HttpServletResponse.SC_NOT_FOUND, "the requested resource was not found");
            } catch (IOException ex) {
                throw new RuntimeException("failed to send 404", ex);
            }
            return;
        }
        
        String error = matchingHandler.func.apply(Pair.with(req, res));
        if(error != null) {
            try {
                res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
            } catch (IOException ex) {
                throw new RuntimeException("failed to send 500", ex);
            }
        }
    }
}
