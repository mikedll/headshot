package com.mikedll.headshot.model;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {

    public User findByGithubId(Long githubId);

}
