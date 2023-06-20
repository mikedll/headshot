package com.mikedll.headshot.controller;

import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import jakarta.servlet.http.HttpServletResponse;

import org.thymeleaf.context.Context;
import org.javatuples.Pair;

import com.mikedll.headshot.model.RepositoryRepository;
import com.mikedll.headshot.model.Repository;
import com.mikedll.headshot.JsonMarshal;
import com.mikedll.headshot.util.PathUtils;
import com.mikedll.headshot.util.PathAncestor;

public class ReposController extends Controller {

    RepositoryRepository repositoryRepository;
    
    @Override
    public void acquireDataAccess() {
        this.repositoryRepository = getRepository(RepositoryRepository.class);
    }
    
    @Request(path="/repos")
    public void index() {
        Pair<List<Repository>, String> existingResult = repositoryRepository.forUser(this.currentUser);
        if(existingResult.getValue1() != null) {
            sendInternalServerError(existingResult.getValue1());
            return;
        }
        Context ctx = defaultCtx();
        ctx.setVariable("existing", existingResult.getValue0());
        render("repos/index", ctx);
    }

    @Request(path="/repos/{id}")
    public void getRepoPath() {
        Repository repository = ResourceLoader.loadRepository(this, this.repositoryRepository, this.currentUser, this.getPathParam("id")).orElse(null);
        if(repository == null) {
            return;
        }

        Context ctx = defaultCtx();
        String path = req.getRequestURI().toString();
        String repoPath = path.replaceFirst("^" + Pattern.quote(this.pathMatch.matched()) + "/?", "").replaceFirst("/$", "");

        ctx.setVariable("ancestors", PathUtils.pathAncestors(repository.getName(), repoPath));
        Map<String,Object> pathInfo = new HashMap<>();
        pathInfo.put("path", repoPath);
        pathInfo.put("repositoryId", repository.getId());
        ctx.setVariable("pathInfo", pathInfo);
        render("repos/path", ctx);
    }

}
