package com.mikedll.headshot.controller;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import jakarta.servlet.http.HttpServletResponse;

import org.javatuples.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import com.mikedll.headshot.model.RepositoryRepository;
import com.mikedll.headshot.model.Repository;
import com.mikedll.headshot.model.User;
import com.mikedll.headshot.controller.ControllerUtils;
import com.mikedll.headshot.apiclients.ApiClientManager;
import com.mikedll.headshot.apiclients.GithubClient;
import com.mikedll.headshot.Factories;

public class GithubControllerTests extends ControllerTest {

    @Test
    public void testLoadRepos() throws IOException {
        User user = Factories.createUser();

        GithubClient client = Mockito.mock(GithubClient.class);
        List<Repository> repos = new ArrayList<Repository>();
        repos.add(Factories.buildRepository());
        repos.add(Factories.buildRepository());
        Mockito.when(client.getRepositories(Mockito.anyString())).thenReturn(Pair.with(repos, null));
        
        ApiClientManager apiClientManager = Mockito.mock(ApiClientManager.class);
        Mockito.when(apiClientManager.getGithubClient(Mockito.any(Controller.class), Mockito.anyString())).thenReturn(client);
        ControllerUtils.app.apiClientManager = apiClientManager;

        TestRequest request = ControllerUtils.builder().withUser(user).build().put("/github/loadRepos");

        Mockito.verify(request.res()).setStatus(HttpServletResponse.SC_OK);

        RepositoryRepository repositoryRepository = ControllerUtils.getRepository(RepositoryRepository.class);

        Pair<List<Repository>,String> reposResult = repositoryRepository.forUser(user);
        List<Repository> createdRepos = reposResult.getValue0();
        repos.forEach(expectedRepo -> {
                Repository found = createdRepos.stream().filter(r -> r.getGithubId().equals(expectedRepo.getGithubId())).findAny().orElse(null);
                Assertions.assertNotNull(found, "created repo");
            });
    }
}
