package com.mikedll.headshot.controller;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.javatuples.Pair;
import jakarta.servlet.http.HttpServletResponse;

import com.mikedll.headshot.Factories;
import com.mikedll.headshot.model.User;
import com.mikedll.headshot.model.Tour;
import com.mikedll.headshot.model.TourRepository;

public class ToursControllerTests extends ControllerTest {

    @Test
    public void testCreate() {
        User user = Factories.createUser();
        TestRequest request = ControllerUtils.builder().withUser(user)
            .expectStatus(HttpServletResponse.SC_OK)
            .build().post("/tours");

        Assertions.assertTrue(request.responseBody().contains("\"userId\":" + user.getId()));

        TourRepository tourRepository = ControllerUtils.getRepository(TourRepository.class);
        Pair<List<Tour>, String> fetchResult = tourRepository.forUser(user);
        Assertions.assertNull(fetchResult.getValue1(), "fetch ok");
        Assertions.assertEquals(user.getId(), fetchResult.getValue0().get(0).getId());
    }

    @Test
    public void testDelete() {
        User user = Factories.createUser();
        Tour tour = Factories.createTour(user);
        TestRequest request = ControllerUtils.builder().withUser(user)
            .expectStatus(HttpServletResponse.SC_OK)
            .build().delete("/tours/" + tour.getId());

        TourRepository tourRepository = ControllerUtils.getRepository(TourRepository.class);
        Pair<Optional<Tour>, String> fetchResult = tourRepository.findById(tour.getId());
        Assertions.assertNull(fetchResult.getValue1(), "fetch ok");
        Assertions.assertNull(fetchResult.getValue0().orElse(null), "deleted");
    }
}
