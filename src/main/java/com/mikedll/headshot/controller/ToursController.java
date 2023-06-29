package com.mikedll.headshot.controller;

import java.util.List;

import org.thymeleaf.context.Context;
import org.javatuples.Pair;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.core.type.TypeReference;

import com.mikedll.headshot.util.JsonMarshal;
import com.mikedll.headshot.model.Tour;
import com.mikedll.headshot.model.TourRepository;

public class ToursController extends Controller {

    private TourRepository tourRepository;
    
    @Override
    public void acquireDataAccess() {
        this.tourRepository = getRepository(TourRepository.class);
    }

    @Request(path="/tours")
    public void index() {
        Pair<List<Tour>,String> toursFetch = tourRepository.forUser(this.currentUser);
        if(toursFetch.getValue1() != null) {
            sendInternalServerError(toursFetch.getValue1());
            return;
        }
        
        Context ctx = defaultCtx();
        ctx.setVariable("tours", toursFetch.getValue0());
        render("tours/index", ctx);
    }

    @Request(path="/tours", method=HttpMethod.POST)
    public void create() {
        Tour tour = new Tour();
        tour.setUserId(this.currentUser.getId());
        tour.setName("Untitled Tour");
        String error = tourRepository.save(tour);
        if(error != null) {
            sendInternalServerError(error);
            return;
        }

        Pair<String,String> marshal = JsonMarshal.marshal(getJsonObjectMapper(), tour);
        if(marshal.getValue1() != null) {
            sendInternalServerError(marshal.getValue1());
            return;
        }
        
        sendJson(marshal.getValue0());
    }

    @Request(path="/tours/{id}", method=HttpMethod.DELETE)
    public void delete() {
        Tour tour = ResourceLoader.loadTour(this, tourRepository, this.currentUser, this.getPathParam("id")).orElse(null);
        if(tour == null) {
            return;
        }
           
        String error = tourRepository.delete(tour.getId());
        if(error != null) {
            sendInternalServerError(error);
            return;
        }

        res.setStatus(HttpServletResponse.SC_OK);
    }

    @Request(path="/tours/{id}", method=HttpMethod.PUT)
    public void update() {
        Tour tour = ResourceLoader.loadTour(this, tourRepository, this.currentUser, this.getPathParam("id")).orElse(null);
        if(tour == null) {
            return;
        }

        Pair<Tour,String> unmarshalled = Unmarshal.go(getJsonObjectMapper(), req, new TypeReference<Tour>() {});
        if(unmarshalled.getValue1() != null) {
            sendInternalServerError(unmarshalled.getValue1());
            return;
        }

        Tour fromBody = unmarshalled.getValue0();
        tour.setName(fromBody.getName());

        String error = tourRepository.save(tour);
        if(error != null) {
            sendInternalServerError(error);
            return;
        }

        Pair<String,String> response = JsonMarshal.marshal(getJsonObjectMapper(), tour);
        if(response.getValue1() != null) {
            sendInternalServerError(response.getValue1());
            return;
        }

        sendJson(response.getValue0());
    }
}
