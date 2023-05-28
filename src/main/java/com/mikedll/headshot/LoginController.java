
package com.mikedll.headshot;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.Arrays;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
    
public class LoginController extends Controller {

    private final String githubPrefix = "https://github.com/login/oauth";
    private final String githubAuthPath = githubPrefix + "/authorize";    
    private final String githubAccessTokenPath = githubPrefix + "/access_token";
    
    public void getLoginPage(HttpServletRequest req, HttpServletResponse res)
        throws IOException {
        
        templateEngine.process("oauth2_login", defaultCtx(req), res.getWriter());
    }

    public void doLogin(HttpServletRequest req, HttpServletResponse res) throws IOException {

        String fullHost = req.getServerName();
        int port = req.getServerPort();
        if(port != 80) {
            fullHost += ":" + port;
        }
        System.out.println("fullHost: " + fullHost);
        
        OAuth2AuthorizationRequest oauth2Req = OAuth2AuthorizationRequest.authorizationCode()
            .authorizationUri(githubAuthPath)
            .clientId(Env.githubConfig.clientId())
            .redirectUri(fullHost + "/login/oauth2/code/github")
            .scopes(new LinkedHashSet<>(Arrays.asList("user")))
            .state("asdf")
            .build();

        System.out.println("URI: " + oauth2Req.getAuthorizationRequestUri());
        res.sendRedirect(oauth2Req.getAuthorizationRequestUri());
    }
}
