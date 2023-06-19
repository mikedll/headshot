
package com.mikedll.headshot.controller;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Base64;
import java.net.URI;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.NameValuePair;
import com.fasterxml.jackson.core.type.TypeReference;
import org.javatuples.Pair;

import com.mikedll.headshot.model.UserRepository;
import com.mikedll.headshot.model.User;
import com.mikedll.headshot.model.GithubService;
import com.mikedll.headshot.util.MyUri;
import com.mikedll.headshot.util.RestClient;
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

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("client_id", Env.githubConfig.clientId()));
        params.add(new BasicNameValuePair("scope", "user repo"));
        params.add(new BasicNameValuePair("state", (String)this.session.get("oauth2state")));
        params.add(new BasicNameValuePair("redirect_uri", redirectUri()));
        String redirectToGithubUrl = MyUri.from(GITHUB_AUTH_PATH, params).toString();

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

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("client_id", Env.githubConfig.clientId()));
        params.add(new BasicNameValuePair("client_secret", Env.githubConfig.clientSecret()));
        params.add(new BasicNameValuePair("code", authCode));

        Map<String,String> headers = new HashMap<String,String>();        
        Pair<List<NameValuePair>,String> restResult = RestClient.nvParamsPost(MyUri.from(GITHUB_ACCESS_TOKEN_PATH),
                                                                              headers,
                                                                              params);

        if(restResult.getValue1() != null) {
            sendInternalServerError(restResult.getValue1());
            return;
        }
        List<NameValuePair> data = restResult.getValue0();
        NameValuePair accessToken = data.stream().filter(p -> p.getName().equals("access_token")).findAny().orElse(null);

        GithubService service = new GithubService(this, accessToken.getValue());
        Pair<User,String> userResult = service.pullUserInfo();
        if(userResult.getValue1() != null) {
            sendInternalServerError("Failed to get user from Github: " + userResult.getValue1());
            return;
        }
        
        this.session.put("user_id", userResult.getValue0().getId());

        sendCookies();
        sendRedirect("/");
    }

    private String redirectUri() {
        return localOrigin() + "/login/oauth2/code/github";
    }
    
}
