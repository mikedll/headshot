package com.mikedll.headshot;

import com.mikedll.headshot.model.User;
import com.mikedll.headshot.model.UserRepository;

public class Factories {

    public static User makeUser() {
        Application app = TestSuite.getSuite(DbSuite.class).getApp();
        UserRepository userRepository = app.dbConf.getRepository(app, UserRepository.class);
        User user = new User();
        user.setName("Randal Johnson");
        user.setGithubId(2000L);
        user.setGithubLogin("randal.johnson");
        user.setUrl("http://api.github.com/randal.johnson");
        user.setHtmlUrl("http://www.github.com/randal.johnson");
        user.setReposUrl("http://api.github.com/randal.johnson/repos");
        user.setAccessToken("asdf");
        userRepository.save(user);
        return user;
    }

    
}
