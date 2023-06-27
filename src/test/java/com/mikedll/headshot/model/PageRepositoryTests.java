package com.mikedll.headshot.model;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.javatuples.Pair;

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
        System.out.println("createdAt after save: " + page.getCreatedAt());
        
        Assertions.assertEquals(1L, pageRepository.count().getValue0(), "count of 1");

        Pair<Optional<Page>,String> findResult = pageRepository.findById(page.getId());
        Assertions.assertNull(findResult.getValue1(), "find ok");
        Page foundPage = findResult.getValue0().orElse(null);
        Assertions.assertNotNull(foundPage, "in db");

        Assertions.assertEquals(page.getId(), foundPage.getId());
        Assertions.assertEquals(page.getTourId(), foundPage.getTourId());
        Assertions.assertEquals(page.getCreatedAt(), foundPage.getCreatedAt());
        Assertions.assertEquals(page.getFilename(), foundPage.getFilename());
        Assertions.assertEquals(page.getLineNumber(), foundPage.getLineNumber());
        Assertions.assertEquals(page.getLanguage(), foundPage.getLanguage());
        Assertions.assertEquals(page.getContent(), foundPage.getContent());
        Assertions.assertEquals(page.getNarration(), foundPage.getNarration());
    }
}
