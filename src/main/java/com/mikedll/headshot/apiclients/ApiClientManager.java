package com.mikedll.headshot.apiclients;

import com.mikedll.headshot.controller.Controller;

public class ApiClientManager {

    /*
     * Maybe we can defer to a "client manager" if this is not really the app's
     * responsibility.
     */
    public GithubClient getGithubClient(Controller controller, String accessToken) {
        if(!controller.canAccessData()) {
            throw new RuntimeException("Controller canAccessData() returned false in Application");
        }
        
        return new GithubClient(controller, accessToken);
    }
    
}
