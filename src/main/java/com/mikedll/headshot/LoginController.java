
package com.mikedll.headshot;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.Arrays;
import java.util.Base64;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;    
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.DeferredSecurityContext;
    
public class LoginController extends Controller {

    private final String githubPrefix = "https://github.com/login/oauth";
    private final String githubAuthPath = githubPrefix + "/authorize";    
    private final String githubAccessTokenPath = githubPrefix + "/access_token";

    private static final StringKeyGenerator DEFAULT_STATE_GENERATOR = new Base64StringKeyGenerator(
			Base64.getUrlEncoder());
    
    public void oauth2LoginStart(HttpServletRequest req, HttpServletResponse res) {
        if(!beforeFilters(req, res)) return;
        
        OAuth2AuthorizationRequest oauth2Req = OAuth2AuthorizationRequest.authorizationCode()
            .authorizationUri(githubAuthPath)
            .clientId(Env.githubConfig.clientId())
            .redirectUri(localOrigin(req) + "/login/oauth2/code/github")
            .scopes(new LinkedHashSet<>(Arrays.asList("user")))
            .state(DEFAULT_STATE_GENERATOR.generateKey())
            .build();

        System.out.println("State: " + oauth2Req.getState());
        this.session.put("oauth2state", oauth2Req.getState());

        flushCookies(res);
        sendRedirect(res, localOrigin(req) + "/idle");
        
        // System.out.println("URI: " + oauth2Req.getAuthorizationRequestUri());
        // res.sendRedirect(oauth2Req.getAuthorizationRequestUri());
    }

    public void oauth2CodeReceive(HttpServletRequest req, HttpServletResponse res) {
        if(!beforeFilters(req, res)) return;
        
        String oauth2State = (String)this.session.get("oauth2state");
        System.out.println("Found a state: " + oauth2State);
                        
        String state = req.getParameter(OAuth2ParameterNames.STATE);
        System.out.println("State var: " + state);
        render("idle", defaultCtx(req), res);
    }
}
