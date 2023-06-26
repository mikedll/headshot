package com.mikedll.headshot.model;

import java.util.Optional;
import java.lang.InterruptedException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.javatuples.Pair;

import com.mikedll.headshot.Factories;
import com.mikedll.headshot.DbTest;
import com.mikedll.headshot.controller.ControllerUtils;

public class UserRepositoryTests extends DbTest {

    @Test
    public void testInsert() throws InterruptedException {
        User user = Factories.buildUser();
        UserRepository userRepository = ControllerUtils.getRepository(UserRepository.class);
        String error = userRepository.save(user);
        Assertions.assertNull(error, "save success");
        Assertions.assertNotNull(user.getId(), "id present");
        
        Pair<Optional<User>,String> fetchUser = userRepository.findById(user.getId());
        Assertions.assertNull(fetchUser.getValue1());
        Assertions.assertNotNull(fetchUser.getValue0().orElse(null), "user is in db");
    }

    @Test
    public void testUpdate() {
        User user = Factories.createUser();
        UserRepository userRepository = ControllerUtils.getRepository(UserRepository.class);
        user.setName("Eddie");
        String error = userRepository.save(user);
        Assertions.assertNull(error, "save success");
        Assertions.assertNotNull(user.getId(), "id present");

        Pair<Optional<User>,String> user2Fetch = userRepository.findById(user.getId());
        Assertions.assertNull(user2Fetch.getValue1(), "findById");
        User user2 = user2Fetch.getValue0().orElse(null);
        Assertions.assertEquals("Eddie", user2.getName(), "name updated");
    }    
}
