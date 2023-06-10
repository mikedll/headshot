package com.mikedll.headshot;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.thymeleaf.context.Context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

public class RootController extends Controller {

    private UserRepository userRepository;
    
    public void index(HttpServletRequest req, HttpServletResponse res) {
        if(!beforeFilters(req, res)) return;
        
        render("index", defaultCtx(req), res);        
    }

    public void profile(HttpServletRequest req, HttpServletResponse res) {
        if(!beforeFilters(req, res)) return;

        Context ctx = defaultCtx(req);
        ctx.setVariable("name", (String)this.session.get("name"));
        render("profile", ctx, res);        
    }

    public void session(HttpServletRequest req, HttpServletResponse res) throws JsonProcessingException {
        if(!beforeFilters(req, res)) return;
        
        Context ctx = defaultCtx(req);
        ObjectMapper mapper = new ObjectMapper();
        String sessionStr = mapper.writeValueAsString(this.session);
        // ctx.setVariable("session", sessionStr);
        ctx.setVariable("session", "");
        render("session", ctx, res);
    }
}
