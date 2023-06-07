package com.mikedll.headshot;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import org.thymeleaf.context.Context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RootController extends Controller {

    @Autowired
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
