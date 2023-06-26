package com.mikedll.headshot.model;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.javatuples.Pair;

import com.mikedll.headshot.Factories;
import com.mikedll.headshot.DbTest;
import com.mikedll.headshot.controller.ControllerUtils;

public class UserRepositoryTests extends DbTest {

    @Test
    public void testInsert() {
        User user = Factories.buildUser();
        UserRepository userRepository = ControllerUtils.getRepository(UserRepository.class);
        String error = userRepository.save(user);
        Assertions.assertNull(error, "save success");
        Assertions.assertNotNull(user.getId(), "id present");
        
        Pair<Optional<User>,String> fetchUser = userRepository.findById(user.getId());
        Assertions.assertNull(fetchUser.getValue1());
        User foundUser = fetchUser.getValue0().orElse(null);
        Assertions.assertNotNull(foundUser, "user is in db");
        Assertions.assertEquals(user.getId(), foundUser.getId());
        Assertions.assertEquals(user.getName(), foundUser.getName());
        Assertions.assertEquals(user.getGithubId(), foundUser.getGithubId());
        Assertions.assertEquals(user.getGithubLogin(), foundUser.getGithubLogin());
        Assertions.assertEquals(user.getUrl(), foundUser.getUrl());
        Assertions.assertEquals(user.getHtmlUrl(), foundUser.getHtmlUrl());
        Assertions.assertEquals(user.getReposUrl(), foundUser.getReposUrl());
        Assertions.assertEquals(user.getAccessToken(), foundUser.getAccessToken());

        Pair<Optional<User>,String> fetchUser2 = userRepository.findByGithubId(user.getGithubId());
        Assertions.assertNull(fetchUser2.getValue1());
        Assertions.assertNotNull(fetchUser2.getValue0().orElse(null), "find via GithubId");
    }

    @Test
    public void testUpdate() {
        User user = Factories.createUser();
        UserRepository userRepository = ControllerUtils.getRepository(UserRepository.class);
        user.setName("Eddie Burback");
        user.setGithubId(9000L);
        user.setGithubLogin("eddie.burback");
        user.setUrl("http://eddie");
        user.setHtmlUrl("http://eddie/html");
        user.setReposUrl("http://eddie/repos");
        user.setAccessToken("differentToken");        
        String error = userRepository.save(user);
        Assertions.assertNull(error, "save success");
        Assertions.assertNotNull(user.getId(), "id present");

        Pair<Optional<User>,String> user2Fetch = userRepository.findById(user.getId());
        Assertions.assertNull(user2Fetch.getValue1(), "findById");
        User user2 = user2Fetch.getValue0().orElse(null);
        Assertions.assertEquals("Eddie Burback", user2.getName());
        Assertions.assertEquals(9000L, user2.getGithubId());
        Assertions.assertEquals("eddie.burback", user2.getGithubLogin());
        Assertions.assertEquals("http://eddie", user2.getUrl());
        Assertions.assertEquals("http://eddie/html", user2.getHtmlUrl());
        Assertions.assertEquals("http://eddie/repos", user2.getReposUrl());
        Assertions.assertEquals("differentToken", user2.getAccessToken());                
    
    }    
}
