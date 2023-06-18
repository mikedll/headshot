package com.mikedll.headshot.controller;

import java.util.Optional;
import jakarta.servlet.http.HttpServletResponse;

import org.javatuples.Pair;

import com.mikedll.headshot.model.User;
import com.mikedll.headshot.model.Repository;
import com.mikedll.headshot.model.RepositoryService;

public class ResourceLoader {
    public static Optional<Repository> loadRepository(Controller controller, RepositoryService service, User user, String stringId) {
        Long id = null;
        try {
            id = Long.parseLong(stringId);
        } catch (NumberFormatException ex) {
            controller.sendInternalServerError("unable to parse id");
            return Optional.empty();
        }

        Pair<Optional<Repository>, String> repoResult = service.forUserAndId(user, id);
        if(repoResult.getValue1() != null) {
            controller.sendInternalServerError(repoResult.getValue1());
            return Optional.empty();
        }

        if(!repoResult.getValue0().isPresent()) {
            controller.sendError(HttpServletResponse.SC_NOT_FOUND, "the requested resource was not found");
            return Optional.empty();
        }

        return repoResult.getValue0();
    }
}
