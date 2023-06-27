package com.mikedll.headshot.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

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
        Assertions.assertEquals(1L, tourRepository.count().getValue0(), "one inserted");
    }
}
