package com.aiworkflow.workmanagement.api.dto;

import com.aiworkflow.workmanagement.orchestration.domain.StoryTokenUsageSummary;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class StoryUsageSummaryResponse {
    private final String storyId;
    private final TokenTotalsResponse exactTotals;
    private final TokenTotalsResponse estimatedTotals;
    private final List<ProviderModelUsageResponse> breakdown;
    private final Instant updatedAt;

    public StoryUsageSummaryResponse(
        String storyId,
        TokenTotalsResponse exactTotals,
        TokenTotalsResponse estimatedTotals,
        List<ProviderModelUsageResponse> breakdown,
        Instant updatedAt
    ) {
        this.storyId = storyId;
        this.exactTotals = exactTotals;
        this.estimatedTotals = estimatedTotals;
        this.breakdown = breakdown;
        this.updatedAt = updatedAt;
    }

    public static StoryUsageSummaryResponse from(StoryTokenUsageSummary summary) {
        return new StoryUsageSummaryResponse(
            summary.getStoryId(),
            TokenTotalsResponse.from(summary.getExactTotals()),
            TokenTotalsResponse.from(summary.getEstimatedTotals()),
            summary.getBreakdown().stream().map(ProviderModelUsageResponse::from).collect(Collectors.toList()),
            Instant.now()
        );
    }

    public String getStoryId() {
        return storyId;
    }

    public TokenTotalsResponse getExactTotals() {
        return exactTotals;
    }

    public TokenTotalsResponse getEstimatedTotals() {
        return estimatedTotals;
    }

    public List<ProviderModelUsageResponse> getBreakdown() {
        return breakdown;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

