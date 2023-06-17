package com.mikedll.headshot.controller;

import jakarta.servlet.http.HttpServletResponse;

import org.thymeleaf.context.Context;
import org.javatuples.Pair;

import com.mikedll.headshot.JsonMarshal;
import com.mikedll.headshot.model.GithubService;
import com.mikedll.headshot.model.GithubPathInfo;
import com.mikedll.headshot.model.Repository;
import com.mikedll.headshot.model.RepositoryService;

public class GithubController extends Controller {

    RepositoryService repositoryService;

    GithubService githubService;
    
    @Override
    public void acquireDbAccess() {
        this.repositoryService = new RepositoryService(this, dbConf);
        this.githubService = new GithubService(this, this.currentUser.getAccessToken());
    }
    
    @Request(path="/github/readDir")
    public void index() {
        Pair<Repository, Boolean> loadResult = ResourceLoader.loadRepository(this, this.repositoryService, this.currentUser, req.getParameter("repositoryId"));
        if(!loadResult.getValue1()) {
            return;
        }
        Repository repository = loadResult.getValue0();
        String path = req.getParameter("path");
        
        Pair<GithubPathInfo, String> result = this.githubService.readPath(this.currentUser, repository, path);

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
