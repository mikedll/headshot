package com.mikedll.headshot.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.javatuples.Pair;

import com.mikedll.headshot.DbTest;
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

}
