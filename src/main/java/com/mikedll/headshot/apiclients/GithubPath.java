package com.mikedll.headshot.apiclients;

import java.util.List;

public record GithubPath(String path, boolean isFile, List<GithubFile> files) {}
