package com.aiworkflow.workmanagement.api.dto;

import java.time.Instant;

public class AssistantReference {
    private final String type;
    private final String storyId;
    private final String id;
    private final String name;
    private final String createdBy;
    private final Instant createdAt;

    public AssistantReference(
        String type,
        String storyId,
        String id,
        String name,
        String createdBy,
        Instant createdAt
    ) {
        this.type = type;
        this.storyId = storyId;
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public String getType() {
        return type;
    }

    public String getStoryId() {
        return storyId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

