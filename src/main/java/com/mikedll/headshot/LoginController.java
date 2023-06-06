
package com.mikedll.headshot;

import java.util.Base64;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.web.client.RestTemplate;        
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Component;

@Component
public class LoginController extends Controller {

    private final String githubPrefix = "https://github.com/login/oauth";
    private final String githubAuthPath = githubPrefix + "/authorize";    
    private final String githubAccessTokenPath = githubPrefix + "/access_token";
    private final String OAUTH2_STATE = "state";
    private final String OAUTH2_CODE = "code";

    // {"access_token":"blahblahblah","token_type":"bearer","scope":"user"}
    public record AccessTokenResponse(String access_token, String token_type, String scope) {}

    public record UserResponse(int id, String login, String name, String url, String html_url, String repos_url) {}
    
    private static final StringKeyGenerator DEFAULT_STATE_GENERATOR = new Base64StringKeyGenerator(
			Base64.getUrlEncoder());

    public void oauth2LoginStart(HttpServletRequest req, HttpServletResponse res) {
        if(!beforeFilters(req, res)) return;

        String state = DEFAULT_STATE_GENERATOR.generateKey();
        this.session.put("oauth2state", state);

        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
        MultiValueMap<String,String> map = new LinkedMultiValueMap<String,String>();        
        map.add("client_id", Env.githubConfig.clientId());
        map.add("scope", "user repo");
        map.add("state", (String)this.session.get("oauth2state"));
        map.add("redirect_uri", redirectUri(req));
        String redirectToGithubUrl = factory.uriString(githubAuthPath).queryParams(map).build().toString();

        flushCookies(res);
        
        // System.out.println("Redirecting to URI: " + redirectToGithubUrl);
        sendRedirect(res, redirectToGithubUrl);
    }

    public void oauth2CodeReceive(HttpServletRequest req, HttpServletResponse res) {
        if(!beforeFilters(req, res)) return;
        
        String stateFromRequest = req.getParameter(OAUTH2_STATE);
        if(stateFromRequest == null || !stateFromRequest.equals((String)this.session.remove("oauth2state"))) {
            throw new RequestException("incoming oauth2 state parameter doesn't match original oauth2 state");
        }

        String authCode = req.getParameter(OAUTH2_CODE);
        // System.out.println("Code: " + authCode);

        RestTemplate restTemplate = getRestTemplate();
        MultiValueMap<String,String> map = new LinkedMultiValueMap<String,String>();
        map.add("client_id", Env.githubConfig.clientId());
        map.add("client_secret", Env.githubConfig.clientSecret());
        map.add("code", authCode);

        AccessTokenResponse restResponse = restTemplate.postForObject(githubAccessTokenPath, map, AccessTokenResponse.class);
        if(restResponse.access_token == null) {
            throw new RequestException("oauth2 access_token retrieval failed");
        }
        // System.out.println("Rest response: ");
        // System.out.println(restResponse);

        this.session.put("access_token", restResponse.access_token);

        pullUserInfo();

        flushCookies(res);
        sendRedirect(res, localOrigin(req) + "/logged_in");
    }

    public void reloadUserInfo(HttpServletRequest req, HttpServletResponse res) {
        if(!beforeFilters(req, res)) return;

        pullUserInfo();
        
        flushCookies(res);
        sendRedirect(res, localOrigin(req) + "/logged_in");
    }

    private String redirectUri(HttpServletRequest req) {
        return localOrigin(req) + "/login/oauth2/code/github";
    }

    private RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new RestErrorHandler());
        return restTemplate;
    }
    
    private void pullUserInfo() {
        RestTemplate restTemplate = getRestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + (String)this.session.get("access_token"));
        headers.set("X-GitHub-Api-Version", "2022-11-28");
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        ResponseEntity<UserResponse> userResEnt = restTemplate.exchange("https://api.github.com/user", HttpMethod.GET, entity, UserResponse.class);
        UserResponse userResp = userResEnt.getBody();
        this.session.put("name", userResp.name);
    }
    
}
