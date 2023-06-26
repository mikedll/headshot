package com.mikedll.headshot.model;

import java.util.List;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.javatuples.Pair;

import com.mikedll.headshot.Factories;
import com.mikedll.headshot.TestSuite;
import com.mikedll.headshot.DbSuite;
import com.mikedll.headshot.DbTest;
import com.mikedll.headshot.controller.ControllerUtils;

public class RepositoryRepositoryTests extends DbTest {
    
    @Test
    public void testSave() {
        User user = Factories.createUser();
        List<Repository> repositories = new ArrayList<>();
        repositories.add(Factories.buildRepository());

        RepositoryRepository repositoryRepository = ControllerUtils.getRepository(RepositoryRepository.class);

        String error = repositoryRepository.save(user, repositories);
        Assertions.assertNull(error, "save success");
        Assertions.assertNotNull(repositories.get(0).getId(), "id set");
    }

    @Test
    public void testSaveWhenExists() {
        User user = Factories.createUser();
        List<Repository> repositories = new ArrayList<>();
        Repository repo = Factories.buildRepository();
        repo.setId(10L);
        repositories.add(repo);
        repositories.add(Factories.buildRepository());
        
        RepositoryRepository repositoryRepository = ControllerUtils.getRepository(RepositoryRepository.class);
        String error = repositoryRepository.save(user, repositories);
        Assertions.assertEquals("can't save Repository that has id (found id 10)", error);
        Assertions.assertEquals(0, repositoryRepository.count().getValue0(), "nothing inserted");
    }

    @Test
    public void testSaveDup() {
        User user = Factories.createUser();
        List<Repository> repositories = new ArrayList<>();
        Repository original = Factories.buildRepository();
        repositories.add(original);
        Repository dup = Factories.buildRepository();
        dup.setGithubId(original.getGithubId());
        repositories.add(dup);

        RepositoryRepository repositoryRepository = ControllerUtils.getRepository(RepositoryRepository.class);

        String error = repositoryRepository.save(user, repositories);
        Assertions.assertTrue(error.contains("duplicate key value violates unique constraint \"repositories_user_id_github_id\""), "dup error");
        Assertions.assertNull(original.getId(), "not inserted");
        Assertions.assertNull(dup.getId(), "not inserted");

        Pair<Long,String> countResult = repositoryRepository.count();
        Assertions.assertEquals(0L, countResult.getValue0(), "nothing inserted");
    }

    @Test
    public void testNullConstraint() {
        User user = Factories.createUser();
        List<Repository> repositories = new ArrayList<>();
        repositories.add(Factories.buildRepository());
        Repository badRepo = Factories.buildRepository();
        badRepo.setName(null);
        repositories.add(badRepo);

        RepositoryRepository repositoryRepository = ControllerUtils.getRepository(RepositoryRepository.class);

        String error = repositoryRepository.save(user, repositories);
        Assertions.assertTrue(error.contains("null value in column \"name\""), "null name check");

        Pair<Long,String> countResult = repositoryRepository.count();
        Assertions.assertEquals(0L, countResult.getValue0(), "nothing inserted");
    }
    
}
