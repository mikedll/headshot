package com.mikedll.headshot.model;

import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import java.util.stream.Collectors;

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

    @Test
    public void testForUser() {
        User user = Factories.createUser();
        Tour tour1 = Factories.createTour(user);
        Tour tour2 = Factories.createTour(user);

        User user2 = Factories.createUser();
        Tour tour3 = Factories.createTour(user2);
        
        TourRepository tourRepository = ControllerUtils.getRepository(TourRepository.class);
        Pair<List<Tour>,String> fetchResult = tourRepository.forUser(user);
        Assertions.assertNull(fetchResult.getValue1(), "fetch ok");
        List<Tour> foundTours = fetchResult.getValue0();
        Assertions.assertEquals(2, foundTours.size(), "found 2");
        List<Long> expected = Arrays.asList(new Long[] { tour1.getId(), tour2.getId() });
        List<Long> foundIds = foundTours.stream().map(t -> t.getId()).collect(Collectors.toList());
        Assertions.assertTrue(expected.size() == foundIds.size() &&
                              expected.containsAll(foundIds) &&
                              foundIds.containsAll(expected), "correct tours returned");
    }

    @Test
    public void testForUserAndId() {
        User user = Factories.createUser();
        Tour tour = Factories.createTour(user);
        TourRepository tourRepository = ControllerUtils.getRepository(TourRepository.class);
        Pair<Optional<Tour>,String> fetchResult = tourRepository.forUserAndId(user, tour.getId());

        Assertions.assertNull(fetchResult.getValue1(), "fetch ok");
        Assertions.assertEquals(tour.getId(), fetchResult.getValue0().orElse(null).getId(), "found");
    }
        
}
