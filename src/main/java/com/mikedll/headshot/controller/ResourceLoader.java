package com.mikedll.headshot.controller;

import java.util.Optional;
import jakarta.servlet.http.HttpServletResponse;

import org.javatuples.Pair;

import com.mikedll.headshot.model.User;
import com.mikedll.headshot.model.Tour;
import com.mikedll.headshot.model.Repository;
import com.mikedll.headshot.model.RepositoryRepository;
import com.mikedll.headshot.model.TourRepository;

public class ResourceLoader {
    public static Long parseId(Controller controller, String stringId) {
        Long id = null;
        try {
            id = Long.parseLong(stringId); 
            return id;
        } catch (NumberFormatException ex) {
            controller.sendInternalServerError("unable to parse id");
        } 
        return null;
   }

    public static <T> Optional<T> checkResult(Controller controller, Pair<Optional<T>, String> fetchResult) {
        if(fetchResult.getValue1() != null) {
            controller.sendInternalServerError(fetchResult.getValue1());
            return Optional.empty();
        }

        if(!fetchResult.getValue0().isPresent()) {
            controller.sendError(HttpServletResponse.SC_NOT_FOUND, "the requested resource was not found");
            return Optional.empty();
        }

        return fetchResult.getValue0();
    }
    
    public static Optional<Repository> loadRepository(Controller controller, RepositoryRepository repository, User user, String stringId) {
        Long id = parseId(controller, stringId);
        if(id == null) {
            return Optional.empty();
        }
        
        return checkResult(controller, repository.forUserAndId(user, id));
    }

    public static Optional<Tour> loadTour(Controller controller, TourRepository repository, User user, String stringId) {
        Long id = parseId(controller, stringId);
        if(id == null) {
            return Optional.empty();
        }

        return checkResult(controller, repository.forUserAndId(user, id));
    }
}
