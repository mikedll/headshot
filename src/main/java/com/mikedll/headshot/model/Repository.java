package com.mikedll.headshot.model;

import java.time.Instant;

import com.mikedll.headshot.db.QuietResultSet;

public class Repository {
    
    private Long id;

    private Long githubId;

    private Long userId;
    
    private String name;

    private boolean isPrivate;

    private String description;

    private Instant createdAt;

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

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public String prettyCreatedAt() {
        return Formats.PRETTY_TIME_FORMATTER.format(this.createdAt);
    }

    public void copyFromRs(QuietResultSet rs) {
        setId(rs.getLong("id"));
        setUserId(rs.getLong("user_id"));
        setGithubId(rs.getLong("github_id"));
        setName(rs.getString("name"));
        setIsPrivate(rs.getBoolean("is_private"));
        setDescription(rs.getString("description"));
        setCreatedAt(Instant.ofEpochMilli(rs.getTimestamp("github_created_at").getTime()));
    }
}
