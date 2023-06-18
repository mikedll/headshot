
package com.mikedll.headshot;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.List;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletContext;

import org.javatuples.Pair;

import com.mikedll.headshot.controller.*;

public class Servlet extends HttpServlet {

    record PathMatch(RequestHandler handler, String matchSubstring) {}
    
    public boolean shouldLog() {
        return Env.shouldLog();
    }

    @Override
    protected final void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doService(req, res, HttpMethod.PUT);
    }
    
    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        doService(req, res, HttpMethod.GET);
    }

    private void doService(HttpServletRequest req, HttpServletResponse res, HttpMethod method) {
        String path = req.getRequestURI().toString();
        if(shouldLog()) {
            System.out.println("Path: " + path);
        }

        List<PathMatch> pathMatches = Application.requestHandlers
            .stream()
            .filter(rh -> path.startsWith(rh.path) && rh.method.equals(method))
            .map(rh -> {
                    // Can later enhance to hold paths that match with capture params
                    return new PathMatch(rh, rh.path);
                })
            .collect(Collectors.toList());
        
        Collections.sort(pathMatches, new PathMatchComparator());

        RequestHandler matchingHandler = null;
        if(pathMatches.size() > 0) {
            matchingHandler = pathMatches.get(0).handler();
        }

        if(matchingHandler == null) {
            try {
                res.sendError(HttpServletResponse.SC_NOT_FOUND, "the requested resource was not found");
            } catch (IOException ex) {
                throw new RuntimeException("failed to send 404", ex);
            }
            return;
        }

        if(Env.env == "development") {
            Application.assetFingerprinter.refresh();
        }
        
        String error = matchingHandler.func.apply(Pair.with(req, res));
        if(error != null) {
            try {
                System.out.println("Error: " + error);
                res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
            } catch (IOException ex) {
                throw new RuntimeException("failed to send 500", ex);
            }
        }        
    }

    class PathMatchComparator implements java.util.Comparator<PathMatch> {
        @Override
        public int compare(PathMatch a, PathMatch b) {
            return b.matchSubstring().length() - a.matchSubstring().length();
        }
    }    
    
}
