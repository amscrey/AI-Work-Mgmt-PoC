package com.aiworkflow.workmanagement.orchestration.node.llm;

import com.aiworkflow.workmanagement.orchestration.domain.OrchestrationState;
import com.aiworkflow.workmanagement.orchestration.llm.LLMChatClient;
import com.aiworkflow.workmanagement.orchestration.service.PromptTemplateService;
import com.aiworkflow.workmanagement.orchestration.service.StoryTokenUsageService;
import com.aiworkflow.workmanagement.orchestration.service.TokenEstimationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "orchestration.execution-mode", havingValue = "llm")
public class TestStrategyLLMNode extends AbstractLLMNodeAction {

    public TestStrategyLLMNode(
        PromptTemplateService promptTemplateService,
        LLMChatClient chatClient,
        StoryTokenUsageService tokenUsageService,
        TokenEstimationService tokenEstimationService
    ) {
        super(promptTemplateService, chatClient, tokenUsageService, tokenEstimationService);
    }

    @Override
    protected String buildPrompt(PromptTemplateService promptTemplateService, OrchestrationState state) {
        return promptTemplateService.buildTestStrategyPrompt(state.toContext());
    }

    @Override
    protected String getArtifactName() {
        return "test-strategy";
    }

    @Override
    protected String getRoleName() {
        return "tester";
    }
}
