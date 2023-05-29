package com.mikedll.headshot;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.thymeleaf.context.Context;

public class RootController extends Controller {

    public void index(HttpServletRequest req, HttpServletResponse res) {
        render("index", defaultCtx(req), res);        
    }

    public void idle(HttpServletRequest req, HttpServletResponse res) {
        if(!beforeFilters(req, res)) return;
                
        Context ctx = defaultCtx(req);
        ctx.setVariable("oauth2state", (String)this.session.get("oauth2state"));
        render("idle", ctx, res);        
    }

    public void loggedIn(HttpServletRequest req, HttpServletResponse res) {
        if(!beforeFilters(req, res)) return;

        Context ctx = defaultCtx(req);
        ctx.setVariable("sub", (String)this.session.get("sub"));
        render("logged_in", ctx, res);        
    }    
    
}
