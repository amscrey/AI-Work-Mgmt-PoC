package com.aiworkflow.workmanagement.orchestration.domain;

import java.util.Map;

/**
 * Usage block text plus structured metrics for logging.
 */
public class UsageBlock {
    private final String text;
    private final Map<String, Object> metrics;

    public UsageBlock(String text, Map<String, Object> metrics) {
        this.text = text;
        this.metrics = metrics;
    }

    public String getText() {
        return text;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }
}

