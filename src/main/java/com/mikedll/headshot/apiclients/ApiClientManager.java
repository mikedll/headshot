package com.mikedll.headshot.apiclients;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.mikedll.headshot.controller.Controller;
import com.mikedll.headshot.util.RestClient;

public class ApiClientManager {

    private ObjectMapper jsonMapper;

    public ApiClientManager(ObjectMapper mapper) {
        this.jsonMapper = mapper;
    }
    
    /*
     * Maybe we can defer to a "client manager" if this is not really the app's
     * responsibility.
     */
    public GithubClient getGithubClient(Controller controller, String accessToken) {
        if(!controller.canAccessData()) {
            throw new RuntimeException("Controller canAccessData() returned false in Application");
        }
        
        return new GithubClient(controller.app.logger, new RestClient(jsonMapper), controller, accessToken);
    }
    
}
