package com.aiworkflow.workmanagement.orchestration.service.impl;

import com.aiworkflow.workmanagement.domain.model.WorkspaceConfig;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.orchestration.domain.LlmUsageRecord;
import com.aiworkflow.workmanagement.orchestration.domain.ProjectTokenUsageSummary;
import com.aiworkflow.workmanagement.orchestration.service.ProjectTokenUsageService;
import com.aiworkflow.workmanagement.orchestration.service.StoryTokenUsageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FileBasedProjectTokenUsageServiceTest {

    @Test
    void aggregatesUsageAcrossStories(@TempDir Path tempDir) {
        WorkspaceConfig workspaceConfig = new WorkspaceConfig(tempDir, "test-workspace");
        StoryTokenUsageService storyUsageService = new FileBasedStoryTokenUsageService(workspaceConfig);
        ProjectTokenUsageService projectUsageService = new FileBasedProjectTokenUsageService(workspaceConfig);

        storyUsageService.recordUsage(new LlmUsageRecord(
            new StoryId("STORY-100").getValue(),
            "exec-1",
            "anthropic",
            "claude",
            100,
            200,
            300,
            120,
            false,
            Instant.now(),
            Map.of("raw", "exact")
        ));

        storyUsageService.recordUsage(new LlmUsageRecord(
            new StoryId("STORY-200").getValue(),
            "exec-2",
            "gemini",
            "gemini-2.0-flash",
            40,
            60,
            100,
            90,
            true,
            Instant.now(),
            Map.of("raw", "estimated")
        ));

        storyUsageService.recordUsage(new LlmUsageRecord(
            new StoryId("STORY-200").getValue(),
            "exec-3",
            "anthropic",
            "claude",
            10,
            40,
            50,
            80,
            false,
            Instant.now(),
            Map.of()
        ));

        ProjectTokenUsageSummary summary = projectUsageService.getProjectSummary(true);

        assertThat(summary.isIncludeInProgress()).isTrue();
        assertThat(summary.getStoryCount()).isEqualTo(2);
        assertThat(summary.getRecordCount()).isEqualTo(3);
        assertThat(summary.getExactTotals().getTotalTokens()).isEqualTo(350);
        assertThat(summary.getEstimatedTotals().getTotalTokens()).isEqualTo(100);
        assertThat(summary.getBreakdown()).hasSize(2);
    }

    @Test
    void returnsEmptySummaryWhenNoStories(@TempDir Path tempDir) {
        WorkspaceConfig workspaceConfig = new WorkspaceConfig(tempDir, "test-workspace");
        ProjectTokenUsageService projectUsageService = new FileBasedProjectTokenUsageService(workspaceConfig);

        ProjectTokenUsageSummary summary = projectUsageService.getProjectSummary(false);

        assertThat(summary.getStoryCount()).isZero();
        assertThat(summary.getRecordCount()).isZero();
        assertThat(summary.getExactTotals().getTotalTokens()).isZero();
        assertThat(summary.getEstimatedTotals().getTotalTokens()).isZero();
        assertThat(summary.getBreakdown()).isEmpty();
        assertThat(summary.isIncludeInProgress()).isFalse();
    }
}

