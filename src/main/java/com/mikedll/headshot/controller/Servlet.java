
package com.mikedll.headshot.controller;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletContext;

import org.javatuples.Pair;
import org.javatuples.Quartet;

import com.mikedll.headshot.Application;

public class Servlet extends HttpServlet {

    private Application app;

    public boolean shouldLog() {
        return app.config.shouldLog();
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

    private Optional<Pair<RequestHandler,PathMatch>> findHandlerMatch(Pair<String, HttpMethod> incomingRequest) {
        List<Pair<RequestHandler,PathMatch>> matchPairs = this.app.requestHandlers
            .stream()
            .map(rh -> Pair.with(rh, rh.tryMatch.apply(incomingRequest).orElse(null)))
            .filter(pair -> pair.getValue1() != null)
            .collect(Collectors.toList());
        
        Collections.sort(matchPairs, new PathMatchComparator());

        Pair<RequestHandler,PathMatch> found = null;
        if(matchPairs.size() > 0) {
            found = matchPairs.get(0);
        }

        return Optional.ofNullable(found);
    }
    
    private void doService(HttpServletRequest req, HttpServletResponse res, HttpMethod method) {
        // would be nice if tomcat would set this.
        this.app = Application.current;
        
        String path = req.getRequestURI().toString();
        if(shouldLog()) {
            System.out.println("Path: " + path);
        }

        Pair<RequestHandler,PathMatch> handlerMatch = findHandlerMatch(Pair.with(path, method)).orElse(null);
        if(handlerMatch == null) {
            try {
                res.sendError(HttpServletResponse.SC_NOT_FOUND, "the requested resource was not found");
            } catch (IOException ex) {
                throw new RuntimeException("failed to send 404", ex);
            }
            return;
        }

        if(this.app.config.env == "development") {
            this.app.assetFingerprinter.refresh();
        }
        
        String error = handlerMatch.getValue0().handlerFunc
            .apply(Quartet.with(this.app, req, res, handlerMatch.getValue1()));
        if(error != null) {
            try {
                System.out.println("Error: " + error);
                res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
            } catch (IOException ex) {
                throw new RuntimeException("failed to send 500", ex);
            }
        }        
    }

    /*
     * Descending sort by path match. "/aaa/bbb" comes before "/ccc".
     */ 
    class PathMatchComparator implements java.util.Comparator<Pair<RequestHandler,PathMatch>> {
        @Override
        public int compare(Pair<RequestHandler,PathMatch> a, Pair<RequestHandler,PathMatch> b) {
            return b.getValue1().matched().length() - a.getValue1().matched().length();
        }
    }    
    
}
