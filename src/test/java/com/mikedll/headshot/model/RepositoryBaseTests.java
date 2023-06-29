package com.mikedll.headshot.model;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.javatuples.Pair;

import com.mikedll.headshot.DbTest;
import com.mikedll.headshot.Factories;
import com.mikedll.headshot.controller.ControllerUtils;

public class RepositoryBaseTests extends DbTest {

    /*
     * TourRepository is an arbitrary subclass.
     */
    @Test
    public void testCount() {
        TourRepository tourRepository = ControllerUtils.getRepository(TourRepository.class);
        Pair<Long,String> count = tourRepository.count();
        Assertions.assertNull(count.getValue1(), "count success");
        Assertions.assertEquals(0L, count.getValue0());
    }

    @Test
    public void testDelete() {
        Tour tour = Factories.createTour();
        TourRepository tourRepository = ControllerUtils.getRepository(TourRepository.class);
        Long id = tour.getId();
        String result = tourRepository.delete(tour.getId());
        Assertions.assertNull(result, "delete ok");
        Pair<Optional<Tour>,String> findResult = tourRepository.findById(id);
        Assertions.assertNull(findResult.getValue1(), "find ok");
        Assertions.assertNull(findResult.getValue0().orElse(null), "not found");
    }
}
