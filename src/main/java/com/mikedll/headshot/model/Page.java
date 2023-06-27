package com.mikedll.headshot.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Page {

    private Long id;

    private Long tourId;

    private Instant createdAt;
    
    private String filename;

    private Integer lineNumber;

    private String language;

    private String content;

    private String narration;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getTourId() {
        return this.tourId;
    }

    public void setTourId(Long tourId) {
        this.tourId = tourId;
    }    

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt.truncatedTo(ChronoUnit.MILLIS);
    }    

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public Integer getLineNumber() {
        return this.lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNarration() {
        return this.narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }    
}
