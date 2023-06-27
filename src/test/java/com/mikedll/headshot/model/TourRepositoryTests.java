package com.mikedll.headshot.model;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.javatuples.Pair;

import com.mikedll.headshot.Factories;
import com.mikedll.headshot.DbTest;
import com.mikedll.headshot.controller.ControllerUtils;

public class TourRepositoryTests extends DbTest {

    @Test
    public void testCreate() {
        User user = Factories.createUser();
        Tour tour = Factories.buildTour(user);
        TourRepository tourRepository = ControllerUtils.getRepository(TourRepository.class);

        String error = tourRepository.save(tour);
        Assertions.assertNull(error, "save success");
        Assertions.assertNotNull(tour.getId(), "id was set");
        Assertions.assertNotNull(tour.getCreatedAt(), "createdAt was set");

        Pair<Optional<Tour>, String> fetchResult = tourRepository.findById(tour.getId());
        Assertions.assertNull(fetchResult.getValue1(), "fetch ok");
        Tour foundTour = fetchResult.getValue0().orElse(null);
        Assertions.assertNotNull(foundTour);
        Assertions.assertEquals(tour.getId(), foundTour.getId());
        Assertions.assertEquals(tour.getUserId(), foundTour.getUserId());
        Assertions.assertEquals(tour.getCreatedAt(), foundTour.getCreatedAt());
        Assertions.assertEquals(tour.getName(), foundTour.getName());
    }
}
