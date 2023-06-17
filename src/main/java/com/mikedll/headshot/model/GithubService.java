package com.mikedll.headshot.model;

import java.util.stream.Collectors;
import java.util.List;
import java.time.Instant;
import java.util.Arrays;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.mikedll.headshot.controller.Controller;

public class GithubService {

    private UserRepository userRepository;
    
    private String accessToken;

    public record RepoResponse(Long id, String name, boolean isPrivate, String description, String created_at) {
        public Repository toRepository() {
            Repository ret = new Repository();
            ret.setGithubId(id);
            ret.setName(name);
            ret.setIsPrivate(isPrivate);
            ret.setDescription(description);
            ret.setCreatedAt(Instant.parse(created_at));
            return ret;
        }
    }

    public record UserResponse(Long id, String login, String name, String url, String html_url, String repos_url) {
        public void copyFieldsTo(User user) {
            user.setName(this.name);
            user.setGithubId(this.id);
            user.setGithubLogin(this.login);
            user.setUrl(this.url);
            user.setHtmlUrl(this.html_url);
            user.setReposUrl(this.repos_url);
        }
    }
    
    public GithubService(Controller controller, String accessToken) {
        this.userRepository = controller.getRepository(UserRepository.class);
        this.accessToken = accessToken;
    }

    private UserResponse getUser() {
        RestTemplate restTemplate = getRestTemplate();
        ResponseEntity<UserResponse> userResEnt = restTemplate.exchange("https://api.github.com/user",
                                                                        HttpMethod.GET,
                                                                        buildGetEntity(), UserResponse.class);
        return userResEnt.getBody();
    }

    public User pullUserInfo() {
        UserResponse userResp = getUser();
        if(userResp.id == null) return null;
        
        User user = userRepository.findByGithubId(userResp.id);
        if(user == null) {
            System.out.println("Found no user");
            user = new User();
            user.setAccessToken(accessToken);
            userResp.copyFieldsTo(user);
            userRepository.save(user);
        } else {
            System.out.println("Found existing user and given access token");
            user.setAccessToken(accessToken);
            userRepository.save(user);
        }

        return user;
    }
    
    
    public List<Repository> getRepositories(String githubLogin) {
        RestTemplate restTemplate = getRestTemplate();
        String url = String.format("https://api.github.com/users/%s/repos", githubLogin);
        ResponseEntity<RepoResponse[]> reposEnt = restTemplate.exchange(url, HttpMethod.GET, buildGetEntity(), RepoResponse[].class);
        List<Repository> response = Arrays.asList(reposEnt.getBody()).stream().map(RepoResponse::toRepository)
            .filter(r -> !r.getIsPrivate()).collect(Collectors.toList());
        return response;
    }

    private HttpEntity<String> buildGetEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        headers.set("X-GitHub-Api-Version", "2022-11-28");
        return new HttpEntity<String>(headers);
    }

    public static RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new RestErrorHandler());
        return restTemplate;
    }        
}
