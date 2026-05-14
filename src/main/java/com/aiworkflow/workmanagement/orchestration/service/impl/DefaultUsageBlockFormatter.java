package com.aiworkflow.workmanagement.orchestration.service.impl;

import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.orchestration.domain.LlmUsageRecord;
import com.aiworkflow.workmanagement.orchestration.domain.ProviderModelUsage;
import com.aiworkflow.workmanagement.orchestration.domain.StoryTokenUsageSummary;
import com.aiworkflow.workmanagement.orchestration.domain.UsageBlock;
import com.aiworkflow.workmanagement.orchestration.service.StoryTokenUsageService;
import com.aiworkflow.workmanagement.orchestration.service.UsageBlockFormatter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DefaultUsageBlockFormatter implements UsageBlockFormatter {

    private final StoryTokenUsageService tokenUsageService;

    public DefaultUsageBlockFormatter(StoryTokenUsageService tokenUsageService) {
        this.tokenUsageService = tokenUsageService;
    }

    @Override
    public UsageBlock format(StoryId storyId) {
        StoryTokenUsageSummary summary = tokenUsageService.getSummary(storyId);
        List<LlmUsageRecord> records = tokenUsageService.getRecords(storyId);
        long maxContextTokens = records.stream()
            .map(record -> record.getRawMetadata() != null ? record.getRawMetadata().get("contextTokens") : null)
            .filter(value -> value instanceof Number)
            .mapToLong(value -> ((Number) value).longValue())
            .max()
            .orElse(0L);

        StringBuilder text = new StringBuilder();
        text.append("---\n");
        text.append("Token usage (story ").append(summary.getStoryId()).append(")\n");
        text.append("Exact total: ").append(summary.getExactTotals().getTotalTokens())
            .append(" (prompt ").append(summary.getExactTotals().getPromptTokens())
            .append(", completion ").append(summary.getExactTotals().getCompletionTokens()).append(")\n");
        text.append("Estimated total: ").append(summary.getEstimatedTotals().getTotalTokens())
            .append(" (prompt ").append(summary.getEstimatedTotals().getPromptTokens())
            .append(", completion ").append(summary.getEstimatedTotals().getCompletionTokens()).append(")\n");
        if (!summary.getBreakdown().isEmpty()) {
            text.append("Per provider/model:\n");
            for (ProviderModelUsage usage : summary.getBreakdown()) {
                text.append("- ").append(usage.getProvider()).append("/").append(usage.getModel())
                    .append(": exact ").append(usage.getExactTotals().getTotalTokens())
                    .append(", estimated ").append(usage.getEstimatedTotals().getTotalTokens()).append("\n");
            }
        }
        text.append("Max context tokens (observed): ").append(maxContextTokens).append("\n");
        text.append("---");

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("usage_exact_total", summary.getExactTotals().getTotalTokens());
        metrics.put("usage_estimated_total", summary.getEstimatedTotals().getTotalTokens());
        metrics.put("usage_context_tokens_max", maxContextTokens);

        return new UsageBlock(text.toString(), metrics);
    }
}

