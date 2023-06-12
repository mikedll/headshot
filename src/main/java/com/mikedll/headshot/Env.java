package com.mikedll.headshot;

public class Env {

    public static GithubConfig githubConfig;

    public static String cookieSigningKey;

    public static String dbUrl;

    public static String env;

    public static int poolSize = 10;

    public static boolean shouldLog() {
        return Env.env != "test";
    }
}
