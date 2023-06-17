
package com.mikedll.headshot.controller;

import java.util.Base64;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;

import com.mikedll.headshot.model.UserRepository;
import com.mikedll.headshot.model.User;
import com.mikedll.headshot.model.GithubService;
import com.mikedll.headshot.Env;
import com.mikedll.headshot.Application;

public class LoginController extends Controller {

    private UserRepository userRepository;
    
    private final String GITHUB_PREFIX = "https://github.com/login/oauth";
    private final String GITHUB_AUTH_PATH = GITHUB_PREFIX + "/authorize";    
    private final String GITHUB_ACCESS_TOKEN_PATH = GITHUB_PREFIX + "/access_token";
    private final String OAUTH2_STATE = "state";
    private final String OAUTH2_CODE = "code";

    // {"access_token":"blahblahblah","token_type":"bearer","scope":"user"}
    public record AccessTokenResponse(String access_token, String token_type, String scope) {}
    
    private static final StringKeyGenerator DEFAULT_STATE_GENERATOR = new Base64StringKeyGenerator(
			Base64.getUrlEncoder());

    @Override
    public void declareAuthRequirements() {
        this.requireAuthentication = false;
    }

    @Override
    public void acquireDbAccess() {
        this.userRepository = dbConf.getRepository(this, UserRepository.class);
    }

    @Request(path="/oauth2/authorization/github")
    public void oauth2LoginStart() {
        String state = DEFAULT_STATE_GENERATOR.generateKey();
        this.session.put("oauth2state", state);

        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
        MultiValueMap<String,String> map = new LinkedMultiValueMap<String,String>();        
        map.add("client_id", Env.githubConfig.clientId());
        map.add("scope", "user repo");
        map.add("state", (String)this.session.get("oauth2state"));
        map.add("redirect_uri", redirectUri());
        String redirectToGithubUrl = factory.uriString(GITHUB_AUTH_PATH).queryParams(map).build().toString();

        sendCookies();
        
        // System.out.println("Redirecting to URI: " + redirectToGithubUrl);
        sendRedirectWorld(redirectToGithubUrl);
    }

    @Request(path="/login/oauth2/code/github")
    public void oauth2CodeReceive() {
        String stateFromRequest = req.getParameter(OAUTH2_STATE);
        if(stateFromRequest == null || !stateFromRequest.equals((String)this.session.remove("oauth2state"))) {
            throw new RequestException("incoming oauth2 state parameter doesn't match original oauth2 state");
        }

        String authCode = req.getParameter(OAUTH2_CODE);
        // System.out.println("Code: " + authCode);

        RestTemplate restTemplate = GithubService.getRestTemplate();
        MultiValueMap<String,String> map = new LinkedMultiValueMap<String,String>();
        map.add("client_id", Env.githubConfig.clientId());
        map.add("client_secret", Env.githubConfig.clientSecret());
        map.add("code", authCode);

        AccessTokenResponse restResponse = restTemplate.postForObject(GITHUB_ACCESS_TOKEN_PATH, map, AccessTokenResponse.class);
        if(restResponse.access_token == null) {
            throw new RequestException("oauth2 access_token retrieval failed");
        }
        // System.out.println("Rest response: ");
        // System.out.println(restResponse);

        GithubService service = new GithubService(this, restResponse.access_token);
        User user = service.pullUserInfo();
        if(user == null) {
            throw new RequestException("Failed to get user from github");
        }
        
        this.session.put("user_id", user.getId());

        sendCookies();
        sendRedirect(localOrigin() + "/");
    }

    private String redirectUri() {
        return localOrigin() + "/login/oauth2/code/github";
    }
    
}
