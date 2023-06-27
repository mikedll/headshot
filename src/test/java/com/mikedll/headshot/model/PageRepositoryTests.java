package com.mikedll.headshot.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import com.mikedll.headshot.model.Page;
import com.mikedll.headshot.model.PageRepository;
import com.mikedll.headshot.Factories;
import com.mikedll.headshot.DbTest;
import com.mikedll.headshot.controller.ControllerUtils;

public class PageRepositoryTests extends DbTest {

    @Test
    public void testCreate() {
        User user = Factories.createUser();
        Tour tour = Factories.createTour(user);
        Page page = Factories.buildPage(tour);
        PageRepository pageRepository = ControllerUtils.getRepository(PageRepository.class);

        String error = pageRepository.save(page);
        Assertions.assertNull(error, "saved");
        Assertions.assertNotNull(page.getId(), "id set");
        Assertions.assertNotNull(page.getCreatedAt(), "createdAt set");
        Assertions.assertEquals(1L, pageRepository.count().getValue0(), "count of 1");
    }
}
