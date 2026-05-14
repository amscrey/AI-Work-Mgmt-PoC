package com.aiworkflow.workmanagement.api;

import com.aiworkflow.workmanagement.api.dto.OrchestrationRunRequest;
import com.aiworkflow.workmanagement.application.service.CommentService;
import com.aiworkflow.workmanagement.orchestration.domain.ExecutionOutcome;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationRequest;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationResult;
import com.aiworkflow.workmanagement.orchestration.domain.StoryTokenUsageSummary;
import com.aiworkflow.workmanagement.orchestration.domain.TokenTotals;
import com.aiworkflow.workmanagement.orchestration.service.OrchestrationService;
import com.aiworkflow.workmanagement.orchestration.service.StoryTokenUsageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrchestrationController.class)
class OrchestrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrchestrationService orchestrationService;

    @MockBean
    private OrchestrationRunRegistry runRegistry;

    @MockBean
    private StoryTokenUsageService tokenUsageService;

    @MockBean
    private CommentService commentService;

    @Test
    void startsRunAndReturnsExecutionId() throws Exception {
        OrchestrationResult result = OrchestrationResult.success("exec-1", List.of(), "ok");
        when(orchestrationService.orchestrate(any(OrchestrationRequest.class))).thenReturn(result);
        when(runRegistry.record(any(), any(OrchestrationResult.class))).thenReturn(new OrchestrationRunRecord("STORY-1", result));

        OrchestrationRunRequest request = new OrchestrationRunRequest();
        request.setStoryId("STORY-1");
        request.setWorkIntent("Research");
        request.setRequestedBy("agent");

        mockMvc.perform(post("/api/v1/orchestration/runs")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.executionId").value("exec-1"))
            .andExpect(jsonPath("$.assistantResponse.summary").isNotEmpty());
    }

    @Test
    void startsPromptRun() throws Exception {
        OrchestrationResult result = OrchestrationResult.success("exec-2", List.of(), "ok");
        when(orchestrationService.orchestrate(any(OrchestrationRequest.class))).thenReturn(result);
        when(runRegistry.record(any(), any(OrchestrationResult.class))).thenReturn(new OrchestrationRunRecord("STORY-2", result));

        mockMvc.perform(post("/api/v1/orchestration/prompt")
                .contentType(MediaType.TEXT_PLAIN)
                .param("requestedBy", "human")
                .param("storyId", "STORY-2")
                .content("Generate a plan"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.executionId").value("exec-2"))
            .andExpect(jsonPath("$.assistantResponse.summary").isNotEmpty());
    }

    @Test
    void returnsStoryUsageSummary() throws Exception {
        StoryTokenUsageSummary summary = new StoryTokenUsageSummary("STORY-1", new TokenTotals(), new TokenTotals(), List.of());
        when(tokenUsageService.getSummary(any())).thenReturn(summary);

        mockMvc.perform(get("/api/v1/stories/STORY-1/usage"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.storyId").value("STORY-1"));
    }
}
