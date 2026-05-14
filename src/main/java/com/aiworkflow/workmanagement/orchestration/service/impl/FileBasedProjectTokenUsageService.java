package com.aiworkflow.workmanagement.orchestration.service.impl;

import com.aiworkflow.workmanagement.domain.model.WorkspaceConfig;
import com.aiworkflow.workmanagement.orchestration.domain.LlmUsageRecord;
import com.aiworkflow.workmanagement.orchestration.domain.ProjectTokenUsageSummary;
import com.aiworkflow.workmanagement.orchestration.domain.ProviderModelUsage;
import com.aiworkflow.workmanagement.orchestration.domain.TokenTotals;
import com.aiworkflow.workmanagement.orchestration.service.ProjectTokenUsageService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class FileBasedProjectTokenUsageService implements ProjectTokenUsageService {

    private static final String STORIES_DIR = "stories";
    private static final String USAGE_DIR = "usage";
    private static final String USAGE_FILE = "llm-usage.jsonl";

    private final WorkspaceConfig workspaceConfig;
    private final ObjectMapper lineObjectMapper;

    public FileBasedProjectTokenUsageService(WorkspaceConfig workspaceConfig) {
        this.workspaceConfig = workspaceConfig;
        this.lineObjectMapper = new ObjectMapper();
        this.lineObjectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public ProjectTokenUsageSummary getProjectSummary(boolean includeInProgress) {
        List<Path> storyDirs = listStoryDirectories();
        if (storyDirs.isEmpty()) {
            return emptySummary(includeInProgress);
        }

        TokenTotals exactTotals = new TokenTotals();
        TokenTotals estimatedTotals = new TokenTotals();
        Map<String, ProviderModelUsage> usageByProvider = new LinkedHashMap<>();
        int storyCount = 0;
        int recordCount = 0;

        for (Path storyDir : storyDirs) {
            Path usagePath = storyDir.resolve(USAGE_DIR).resolve(USAGE_FILE);
            if (!Files.exists(usagePath)) {
                continue;
            }

            List<LlmUsageRecord> records = readUsageRecords(usagePath);
            if (records.isEmpty()) {
                continue;
            }

            storyCount++;
            for (LlmUsageRecord record : records) {
                recordCount++;
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
        }

        return new ProjectTokenUsageSummary(
            includeInProgress,
            storyCount,
            recordCount,
            exactTotals,
            estimatedTotals,
            new ArrayList<>(usageByProvider.values())
        );
    }

    private List<Path> listStoryDirectories() {
        List<Path> storyDirs = new ArrayList<>();
        Path workspaceRoot = workspaceConfig.getWorkspaceRoot();
        for (String dir : new String[]{"backlog", "prioritized", "done"}) {
            Path base = workspaceRoot.resolve(dir);
            if (Files.exists(base) && Files.isDirectory(base)) {
                try {
                    storyDirs.addAll(Files.list(base).filter(Files::isDirectory).collect(Collectors.toList()));
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to list stories in " + base, e);
                }
            }
        }
        return storyDirs;
    }

    private List<LlmUsageRecord> readUsageRecords(Path usagePath) {
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

    private ProjectTokenUsageSummary emptySummary(boolean includeInProgress) {
        return new ProjectTokenUsageSummary(
            includeInProgress,
            0,
            0,
            new TokenTotals(),
            new TokenTotals(),
            List.of()
        );
    }
}
