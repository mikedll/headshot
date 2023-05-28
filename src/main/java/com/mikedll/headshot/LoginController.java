
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
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.core.context.SecurityContext;
    
public class LoginController extends Controller {

    private final String githubPrefix = "https://github.com/login/oauth";
    private final String githubAuthPath = githubPrefix + "/authorize";    
    private final String githubAccessTokenPath = githubPrefix + "/access_token";

    private static final StringKeyGenerator DEFAULT_STATE_GENERATOR = new Base64StringKeyGenerator(
			Base64.getUrlEncoder());
    
    public void getLoginPage(HttpServletRequest req, HttpServletResponse res)
        throws IOException {
        
        templateEngine.process("oauth2_login", defaultCtx(req), res.getWriter());
    }

    public void doLogin(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String scheme = "http://";
        String fullHost = req.getServerName();
        int port = req.getServerPort();
        if(port != 80) {
            fullHost += ":" + port;
        }
        System.out.println("fullHost: " + fullHost);
        
        OAuth2AuthorizationRequest oauth2Req = OAuth2AuthorizationRequest.authorizationCode()
            .authorizationUri(githubAuthPath)
            .clientId(Env.githubConfig.clientId())
            .redirectUri(scheme + fullHost + "/login/oauth2/code/github")
            .scopes(new LinkedHashSet<>(Arrays.asList("user")))
            .state(DEFAULT_STATE_GENERATOR.generateKey())
            .build();

        System.out.println("State: " + oauth2Req.getState());

        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        Session auth = new Session();
        auth.oauth2State = oauth2Req.getState();
        context.setAuthentication(auth);
        securityContextRepository.saveContext(context, req, res);
        
        System.out.println("URI: " + oauth2Req.getAuthorizationRequestUri());
        res.sendRedirect(oauth2Req.getAuthorizationRequestUri());
    }

    public void doCodeReceive(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpRequestResponseHolder reqResHolder = new HttpRequestResponseHolder(req, res);
        SecurityContext foundContext = securityContextRepository.loadContext(reqResHolder);
        Session foundSession = (Session)foundContext.getAuthentication();
        if(foundSession != null) {
            System.out.println("Found a state: " + foundSession.oauth2State);
        }
        
        String state = req.getParameter(OAuth2ParameterNames.STATE);
        System.out.println("State var: " + state);
        templateEngine.process("idle", defaultCtx(req), res.getWriter());
    }
}
