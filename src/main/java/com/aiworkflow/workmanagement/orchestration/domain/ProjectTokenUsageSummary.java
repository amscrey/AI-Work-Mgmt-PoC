package com.aiworkflow.workmanagement.orchestration.domain;

import java.util.List;

/**
 * Summary of token usage across all stories in a workspace.
 */
public class ProjectTokenUsageSummary {
    private final boolean includeInProgress;
    private final int storyCount;
    private final int recordCount;
    private final TokenTotals exactTotals;
    private final TokenTotals estimatedTotals;
    private final List<ProviderModelUsage> breakdown;

    public ProjectTokenUsageSummary(
        boolean includeInProgress,
        int storyCount,
        int recordCount,
        TokenTotals exactTotals,
        TokenTotals estimatedTotals,
        List<ProviderModelUsage> breakdown
    ) {
        this.includeInProgress = includeInProgress;
        this.storyCount = storyCount;
        this.recordCount = recordCount;
        this.exactTotals = exactTotals;
        this.estimatedTotals = estimatedTotals;
        this.breakdown = breakdown;
    }

    public boolean isIncludeInProgress() {
        return includeInProgress;
    }

    public int getStoryCount() {
        return storyCount;
    }

    public int getRecordCount() {
        return recordCount;
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

