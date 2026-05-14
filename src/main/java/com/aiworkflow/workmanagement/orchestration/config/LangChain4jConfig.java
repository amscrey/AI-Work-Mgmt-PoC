package com.aiworkflow.workmanagement.orchestration.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j Spring configuration skeleton used by STORY-009.
 * This provides a placeholder bean for ChatLanguageModel while keeping the build independent
 * of LangChain4j provider libraries. During real implementation replace Object with the
 * actual ChatLanguageModel type from LangChain4j and wire provider clients.
 */
@Configuration
@ConditionalOnProperty(prefix = "orchestration", name = "execution-mode", havingValue = "llm")
public class LangChain4jConfig {

    @Bean
    public Object chatLanguageModel() {
        // Placeholder bean - replace with real ChatLanguageModel implementation
        return new Object();
    }
}

