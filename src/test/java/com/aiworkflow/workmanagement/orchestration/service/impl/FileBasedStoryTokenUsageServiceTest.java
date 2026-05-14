package com.aiworkflow.workmanagement.orchestration.service.impl;

import com.aiworkflow.workmanagement.domain.model.WorkspaceConfig;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.orchestration.domain.LlmUsageRecord;
import com.aiworkflow.workmanagement.orchestration.domain.StoryTokenUsageSummary;
import com.aiworkflow.workmanagement.orchestration.service.StoryTokenUsageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileBasedStoryTokenUsageServiceTest {

    @Test
    void recordsUsageAndBuildsSummary(@TempDir Path tempDir) throws Exception {
        WorkspaceConfig workspaceConfig = new WorkspaceConfig(tempDir, "test-workspace");
        StoryTokenUsageService service = new FileBasedStoryTokenUsageService(workspaceConfig);
        StoryId storyId = new StoryId("STORY-015");

        service.recordUsage(new LlmUsageRecord(
            storyId.getValue(),
            "exec-1",
            "anthropic",
            "claude",
            100,
            200,
            300,
            150,
            false,
            Instant.now(),
            Map.of("raw", "value")
        ));

        service.recordUsage(new LlmUsageRecord(
            storyId.getValue(),
            "exec-2",
            "gemini",
            "gemini-2.0-flash",
            50,
            50,
            100,
            120,
            true,
            Instant.now(),
            Map.of("raw", "estimated")
        ));

        StoryTokenUsageSummary summary = service.getSummary(storyId);

        assertThat(summary.getExactTotals().getTotalTokens()).isEqualTo(300);
        assertThat(summary.getEstimatedTotals().getTotalTokens()).isEqualTo(100);
        assertThat(summary.getBreakdown()).hasSize(2);

        Path usageDir = tempDir.resolve("prioritized").resolve(storyId.getValue()).resolve("usage");
        assertThat(Files.exists(usageDir.resolve("llm-usage-summary.json"))).isTrue();

        List<LlmUsageRecord> records = service.getRecords(storyId);
        assertThat(records).hasSize(2);
        assertThat(records.get(0).getRawMetadata()).containsEntry("raw", "value");
    }

    @Test
    void throwsWhenJsonlLineIsCorrupt(@TempDir Path tempDir) throws Exception {
        WorkspaceConfig workspaceConfig = new WorkspaceConfig(tempDir, "test-workspace");
        StoryTokenUsageService service = new FileBasedStoryTokenUsageService(workspaceConfig);
        StoryId storyId = new StoryId("STORY-016");

        service.recordUsage(new LlmUsageRecord(
            storyId.getValue(),
            "exec-1",
            "anthropic",
            "claude",
            10,
            20,
            30,
            50,
            false,
            Instant.now(),
            Map.of()
        ));

        Path usageFile = tempDir.resolve("prioritized").resolve(storyId.getValue()).resolve("usage").resolve("llm-usage.jsonl");
        Files.writeString(usageFile, "{", StandardOpenOption.APPEND);

        assertThatThrownBy(() -> service.getRecords(storyId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Failed to parse usage record");
    }
}
