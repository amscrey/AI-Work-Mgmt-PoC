package com.aiworkflow.workmanagement.api.dto;

import com.aiworkflow.workmanagement.orchestration.domain.ArtifactReference;

import java.time.Instant;

public class ArtifactReferenceResponse {
    private final String name;
    private final String createdBy;
    private final Instant createdAt;
    private final long contentSize;

    public ArtifactReferenceResponse(String name, String createdBy, Instant createdAt, long contentSize) {
        this.name = name;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.contentSize = contentSize;
    }

    public static ArtifactReferenceResponse from(ArtifactReference reference) {
        return new ArtifactReferenceResponse(
            reference.getName(),
            reference.getCreatedBy(),
            reference.getCreatedAt(),
            reference.getContentSize()
        );
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

    public long getContentSize() {
        return contentSize;
    }
}

