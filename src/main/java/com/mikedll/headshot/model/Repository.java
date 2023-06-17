package com.mikedll.headshot.model;

import java.time.Instant;

import com.mikedll.headshot.db.SimpleSql;

public class Repository {
    
    private Long id;
    
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

    public void copyFromRs(SimpleSql rs) {
        setId(rs.getLong("id"));
        setName(rs.getString("name"));
        setIsPrivate(rs.getBoolean("is_private"));
        setDescription(rs.getString("description"));
        setCreatedAt(Instant.ofEpochMilli(rs.getTimestamp("created_at").getTime()));
    }
}
