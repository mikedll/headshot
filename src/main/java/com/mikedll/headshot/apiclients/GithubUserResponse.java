package com.mikedll.headshot.apiclients;

import com.mikedll.headshot.model.User;

public record GithubUserResponse(Long id, String login, String name, String url, String html_url, String repos_url) {
    
    public void copyFieldsTo(User user) {
        user.setName(this.name);
        user.setGithubId(this.id);
        user.setGithubLogin(this.login);
        user.setUrl(this.url);
        user.setHtmlUrl(this.html_url);
        user.setReposUrl(this.repos_url);
    }
}
