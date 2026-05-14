package com.aiworkflow.workmanagement.api.dto;

import com.aiworkflow.workmanagement.orchestration.domain.TokenTotals;

public class TokenTotalsResponse {
    private final long promptTokens;
    private final long completionTokens;
    private final long totalTokens;

    public TokenTotalsResponse(long promptTokens, long completionTokens, long totalTokens) {
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
    }

    public static TokenTotalsResponse from(TokenTotals totals) {
        return new TokenTotalsResponse(
            totals.getPromptTokens(),
            totals.getCompletionTokens(),
            totals.getTotalTokens()
        );
    }

    public long getPromptTokens() {
        return promptTokens;
    }

    public long getCompletionTokens() {
        return completionTokens;
    }

    public long getTotalTokens() {
        return totalTokens;
    }
}

