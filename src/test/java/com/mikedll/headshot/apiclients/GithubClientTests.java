package com.mikedll.headshot.apiclients;

import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.javatuples.Pair;
import org.apache.commons.io.FileUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import com.mikedll.headshot.Factories;
import com.mikedll.headshot.DbTest;
import com.mikedll.headshot.DbSuite;
import com.mikedll.headshot.TestSuite;
import com.mikedll.headshot.controller.Controller;
import com.mikedll.headshot.controller.ControllerUtils;
import com.mikedll.headshot.model.User;
import com.mikedll.headshot.model.Repository;
import com.mikedll.headshot.model.UserRepository;
import com.mikedll.headshot.util.RestClient;
import com.mikedll.headshot.util.MyUri;

public class GithubClientTests extends DbTest {

    private DbSuite suite;
    
    @BeforeEach
    public void setup() {
        this.suite = TestSuite.getSuite(DbSuite.class);
    }
    
    @Test
    public void testRestDir() throws IOException {
        Controller controller = Mockito.mock(Controller.class);
        Mockito.when(controller.getRepository(UserRepository.class)).thenReturn(ControllerUtils.getRepository(UserRepository.class));
        
        User user = Factories.createUser();
        Repository repository = Factories.createRepository(user);

        RestClient restClient = Mockito.mock(RestClient.class);
        String responseBody = FileUtils.readFileToString(new File("src/test/files/rest_responses/someDir.json"), "UTF-8");

        // dunno how to specify more concrete Map.class
        @SuppressWarnings("unchecked")
            OngoingStubbing z = Mockito.when(restClient.get(Mockito.any(URI.class), Mockito.any(Map.class)))
            .thenReturn(Pair.with(responseBody, null));
        
        GithubClient client = new GithubClient(this.suite.getApp().logger, restClient, controller, "myAccessToken");
        Pair<GithubPath, String> result = client.readPath(user, repository, "some/root");
        Assertions.assertNull(result.getValue1(), "successful read");
        List<GithubFile> files = new ArrayList<>();
        files.add(new GithubFile("file", ".gitignore", null, false));
        files.add(new GithubFile("file", ".ruby-gemset", null, false));
        GithubPath expected = new GithubPath("some/root", false, files);
        Assertions.assertEquals(expected, result.getValue0());
    }

    // read file
    @Test
    public void testRestFile() throws IOException {
        Controller controller = Mockito.mock(Controller.class);
        Mockito.when(controller.getRepository(UserRepository.class)).thenReturn(ControllerUtils.getRepository(UserRepository.class));
        
        User user = Factories.createUser();
        Repository repository = Factories.createRepository(user);

        RestClient restClient = Mockito.mock(RestClient.class);
        String responseBody = FileUtils.readFileToString(new File("src/test/files/rest_responses/myRubyFile.json"), "UTF-8");
        String decodedContent = FileUtils.readFileToString(new File("src/test/files/rest_responses/aGemfile"), "UTF-8");
        @SuppressWarnings("unchecked")
            OngoingStubbing z = Mockito.when(restClient.get(Mockito.any(URI.class), Mockito.any(Map.class)))
            .thenReturn(Pair.with(responseBody, null));
        
        GithubClient client = new GithubClient(this.suite.getApp().logger, restClient, controller, "myAccessToken");
        Pair<GithubPath, String> result = client.readPath(user, repository, "some/file.rb");
        Assertions.assertNull(result.getValue1(), "successful read");
        List<GithubFile> files = new ArrayList<>();
        files.add(new GithubFile("file", "myRubyFile.rb", decodedContent, true));
        GithubPath expected = new GithubPath("some/file.rb", true, files);
        Assertions.assertEquals(expected, result.getValue0());
    }

