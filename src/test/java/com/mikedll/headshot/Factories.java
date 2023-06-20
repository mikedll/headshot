package com.mikedll.headshot;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

import com.mikedll.headshot.model.User;
import com.mikedll.headshot.model.Repository;
import com.mikedll.headshot.model.UserRepository;
import com.mikedll.headshot.model.RepositoryRepository;

public class Factories {

    private static long repoI = 2000L;
    
    public static User createUser() {
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

    public static Repository buildRepository() {
        repoI++;
        
        Repository repository = new Repository();
        repository.setGithubId(repoI);
        repository.setName("activeadmin " + repoI);
        repository.setIsPrivate(false);
        repository.setDescription("A ruby gem to provide a UI to a database");
        repository.setCreatedAt(Instant.now().minus(Duration.ofDays(30)));        
        return repository;
    }

    public static Repository createRepository(User user) {
        Repository repository = buildRepository();
        Application app = TestSuite.getSuite(DbSuite.class).getApp();
        RepositoryRepository repositoryRepository = app.dbConf.getRepository(app, RepositoryRepository.class);
        List<Repository> repositories = new ArrayList<>();
        repositories.add(repository);
        repositoryRepository.save(user, repositories);
        return repository;
    }
}
