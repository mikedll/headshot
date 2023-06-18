package com.mikedll.headshot.controller;

import java.util.List;
import java.util.regex.Pattern;
import jakarta.servlet.http.HttpServletResponse;

import org.thymeleaf.context.Context;
import org.javatuples.Pair;

import com.mikedll.headshot.JsonMarshal;
import com.mikedll.headshot.model.GithubService;
import com.mikedll.headshot.model.Repository;
import com.mikedll.headshot.model.RepositoryService;
import com.mikedll.headshot.model.GithubFile;

public class GithubController extends Controller {

    RepositoryService repositoryService;

    GithubService githubService;
    
    @Override
    public void acquireDbAccess() {
        this.repositoryService = new RepositoryService(this, dbConf);
        this.githubService = new GithubService(this, this.currentUser.getAccessToken());
    }
    
    @Request(path="/github/readDir/{id}")
    public void index() {
        Repository repository = ResourceLoader.loadRepository(this, this.repositoryService, this.currentUser, this.getPathParam("id")).orElse(null);
        if(repository == null) {
            return;
        }

        String path = req.getRequestURI().toString().replaceFirst("^" + Pattern.quote(this.pathMatch.matched()) + "/?", "");
        Pair<List<GithubFile>, String> result = this.githubService.readPath(this.currentUser, repository, path);

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
