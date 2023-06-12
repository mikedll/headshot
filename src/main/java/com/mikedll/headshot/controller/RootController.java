package com.mikedll.headshot.controller;

public class RootController extends Controller {

    @Override
    public void declareAuthRequirements() {
        this.requireAuthentication = false;
    }

    @Request(path="/")
    public void index() {
        render("index", defaultCtx());        
    }

    @Request(path="/logout")
    public void logout() {
        clearSession();
        sendCookies();
        sendRedirect(localOrigin() + "/");
    }    
}
