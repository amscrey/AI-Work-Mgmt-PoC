package com.aiworkflow.workmanagement.orchestration.llm;

import com.aiworkflow.workmanagement.orchestration.domain.LlmConfigSnapshot;
import com.aiworkflow.workmanagement.orchestration.domain.LlmSummary;
import com.aiworkflow.workmanagement.orchestration.service.LlmConfigLoader;
import com.aiworkflow.workmanagement.orchestration.service.LlmSummaryService;
import org.springframework.stereotype.Service;

/**
 * Default runtime summary service for LLM configuration.
 */
@Service
public class DefaultLlmSummaryService implements LlmSummaryService {

    private final LlmConfigLoader configLoader;

    public DefaultLlmSummaryService(LlmConfigLoader configLoader) {
        this.configLoader = configLoader;
    }

    @Override
    public LlmSummary getSummary() {
        LlmConfigSnapshot snapshot = configLoader.load();
        return new LlmSummary(
            snapshot.getDefaultProvider(),
            snapshot.getProviders(),
            snapshot.getRoleMappings(),
            snapshot.getDiagnostics()
        );
    }
}

