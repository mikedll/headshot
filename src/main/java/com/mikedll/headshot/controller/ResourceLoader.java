package com.mikedll.headshot.controller;

import java.util.Optional;
import jakarta.servlet.http.HttpServletResponse;

import org.javatuples.Pair;

import com.mikedll.headshot.model.User;
import com.mikedll.headshot.model.Repository;
import com.mikedll.headshot.model.RepositoryService;

public class ResourceLoader {
    public static Pair<Repository, Boolean> loadRepository(Controller controller, RepositoryService service, User user, String stringId) {
        Long id = null;
        try {
            id = Long.parseLong(stringId);
        } catch (NumberFormatException ex) {
            controller.sendInternalServerError("unable to parse id");
            return Pair.with(null, false);
        }

        Pair<Optional<Repository>, String> repoResult = service.forUserAndId(user, id);
        if(repoResult.getValue1() != null) {
            controller.sendInternalServerError(repoResult.getValue1());
            return Pair.with(null, false);
        }

        if(!repoResult.getValue0().isPresent()) {
            controller.sendError(HttpServletResponse.SC_NOT_FOUND, "the requested resource was not found");
            return Pair.with(null, false);
        }

        return Pair.with(repoResult.getValue0().get(), true);
    }
}
