package com.aiworkflow.workmanagement.orchestration.domain;

/**
 * Token totals for prompt/completion/total counts.
 */
public class TokenTotals {
    private long promptTokens;
    private long completionTokens;
    private long totalTokens;

    public TokenTotals() {
        this(0, 0, 0);
    }

    public TokenTotals(long promptTokens, long completionTokens, long totalTokens) {
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
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

    public void add(TokenTotals other) {
        if (other == null) {
            return;
        }
        this.promptTokens += other.promptTokens;
        this.completionTokens += other.completionTokens;
        this.totalTokens += other.totalTokens;
    }
}

