package com.mikedll.headshot.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.mikedll.headshot.db.QuietResultSet;

public class Repository {
    
    private Long id;

    private Long githubId;

    private Long userId;
    
    private String name;

    private boolean isPrivate;

    private String description;

    private Instant githubCreatedAt;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public void setGithubId(Long id) {
        this.githubId = id;
    }

    public Long getGithubId() {
        return this.githubId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return this.userId;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getIsPrivate() {
        return this.isPrivate;
    }

    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setGithubCreatedAt(Instant githubCreatedAt) {
        this.githubCreatedAt = githubCreatedAt.truncatedTo(ChronoUnit.MILLIS);
    }

    public Instant getGithubCreatedAt() {
        return this.githubCreatedAt;
    }

    public String prettyCreatedAt() {
        return Formats.PRETTY_TIME_FORMATTER.format(this.githubCreatedAt);
    }

    public void copyFromRs(QuietResultSet rs) {
        setId(rs.getLong("id"));
        setUserId(rs.getLong("user_id"));
        setGithubId(rs.getLong("github_id"));
        setName(rs.getString("name"));
        setIsPrivate(rs.getBoolean("is_private"));
        setDescription(rs.getString("description"));
        setGithubCreatedAt(Instant.ofEpochMilli(rs.getTimestamp("github_created_at").getTime()));
    }
}