    @Test
    public void testNonUtf8File() throws IOException, JsonProcessingException {
        Controller controller = Mockito.mock(Controller.class);
        Mockito.when(controller.getRepository(UserRepository.class)).thenReturn(ControllerUtils.getRepository(UserRepository.class));
        
        User user = Factories.createUser();
        Repository repository = Factories.createRepository(user);

        RestClient restClient = Mockito.mock(RestClient.class);
        String responseBody = FileUtils.readFileToString(new File("src/test/files/rest_responses/jungKook.json"), "UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(responseBody);
        String base64 = node.findValue("content").asText();
        
        @SuppressWarnings("unchecked")
            OngoingStubbing z = Mockito.when(restClient.get(Mockito.any(URI.class), Mockito.any(Map.class)))
            .thenReturn(Pair.with(responseBody, null));
        
        GithubClient client = new GithubClient(this.suite.getApp().logger, restClient, controller, "myAccessToken");
        Pair<GithubPath, String> result = client.readPath(user, repository, "some/file.rb");
        Assertions.assertNull(result.getValue1(), "successful read");
        List<GithubFile> files = new ArrayList<>();
        files.add(new GithubFile("file", "jungKook.jpg", base64, false));
        GithubPath expected = new GithubPath("some/file.rb", true, files);
        Assertions.assertEquals(expected, result.getValue0());        
    }

    @Test
    public void testPullNewUser() {
        Controller controller = Mockito.mock(Controller.class);
        Mockito.when(controller.getRepository(UserRepository.class)).thenReturn(ControllerUtils.getRepository(UserRepository.class));
        
        RestClient restClient = Mockito.mock(RestClient.class);
        GithubUserResponse userResp = new GithubUserResponse(10L, "mikelogin", "Mike", "http://mike",
                                                             "http://mike/html", "http://mike/repos");
     
        @SuppressWarnings("unchecked")
            OngoingStubbing z = Mockito.when(restClient.get(Mockito.any(URI.class), Mockito.any(Map.class), Mockito.any(TypeReference.class)))
            .thenReturn(Pair.with(userResp, null));
        
        GithubClient client = new GithubClient(this.suite.getApp().logger, restClient, controller, "myAccessToken");
        Pair<User,String> pullResult = client.pullUserInfo();
        Assertions.assertNull(pullResult.getValue1(), "pulluser success");
        User user = pullResult.getValue0();
        Assertions.assertEquals(10L, user.getGithubId());        
        Assertions.assertEquals("mikelogin", user.getGithubLogin());
        Assertions.assertEquals("Mike", user.getName());
        Assertions.assertEquals("http://mike", user.getUrl());
        Assertions.assertEquals("http://mike/html", user.getHtmlUrl());
        Assertions.assertEquals("http://mike/repos", user.getReposUrl());
        Assertions.assertEquals("myAccessToken", user.getAccessToken());
    }

    @Test
    public void pullExistingUser() {
        User user = Factories.createUser();
        Controller controller = Mockito.mock(Controller.class);
        Mockito.when(controller.getRepository(UserRepository.class)).thenReturn(ControllerUtils.getRepository(UserRepository.class));
        
        RestClient restClient = Mockito.mock(RestClient.class);
        GithubUserResponse userResp = new GithubUserResponse(user.getGithubId(), "mikelogin", "Mike", "http://mike",
                                                             "http://mike/html", "http://mike/repos");
     
        @SuppressWarnings("unchecked")
            OngoingStubbing z = Mockito.when(restClient.get(Mockito.any(URI.class), Mockito.any(Map.class), Mockito.any(TypeReference.class)))
            .thenReturn(Pair.with(userResp, null));
        
        GithubClient client = new GithubClient(this.suite.getApp().logger, restClient, controller, "myAccessToken");
        Pair<User,String> pullResult = client.pullUserInfo();
        Assertions.assertNull(pullResult.getValue1(), "pulluser success");

        UserRepository userRepository = ControllerUtils.getRepository(UserRepository.class);
        Assertions.assertEquals(1L, userRepository.count().getValue0(), "only 1 user");
    }
}
