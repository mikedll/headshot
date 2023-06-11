
package com.mikedll.headshot.controller;

import org.thymeleaf.context.Context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

public class ProfileController extends Controller {
    
    @Request(path="/profile")
    public void profile() {
        Context ctx = defaultCtx();
        ctx.setVariable("name", this.currentUser.getName());
        render("profile", ctx);        
    }

    @Request(path="/session")
    public void session() throws JsonProcessingException {
        Context ctx = defaultCtx();
        ObjectMapper mapper = new ObjectMapper();
        String sessionStr = mapper.writeValueAsString(this.session);
        // ctx.setVariable("session", sessionStr);
        ctx.setVariable("session", "");
        render("session", ctx);
    }

    /*
    @Request(path="/reload_user_info")
    public void reloadUserInfo() {
        UserResponse userResp = getUser(currentUser.getAccessToken());
        if(userResp.id == null) {
            throw new RequestException("Failed to get user from github");
        }

        userResp.copyFieldsTo(this.currentUser);
        userRepository.save(this.currentUser);
        
        sendCookies();
        sendRedirect(localOrigin() + "/profile");
    }
    */
}
