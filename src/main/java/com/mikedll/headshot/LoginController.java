
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

import org.springframework.util.MultiValueMap;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;    
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.DeferredSecurityContext;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationResponseUtils;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationResponseUtils;

public class LoginController extends Controller {

    private final String githubPrefix = "https://github.com/login/oauth";
    private final String githubAuthPath = githubPrefix + "/authorize";    
    private final String githubAccessTokenPath = githubPrefix + "/access_token";

    private static final StringKeyGenerator DEFAULT_STATE_GENERATOR = new Base64StringKeyGenerator(
			Base64.getUrlEncoder());

    public void oauth2LoginStart(HttpServletRequest req, HttpServletResponse res) {
        if(!beforeFilters(req, res)) return;
        
        OAuth2AuthorizationRequest oauth2Req = oauth2Request(DEFAULT_STATE_GENERATOR.generateKey());

        System.out.println("State: " + oauth2Req.getState());
        this.session.put("oauth2state", oauth2Req.getState());

        flushCookies(res);
        sendRedirect(res, localOrigin(req) + "/idle");
        
        // System.out.println("URI: " + oauth2Req.getAuthorizationRequestUri());
        // res.sendRedirect(oauth2Req.getAuthorizationRequestUri());
    }

    public void oauth2CodeReceive(HttpServletRequest req, HttpServletResponse res) {
        if(!beforeFilters(req, res)) return;
        

        ClientRegistration.Builder clientRegistrationBuilder = ClientRegistration.withRegistrationId("n/a")
            .clientSecret("secret")
            .redirectUri(localOrigin(req) + "/login/oauth2/code/github")
            .tokenUri(githubAccessTokenPath)
            .scope("user");



        String oauth2State = (String)this.session.get("oauth2state");
        OAuth2AuthorizationRequest oauth2Req = oauth2Request(oauth2State);

        
        MultiValueMap<String, String> params = OAuth2AuthorizationResponseUtils.toMultiMap(req.getParameterMap());
        if (!OAuth2AuthorizationResponseUtils.isAuthorizationResponse(params)) {
            throw new RequestException("got non-oauth2 response");
        }

        String redirectUri = UriComponentsBuilder.fromHttpUrl(UrlUtils.buildFullRequestUrl(req))
            .replaceQuery(null)
            .build()
            .toUriString();

        
        OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponseUtils.convert(params, redirectUri);

        // check here for error
        
        // core
        OAuth2AuthorizationExchange authorizationExchange = new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse);
        // client
        OAuth2AuthorizationCodeGrantRequest codeGrantRequest = new OAuth2AuthorizationCodeGrantRequest(clientRegistration, authorizationExchange);
        
        DefaultAuthorizationCodeTokenResponseClient client = new DefaultAuthorizationCodeTokenResponseClient();
        try {
            OAuth2AccessTokenResponse accessTokenResponse = client.getTokenResponse(codeGrantRequest);
        } catch (OAuth2AuthenticationException ex) {
            throw new RequestException("failed to validate oauth2 response with oauth2 provider's server");
        }

        // where do we verify state? probably up above

        String state = req.getParameter(OAuth2ParameterNames.STATE);
        System.out.println("State var: " + state);

        render("idle", defaultCtx(req), res);
    }

    private oauth2Request(String state) {
        OAuth2AuthorizationRequest.authorizationCode()
            .authorizationUri(githubAuthPath)
            .clientId(Env.githubConfig.clientId())
            .redirectUri(localOrigin(req) + "/login/oauth2/code/github")
            .scopes("user")
            .state(state)
            .build();        
    }
    
}
