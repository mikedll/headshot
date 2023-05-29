
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
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.AuthorizationGrantType;    
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.DeferredSecurityContext;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

public class LoginController extends Controller {

    private final String githubPrefix = "https://github.com/login/oauth";
    private final String githubAuthPath = githubPrefix + "/authorize";    
    private final String githubAccessTokenPath = githubPrefix + "/access_token";

    private static final StringKeyGenerator DEFAULT_STATE_GENERATOR = new Base64StringKeyGenerator(
			Base64.getUrlEncoder());

    public void oauth2LoginStart(HttpServletRequest req, HttpServletResponse res) {
        if(!beforeFilters(req, res)) return;
        
        OAuth2AuthorizationRequest oauth2Req = oauth2Request(req, DEFAULT_STATE_GENERATOR.generateKey());

        this.session.put("oauth2state", oauth2Req.getState());

        flushCookies(res);
        
        // System.out.println("Redirecting to URI: " + oauth2Req.getAuthorizationRequestUri());
        sendRedirect(res, oauth2Req.getAuthorizationRequestUri());
    }

    public void oauth2CodeReceive(HttpServletRequest req, HttpServletResponse res) {
        if(!beforeFilters(req, res)) return;
        
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("n/a")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationUri(githubAccessTokenPath)
            .clientId(Env.githubConfig.clientId())
            .clientSecret(Env.githubConfig.clientSecret())
            .redirectUri(localOrigin(req) + "/login/oauth2/code/github")
            .tokenUri(githubAccessTokenPath)
            .scope("user")
            .build();

        String oauth2State = (String)this.session.get("oauth2state");
        OAuth2AuthorizationRequest oauth2Req = oauth2Request(req, oauth2State);

        String stateFromRequest = req.getParameter(OAuth2ParameterNames.STATE);
        if(!stateFromRequest.equals(oauth2Req.getState())) {
            throw new RequestException("incoming oauth2 state parameter doesn't match original oauth2 state");
        }
        
        MultiValueMap<String, String> params = OAuth2Utils.toMultiMap(req.getParameterMap());
        if (!OAuth2Utils.isAuthorizationResponse(params)) {
            throw new RequestException("got non-oauth2 response");
        }

        String redirectUri = UriComponentsBuilder.fromHttpUrl(UrlUtils.buildFullRequestUrl(req))
            .replaceQuery(null)
            .build()
            .toUriString();
        
        OAuth2AuthorizationResponse oauth2Res = OAuth2Utils.convert(params, redirectUri);
        if(oauth2Res.statusError()) {
            throw new RequestException("oauth2 response has error: " + oauth2Res.getError().toString());
        }
        
        // core
        OAuth2AuthorizationExchange authorizationExchange = new OAuth2AuthorizationExchange(oauth2Req, oauth2Res);
        // client
        OAuth2AuthorizationCodeGrantRequest codeGrantRequest = new OAuth2AuthorizationCodeGrantRequest(clientRegistration, authorizationExchange);
        
        DefaultAuthorizationCodeTokenResponseClient client = new DefaultAuthorizationCodeTokenResponseClient();
        try {
            OAuth2AccessTokenResponse accessTokenResponse = client.getTokenResponse(codeGrantRequest);
        } catch (OAuth2AuthenticationException ex) {
            throw new RequestException("failed to validate oauth2 response with oauth2 provider's server");
        }

        // where do we verify state? probably up above

        this.session.put("sub", "someone");
        flushCookies(res);
        sendRedirect(res, localOrigin(req) + "/logged_in");
    }

    private OAuth2AuthorizationRequest oauth2Request(HttpServletRequest req, String state) {
        return OAuth2AuthorizationRequest.authorizationCode()
            .authorizationUri(githubAuthPath)
            .clientId(Env.githubConfig.clientId())
            .redirectUri(localOrigin(req) + "/login/oauth2/code/github")
            .scopes(new LinkedHashSet<>(Arrays.asList("user")))
            .state(state)
            .build();        
    }
    
}
