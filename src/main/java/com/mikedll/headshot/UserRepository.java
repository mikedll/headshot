package com.mikedll.headshot;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {

    public User findByGithubId(Long githubId);

}
