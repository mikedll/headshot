package com.mikedll.headshot.model;

import java.util.stream.Collectors;
import java.util.List;
import java.time.Instant;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.javatuples.Pair;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.type.TypeReference;

import com.mikedll.headshot.controller.Controller;
import com.mikedll.headshot.JsonMarshal;
import com.mikedll.headshot.util.MyUri;
import com.mikedll.headshot.util.RestClient;

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

    public GithubService(Controller controller, String accessToken) {
        if(!controller.canAccessDb()) {
            throw new RuntimeException("Controller canAccessDb() returned false in GithubService");
        }
        
        this.userRepository = controller.getRepository(UserRepository.class);
        this.accessToken = accessToken;
    }

    private Pair<UserResponse,String> getUser() {
        Pair<UserResponse,String> restResult = RestClient.get(MyUri.from("https://api.github.com/user"),
                                                              buildHeaders(),
                                                              new TypeReference<UserResponse>() {});

        if(restResult.getValue1() != null) {
            return Pair.with(null, restResult.getValue1());
        }
        
        return Pair.with(restResult.getValue0(), null);
    }

    public Pair<User,String> pullUserInfo() {
        Pair<UserResponse,String> getUserResult = getUser();
        if(getUserResult.getValue1() != null) {
            return Pair.with(null, getUserResult.getValue1());
        }

        UserResponse userResponse = getUserResult.getValue0();
        
        User user = userRepository.findByGithubId(userResponse.id);
        if(user == null) {
            System.out.println("Found no user");
            user = new User();
            user.setAccessToken(accessToken);
            userResponse.copyFieldsTo(user);
            userRepository.save(user);
        } else {
            System.out.println("Found existing user and given access token");
            user.setAccessToken(accessToken);
            userRepository.save(user);
        }

        return Pair.with(user, null);
    }
    
    
    public Pair<List<Repository>,String> getRepositories(String githubLogin) {
        String url = String.format("https://api.github.com/users/%s/repos", githubLogin);
        Pair<List<RepoResponse>,String> restResult = RestClient.get(MyUri.from(url),
                                                                    buildHeaders(),
                                                                    new TypeReference<List<RepoResponse>>() {});
        if(restResult.getValue1() != null) {
            return Pair.with(null, restResult.getValue1());
        }

        List<Repository> response = restResult.getValue0().stream().map(RepoResponse::toRepository)
            .filter(r -> !r.getIsPrivate()).collect(Collectors.toList());
        return Pair.with(response, null);
    }

    public Pair<List<GithubFile>, String> readPath(User user, Repository repository, String path) {
        String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", user.getGithubLogin(), repository.getName(), path);
        Pair<String,String> restResult = RestClient.get(MyUri.from(url), buildHeaders());
        if(restResult.getValue1() != null) {
            return Pair.with(null, restResult.getValue1());
        }

        String body = restResult.getValue0();
        Pair<JsonNode,String> node = JsonMarshal.getJsonNode(body);
        if(node.getValue1() != null) {
            return Pair.with(null, "unable to read path: " + node.getValue1());
        }

        if(node.getValue0().isArray()) {
            Pair<List<PathReadFileResponse>, String> dirResult
                = JsonMarshal.convert(node.getValue0(), new TypeReference<List<PathReadFileResponse>>() {});
            List<GithubFile> files = dirResult.getValue0().stream().map(fileResp -> {
                    return new GithubFile(fileResp.type(), "/some/path", fileResp.name(), null);
                }).collect(Collectors.toList());
            return Pair.with(files, null);
        } else {
            Pair<PathReadFileResponse, String> fileResult
                = JsonMarshal.convert(node.getValue0(), new TypeReference<PathReadFileResponse>() {});
            List<GithubFile> files = new ArrayList<>();
            PathReadFileResponse fileResp = fileResult.getValue0();
            files.add(new GithubFile(fileResp.type(), "/some/path", fileResp.name(), fileResp.content()));
            return Pair.with(files, null);
        }
    }

    private Map<String,String> buildHeaders() {
        Map<String,String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);
        headers.put("X-GitHub-Api-Version", "2022-11-28");
        return headers;
    }
}
