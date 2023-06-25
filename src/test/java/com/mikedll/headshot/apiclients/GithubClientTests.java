package com.mikedll.headshot.apiclients;

import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;
import org.apache.commons.io.FileUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;


import com.mikedll.headshot.Factories;
import com.mikedll.headshot.DbTest;
import com.mikedll.headshot.controller.Controller;
import com.mikedll.headshot.controller.ControllerUtils;
import com.mikedll.headshot.model.User;
import com.mikedll.headshot.model.Repository;
import com.mikedll.headshot.model.UserRepository;
import com.mikedll.headshot.util.RestClient;
import com.mikedll.headshot.util.MyUri;

public class GithubClientTests extends DbTest {

    @Test
    public void testRestDir() throws IOException {
        Controller controller = mock(Controller.class);
        when(controller.getRepository(UserRepository.class)).thenReturn(ControllerUtils.getRepository(UserRepository.class));
        
        User user = Factories.createUser();
        Repository repository = Factories.createRepository(user);

        RestClient restClient = mock(RestClient.class);
        String responseBody = FileUtils.readFileToString(new File("src/test/files/rest_responses/someDir.json"), "UTF-8");

        when(restClient.get(any(URI.class), any(Map.class))).thenReturn(Pair.with(responseBody, null));
        
        GithubClient client = new GithubClient(restClient, controller, "myAccessToken");
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
        Controller controller = mock(Controller.class);
        when(controller.getRepository(UserRepository.class)).thenReturn(ControllerUtils.getRepository(UserRepository.class));
        
        User user = Factories.createUser();
        Repository repository = Factories.createRepository(user);

        RestClient restClient = mock(RestClient.class);
        String responseBody = FileUtils.readFileToString(new File("src/test/files/rest_responses/myRubyFile.json"), "UTF-8");
        String decodedContent = FileUtils.readFileToString(new File("src/test/files/rest_responses/aGemfile"), "UTF-8");        
        when(restClient.get(any(URI.class), any(Map.class))).thenReturn(Pair.with(responseBody, null));
        
        GithubClient client = new GithubClient(restClient, controller, "myAccessToken");
        Pair<GithubPath, String> result = client.readPath(user, repository, "some/file.rb");
        Assertions.assertNull(result.getValue1(), "successful read");
        List<GithubFile> files = new ArrayList<>();
        files.add(new GithubFile("file", "myRubyFile.rb", decodedContent, true));
        GithubPath expected = new GithubPath("some/file.rb", true, files);
        Assertions.assertEquals(expected, result.getValue0());
    }

    @Test
    public void testNonUtf8File() throws IOException, JsonProcessingException {
        Controller controller = mock(Controller.class);
        when(controller.getRepository(UserRepository.class)).thenReturn(ControllerUtils.getRepository(UserRepository.class));
        
        User user = Factories.createUser();
        Repository repository = Factories.createRepository(user);

        RestClient restClient = mock(RestClient.class);
        String responseBody = FileUtils.readFileToString(new File("src/test/files/rest_responses/jungKook.json"), "UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(responseBody);
        String base64 = node.findValue("content").asText();

        when(restClient.get(any(URI.class), any(Map.class))).thenReturn(Pair.with(responseBody, null));
        
        GithubClient client = new GithubClient(restClient, controller, "myAccessToken");
        Pair<GithubPath, String> result = client.readPath(user, repository, "some/file.rb");
        Assertions.assertNull(result.getValue1(), "successful read");
        List<GithubFile> files = new ArrayList<>();
        files.add(new GithubFile("file", "jungKook.jpg", base64, false));
        GithubPath expected = new GithubPath("some/file.rb", true, files);
        Assertions.assertEquals(expected, result.getValue0());        
    }

    // handle legitimate error different from malformed encoding or unmappable symbol error
}
