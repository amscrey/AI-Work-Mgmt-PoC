package com.aiworkflow.workmanagement.orchestration.service.impl;

import com.aiworkflow.workmanagement.domain.model.WorkspaceConfig;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.orchestration.domain.LlmUsageRecord;
import com.aiworkflow.workmanagement.orchestration.domain.ProviderModelUsage;
import com.aiworkflow.workmanagement.orchestration.domain.StoryTokenUsageSummary;
import com.aiworkflow.workmanagement.orchestration.domain.TokenTotals;
import com.aiworkflow.workmanagement.orchestration.service.StoryTokenUsageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FileBasedStoryTokenUsageService implements StoryTokenUsageService {

    private static final String USAGE_DIR = "usage";
    private static final String USAGE_FILE = "llm-usage.jsonl";
    private static final String SUMMARY_FILE = "llm-usage-summary.json";

    private final WorkspaceConfig workspaceConfig;
    private final ObjectMapper objectMapper;
    private final ObjectMapper lineObjectMapper;

    public FileBasedStoryTokenUsageService(WorkspaceConfig workspaceConfig) {
        this.workspaceConfig = workspaceConfig;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        this.lineObjectMapper = new ObjectMapper();
        this.lineObjectMapper.registerModule(new JavaTimeModule());
        this.lineObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void recordUsage(LlmUsageRecord record) {
        if (record == null || record.getStoryId() == null) {
            throw new IllegalArgumentException("Usage record and storyId are required");
        }

        Path usageDir = getUsageDir(new StoryId(record.getStoryId()));
        try {
            Files.createDirectories(usageDir);
            Path usagePath = usageDir.resolve(USAGE_FILE);
            String line = lineObjectMapper.writeValueAsString(record) + System.lineSeparator();
            Files.writeString(usagePath, line, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);

            StoryTokenUsageSummary summary = buildSummary(record.getStoryId(), getRecords(new StoryId(record.getStoryId())));
            objectMapper.writeValue(usageDir.resolve(SUMMARY_FILE).toFile(), summary);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write usage record", e);
        }
    }

    @Override
    public List<LlmUsageRecord> getRecords(StoryId storyId) {
        Path usagePath = getUsageDir(storyId).resolve(USAGE_FILE);
        if (!Files.exists(usagePath)) {
            return List.of();
        }
        try {
            return Files.readAllLines(usagePath).stream()
                .filter(line -> !line.isBlank())
                .map(line -> {
                    try {
                        return lineObjectMapper.readValue(line, LlmUsageRecord.class);
                    } catch (IOException e) {
                        throw new IllegalStateException("Failed to parse usage record", e);
                    }
                })
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read usage records", e);
        }
    }

    @Override
    public StoryTokenUsageSummary getSummary(StoryId storyId) {
        List<LlmUsageRecord> records = getRecords(storyId);
        return buildSummary(storyId.getValue(), records);
    }

    private StoryTokenUsageSummary buildSummary(String storyId, List<LlmUsageRecord> records) {
        TokenTotals exactTotals = new TokenTotals();
        TokenTotals estimatedTotals = new TokenTotals();
        Map<String, ProviderModelUsage> usageByProvider = new LinkedHashMap<>();

        for (LlmUsageRecord record : records) {
            TokenTotals recordTotals = new TokenTotals(
                record.getPromptTokens(),
                record.getCompletionTokens(),
                record.getTotalTokens()
            );

            String provider = record.getProvider() != null ? record.getProvider() : "unknown";
            String model = record.getModel() != null ? record.getModel() : "unknown";
            String key = provider + "::" + model;

            ProviderModelUsage usage = usageByProvider.computeIfAbsent(
                key,
                k -> new ProviderModelUsage(provider, model)
            );

            if (record.isEstimated()) {
                estimatedTotals.add(recordTotals);
                usage.getEstimatedTotals().add(recordTotals);
            } else {
                exactTotals.add(recordTotals);
                usage.getExactTotals().add(recordTotals);
            }
        }

        List<ProviderModelUsage> breakdown = new ArrayList<>(usageByProvider.values());
        return new StoryTokenUsageSummary(storyId, exactTotals, estimatedTotals, breakdown);
    }

    private Path getUsageDir(StoryId storyId) {
        Path workspaceRoot = workspaceConfig.getWorkspaceRoot();
        for (String dir : new String[]{"prioritized", "backlog", "done"}) {
            Path storyDir = workspaceRoot.resolve(dir).resolve(storyId.getValue());
            if (Files.exists(storyDir)) {
                return storyDir.resolve(USAGE_DIR);
            }
        }
        return workspaceRoot.resolve("prioritized").resolve(storyId.getValue()).resolve(USAGE_DIR);
    }
}
