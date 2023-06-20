package com.mikedll.headshot;

public class Config {

    public GithubConfig githubConfig;

    public String cookieSigningKey;

    public String dbUrl;

    public String env;

    public int poolSize = 10;

    public boolean shouldLog() {
        return this.env != "test";
    }
}
