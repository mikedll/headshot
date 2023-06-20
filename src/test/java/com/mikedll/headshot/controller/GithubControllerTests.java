package com.mikedll.headshot.controller;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import jakarta.servlet.http.HttpServletResponse;

import org.javatuples.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.*;

import com.mikedll.headshot.model.RepositoryRepository;
import com.mikedll.headshot.model.Repository;
import com.mikedll.headshot.model.User;
import com.mikedll.headshot.DbSuite;
import com.mikedll.headshot.TestSuite;
import com.mikedll.headshot.Factories;
import com.mikedll.headshot.apiclients.ApiClientManager;
import com.mikedll.headshot.apiclients.GithubClient;

public class GithubControllerTests {

    @BeforeEach
    public void beforeEach() {
        if(!TestSuite.getSuite(DbSuite.class).beforeEach()) {
            Assertions.fail("beforeEach failed");
        }
    }

    @Test
    public void testLoadRepos() throws IOException {
        User user = Factories.createUser();
        ControllerUtils.app.apiClientManager = mock(ApiClientManager.class);

        GithubClient client = mock(GithubClient.class);
        List<Repository> repos = new ArrayList<Repository>();
        repos.add(Factories.buildRepository());
        repos.add(Factories.buildRepository());
        when(client.getRepositories(anyString())).thenReturn(Pair.with(repos, null));
        when(ControllerUtils.app.apiClientManager.getGithubClient(any(Controller.class), anyString())).thenReturn(client);

        TestRequest request = ControllerUtils.builder().withUser(user).build().put("/github/loadRepos");

        // System.out.println(request.res().getStatus());
        verify(request.res()).setStatus(HttpServletResponse.SC_OK);
        // Assertions.assertTrue(request.responseBody().contains("Hello"));

        RepositoryRepository repositoryRepository = ControllerUtils.getRepository(RepositoryRepository.class);

        Pair<List<Repository>,String> reposResult = repositoryRepository.forUser(user);
        List<Repository> createdRepos = reposResult.getValue0();
        repos.forEach(expectedRepo -> {
                Repository found = createdRepos.stream().filter(r -> r.getGithubId().equals(expectedRepo.getGithubId())).findAny().orElse(null);
                Assertions.assertNotNull(found, "created repo");
            });
    }
}
