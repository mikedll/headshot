package com.mikedll.headshot.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

import org.thymeleaf.context.Context;
import org.javatuples.Pair;

import com.mikedll.headshot.model.GithubService;
import com.mikedll.headshot.model.RepositoryService;
import com.mikedll.headshot.model.Repository;

public class ReposController extends Controller {

    RepositoryService repositoryService;
    
    @Override
    public void acquireDbAccess() {
        this.repositoryService = new RepositoryService(this, dbConf);
    }
    
    @Request(path="/repos")
    public void index() {
        Pair<List<Repository>, String> existingResult = repositoryService.forUser(this.currentUser);
        if(existingResult.getValue1() != null) {
            sendInternalServerError(existingResult.getValue1());
            return;
        }
        Context ctx = defaultCtx();
        ctx.setVariable("existing", existingResult.getValue0());
        render("repos/index", ctx);
    }

    @Request(path="/repos/load", method=HttpMethod.PUT)
    public void loadRepos() {
        GithubService service = new GithubService(this, this.currentUser.getAccessToken());
        List<Repository> repositories = service.getRepositories(this.currentUser.getGithubLogin());
        String error = this.repositoryService.save(this.currentUser, repositories);
        if(error != null) {
            sendInternalServerError(error);
            return;
        }
        res.setStatus(HttpServletResponse.SC_OK);
    }
}
