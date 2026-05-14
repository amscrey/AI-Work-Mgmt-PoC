package com.aiworkflow.workmanagement.orchestration.node.llm;

import com.aiworkflow.workmanagement.orchestration.domain.ArtifactReference;
import com.aiworkflow.workmanagement.orchestration.domain.LlmChatResponse;
import com.aiworkflow.workmanagement.orchestration.domain.LlmUsage;
import com.aiworkflow.workmanagement.orchestration.domain.LlmUsageRecord;
import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationState;
import com.aiworkflow.workmanagement.orchestration.llm.LLMChatClient;
import com.aiworkflow.workmanagement.orchestration.node.NodeAction;
import com.aiworkflow.workmanagement.orchestration.service.PromptTemplateService;
import com.aiworkflow.workmanagement.orchestration.service.StoryTokenUsageService;
import com.aiworkflow.workmanagement.orchestration.service.TokenEstimationService;

/**
 * Base class for LLM-backed node actions.
 */
public abstract class AbstractLLMNodeAction implements NodeAction {

    private final PromptTemplateService promptTemplateService;
    private final LLMChatClient chatClient;
    private final StoryTokenUsageService tokenUsageService;
    private final TokenEstimationService tokenEstimationService;

    protected AbstractLLMNodeAction(
        PromptTemplateService promptTemplateService,
        LLMChatClient chatClient,
        StoryTokenUsageService tokenUsageService,
        TokenEstimationService tokenEstimationService
    ) {
        this.promptTemplateService = promptTemplateService;
        this.chatClient = chatClient;
        this.tokenUsageService = tokenUsageService;
        this.tokenEstimationService = tokenEstimationService;
    }

    protected AbstractLLMNodeAction(PromptTemplateService promptTemplateService, LLMChatClient chatClient, StoryTokenUsageService tokenUsageService) {
        this(promptTemplateService, chatClient, tokenUsageService, null);
    }

    protected AbstractLLMNodeAction(PromptTemplateService promptTemplateService, LLMChatClient chatClient) {
        this(promptTemplateService, chatClient, null, null);
    }

    @Override
    public OrchestrationState execute(OrchestrationState state) {
        String prompt = buildPrompt(promptTemplateService, state);
        LlmChatResponse response = chatClient.generate(getRoleName(), prompt);
        String content = response != null ? parseResponse(response.getText()) : "";

        recordUsageIfAvailable(state, response, prompt);

        ArtifactReference artifact = ArtifactReference.builder()
            .name(getArtifactName())
            .content(content)
            .createdBy(getRoleName())
            .build();

        state.addArtifact(artifact);
        state.setCurrentRole(getRoleName());
        return state;
    }

    private void recordUsageIfAvailable(OrchestrationState state, LlmChatResponse response, String prompt) {
        if (tokenUsageService == null || response == null) {
            return;
        }
        if (state.getStory() == null) {
            return;
        }
        LlmUsage usage = response.getUsage();
        if (usage == null && tokenEstimationService != null) {
            usage = tokenEstimationService.estimate(response.getProvider(), response.getModel(), prompt, response.getText());
        }
        if (usage == null) {
            return;
        }
        LlmUsageRecord record = new LlmUsageRecord(
            state.getStory().getId().getValue(),
            state.getExecutionId(),
            response.getProvider(),
            response.getModel(),
            usage.getPromptTokens(),
            usage.getCompletionTokens(),
            usage.getTotalTokens(),
            usage.getLatencyMs(),
            usage.isEstimated(),
            java.time.Instant.now(),
            usage.getRawMetadata()
        );
        tokenUsageService.recordUsage(record);
    }

    protected abstract String buildPrompt(PromptTemplateService promptTemplateService, OrchestrationState state);

    protected abstract String getArtifactName();

    protected abstract String getRoleName();

    protected String parseResponse(String response) {
        return response != null ? response : "";
    }
}
