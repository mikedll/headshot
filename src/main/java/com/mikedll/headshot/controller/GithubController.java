package com.mikedll.headshot.controller;

import java.util.List;
import java.util.regex.Pattern;
import jakarta.servlet.http.HttpServletResponse;

import org.thymeleaf.context.Context;
import org.javatuples.Pair;

import com.mikedll.headshot.JsonMarshal;
import com.mikedll.headshot.apiclients.GithubClient;
import com.mikedll.headshot.apiclients.GithubFile;
import com.mikedll.headshot.model.Repository;
import com.mikedll.headshot.model.RepositoryRepository;

public class GithubController extends Controller {

    RepositoryRepository repositoryRepository;

    GithubClient githubClient;
    
    @Override
    public void acquireDataAccess() {
        this.repositoryRepository = getRepository(RepositoryRepository.class);
        this.githubClient = getGithubClient(this.currentUser.getAccessToken());
    }

    @Request(path="/github/loadRepos", method=HttpMethod.PUT)
    public void loadRepos() {
        Pair<List<Repository>,String> repositoriesResult = this.githubClient.getRepositories(this.currentUser.getGithubLogin());
        if(repositoriesResult.getValue1() != null) {
            sendInternalServerError(repositoriesResult.getValue1());
            return;
        }

        List<Repository> repositories = repositoriesResult.getValue0();
        String error = this.repositoryRepository.save(this.currentUser, repositories);
        if(error != null) {
            sendInternalServerError(error);
            return;
        }
        res.setStatus(HttpServletResponse.SC_OK);
    }
    
    @Request(path="/github/readDir/{id}")
    public void index() {
        Repository repository = ResourceLoader.loadRepository(this, this.repositoryRepository, this.currentUser, this.getPathParam("id")).orElse(null);
        if(repository == null) {
            return;
        }

        String path = req.getRequestURI().toString().replaceFirst("^" + Pattern.quote(this.pathMatch.matched()) + "/?", "");
        Pair<List<GithubFile>, String> result = this.githubClient.readPath(this.currentUser, repository, path);

        if(result.getValue1() != null) {
            sendInternalServerError(result.getValue1());
            return;
        }

        Pair<String,String> marshalled = JsonMarshal.marshal(result.getValue0());
        if(marshalled.getValue1() != null) {
            sendInternalServerError(marshalled.getValue1());
            return;
        }

        sendJson(marshalled.getValue0());
    }

}
