package com.mikedll.headshot.controller;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ParamCaptureController extends Controller {

    @Override
    public void declareAuthRequirements() {
        this.requireAuthentication = false;
    }
    
    @Request(path="/animals/{name}")
    public void index() throws IOException {
        try {
            res.setStatus(HttpServletResponse.SC_OK);
            // res.getWriter().write("Found name: " + this.pathParams.get("name"));
            res.getWriter().write("This animal's name is: " + this.getPathParam("name"));
        } catch (IOException ex) {
            throw new RuntimeException("controller failed", ex);
        }
    }
}
