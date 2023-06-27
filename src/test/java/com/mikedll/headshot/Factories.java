package com.mikedll.headshot;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import com.mikedll.headshot.model.User;
import com.mikedll.headshot.model.Page;
import com.mikedll.headshot.model.Tour;
import com.mikedll.headshot.model.Repository;
import com.mikedll.headshot.model.UserRepository;
import com.mikedll.headshot.model.TourRepository;
import com.mikedll.headshot.model.RepositoryRepository;

public class Factories {

    private static long repoGithubIdI = 2000L;

    private static long repoI = 2000L;
    
    private static long accessTokenI = 50L;

    private static long filenameI = 150L;

    private static int lineNumberI = 200;

    private static long tourI = 1L;
    
    public static String sourceCode() {
        try {
            return FileUtils.readFileToString(new File("src/test/files/sample_source_files/cknife_aws.rb"), "UTF-8");
        } catch (IOException ex) {
            throw new RuntimeException("IOException when reading source code", ex);
        }
    }
    
    public static User buildUser() {
        User user = new User();
        user.setName("Randal Johnson");
        user.setGithubId(2000L);
        user.setGithubLogin("randal.johnson");
        user.setUrl("http://api.github.com/randal.johnson");
        user.setHtmlUrl("http://www.github.com/randal.johnson");
        user.setReposUrl("http://api.github.com/randal.johnson/repos");
        user.setAccessToken("asdf" + accessTokenI);
        return user;
    }
    
    public static User createUser() {
        Application app = TestSuite.getSuite(DbSuite.class).getApp();
        UserRepository userRepository = app.dbConf.getRepository(app, UserRepository.class);
        User user = buildUser();
        userRepository.save(user);
        return user;
    }

    public static Repository buildRepository() {
        repoI++;
        repoGithubIdI++;
        
        Repository repository = new Repository();
        repository.setGithubId(repoGithubIdI);
        repository.setName("activeadmin" + repoI);
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

    public static Tour buildTour(User user) {
        tourI++;
        
        Tour tour = new Tour();
        tour.setUserId(user.getId());
        tour.setName("Lovely Tour " + tourI);
        return tour;
    }

    public static Tour createTour(User user) {
        Tour tour = buildTour(user);
        
        Application app = TestSuite.getSuite(DbSuite.class).getApp();
        TourRepository tourRepository = app.dbConf.getRepository(app, TourRepository.class);
        tourRepository.save(tour);

        return tour;
    }
    
    public static Page buildPage(Tour tour) {
        filenameI++;
        lineNumberI++;
        
        Page page = new Page();
        page.setTourId(tour.getId());
        page.setFilename("myFile" + filenameI + ".rb");
        page.setLineNumber(lineNumberI);
        page.setLanguage("ruby");
        page.setContent(sourceCode());
        page.setNarration("The subject of this method is farming corn. The farmer comes in and we examine whether he wants to harvest, or leave the corn to grow more.");
        return page;
    }
    
}
