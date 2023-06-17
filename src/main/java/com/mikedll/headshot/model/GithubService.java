package com.mikedll.headshot.model;

import java.util.stream.Collectors;
import java.util.List;
import java.time.Instant;
import java.util.Arrays;
import java.util.ArrayList;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.javatuples.Pair;

import com.mikedll.headshot.controller.Controller;
import com.mikedll.headshot.JsonMarshal;

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

    public record PathReadFileResponse(String type, String name, String content) {}

    public record PathReadDirResponse(PathReadFileResponse[] files) {}

    public GithubService(Controller controller, String accessToken) {
        if(!controller.canAccessDb()) {
            throw new RuntimeException("Controller canAccessDb() returned false in GithubService");
        }
        
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

    public Pair<GithubPathInfo, String> readPath(User user, Repository repository, String path) {
        RestTemplate restTemplate = getRestTemplate();
        String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", user.getGithubLogin(), repository.getName(), path);
        ResponseEntity<String> respEnt = restTemplate.exchange(url, HttpMethod.GET, buildGetEntity(), String.class);
        System.out.println(respEnt.getBody());

        String body = respEnt.getBody();
        Pair<PathReadFileResponse, String> fileResult = JsonMarshal.unmarshal(body);
        if(fileResult.getValue1() == null) {
            List<GithubFile> files = new ArrayList<>();
            PathReadFileResponse fileResp = fileResult.getValue0();
            files.add(new GithubFile("/some/path", fileResp.name(), fileResp.content()));
            return Pair.with(new GithubPathInfo(files), null);
        }

        Pair<PathReadDirResponse, String> dirResult = JsonMarshal.unmarshal(body);
        if(dirResult.getValue1() == null) {
            List<GithubFile> files = new ArrayList<>();
            Arrays.asList(dirResult.getValue0().files()).forEach(readFileResponse -> {
                    files.add(new GithubFile("/some/path", readFileResponse.name(), null));
                });
            return Pair.with(new GithubPathInfo(files), null);
        }
        
        return Pair.with(null, "Unable to read path");
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
