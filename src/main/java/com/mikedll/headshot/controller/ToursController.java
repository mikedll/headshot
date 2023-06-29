package com.mikedll.headshot.controller;

import java.util.List;

import org.thymeleaf.context.Context;
import org.javatuples.Pair;

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
        tour.setName("");
        String error = tourRepository.save(tour);
        if(error != null) {
            System.out.println("Error: " + error);
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
}
