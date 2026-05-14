package com.aiworkflow.workmanagement.orchestration.domain;

import java.util.List;

/**
 * Summary of token usage for a story.
 */
public class StoryTokenUsageSummary {
    private final String storyId;
    private final TokenTotals exactTotals;
    private final TokenTotals estimatedTotals;
    private final List<ProviderModelUsage> breakdown;

    public StoryTokenUsageSummary(
        String storyId,
        TokenTotals exactTotals,
        TokenTotals estimatedTotals,
        List<ProviderModelUsage> breakdown
    ) {
        this.storyId = storyId;
        this.exactTotals = exactTotals;
        this.estimatedTotals = estimatedTotals;
        this.breakdown = breakdown;
    }

    public String getStoryId() {
        return storyId;
    }

    public TokenTotals getExactTotals() {
        return exactTotals;
    }

    public TokenTotals getEstimatedTotals() {
        return estimatedTotals;
    }

    public List<ProviderModelUsage> getBreakdown() {
        return breakdown;
    }
}

