package com.aiworkflow.workmanagement.orchestration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for the orchestration layer.
 * <p>
 * This configuration supports two execution modes:
 * <ul>
 *   <li><strong>template</strong>: Template-based nodes (deterministic, no LLM calls)</li>
 *   <li><strong>llm</strong>: LLM nodes (real Claude API calls via LangChain4j)</li>
 * </ul>
 * <p>
 * Both modes use the same {@code NodeAction} interface and are completely interchangeable.
 * The mode determines which implementation beans are loaded via conditional configuration.
 *
 * <h2>Configuration Example (application.yml)</h2>
 * <pre>
 * orchestration:
 *   execution-mode: template  # or "llm"
 *
 *   template:
 *     simulate-delay: true
 *     delay-ms: 1000
 *
 *   context:
 *     max-reference-files: 5
 *     max-artifacts: 10
 *     max-context-chars: 50000
 * </pre>
 *
 * <h2>Validation</h2>
 * The execution-mode must be either "template" or "llm". Invalid values will cause
 * application startup to fail with a descriptive error message.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * @Autowired
 * private OrchestrationConfig config;
 *
 * if (config.isTemplateMode()) {
 *     // Use template-based execution
 * } else {
 *     // Use LLM-based execution
 * }
 * }</pre>
 *
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 */
@Configuration
@ConfigurationProperties(prefix = "orchestration")
public class OrchestrationConfig {

    private static final Logger logger = LoggerFactory.getLogger(OrchestrationConfig.class);

    /**
     * Execution mode: "template" or "llm".
     * <p>
     * Default is "template" to enable testing without LLM API costs.
     */
    private String executionMode = "template";

    /**
     * Template mode configuration.
     */
    private TemplateConfig template = new TemplateConfig();

    /**
     * Context assembly configuration.
     */
    private ContextConfig context = new ContextConfig();

    /**
     * Gets the execution mode.
     *
     * @return "template" or "llm"
     */
    public String getExecutionMode() {
        return executionMode;
    }

    /**
     * Sets the execution mode.
     * <p>
     * Must be either "template" or "llm". Invalid values will be rejected during validation.
     *
     * @param executionMode the execution mode
     */
    public void setExecutionMode(String executionMode) {
        this.executionMode = executionMode;
    }

    /**
     * Checks if template mode is active.
     *
     * @return true if execution-mode is "template"
     */
    public boolean isTemplateMode() {
        return "template".equalsIgnoreCase(executionMode);
    }

    /**
     * Checks if LLM mode is active.
     *
     * @return true if execution-mode is "llm"
     */
    public boolean isLlmMode() {
        return "llm".equalsIgnoreCase(executionMode);
    }

    /**
     * Gets template mode configuration.
     *
     * @return template configuration
     */
    public TemplateConfig getTemplate() {
        return template;
    }

    /**
     * Sets template mode configuration.
     *
     * @param template template configuration
     */
    public void setTemplate(TemplateConfig template) {
        this.template = template;
    }

    /**
     * Gets context assembly configuration.
     *
     * @return context configuration
     */
    public ContextConfig getContext() {
        return context;
    }

    /**
     * Sets context assembly configuration.
     *
     * @param context context configuration
     */
    public void setContext(ContextConfig context) {
        this.context = context;
    }

    /**
     * Validates the configuration.
     * <p>
     * This method is called during application startup to ensure valid configuration.
     *
     * @throws IllegalArgumentException if configuration is invalid
     */
    public void validate() {
        if (executionMode == null || executionMode.trim().isEmpty()) {
            throw new IllegalArgumentException("orchestration.execution-mode must be set");
        }

        String normalizedMode = executionMode.trim().toLowerCase();
        if (!normalizedMode.equals("template") && !normalizedMode.equals("llm")) {
            throw new IllegalArgumentException(
                "orchestration.execution-mode must be 'template' or 'llm', got: " + executionMode
            );
        }

        // Normalize mode for case-insensitive matching
        this.executionMode = normalizedMode;

        logger.info("Orchestration execution mode resolved: {}", this.executionMode);

        // Validate nested configs
        if (template != null) {
            template.validate();
        }
        if (context != null) {
            context.validate();
        }
    }

    /**
     * Template mode configuration.
     */
    public static class TemplateConfig {

        /**
         * Whether to simulate execution delay (for realistic testing).
         * Default: true
         */
        private boolean simulateDelay = true;

        /**
         * Simulated delay in milliseconds.
         * Default: 1000ms (1 second)
         */
        private long delayMs = 1000;

        public boolean isSimulateDelay() {
            return simulateDelay;
        }

        public void setSimulateDelay(boolean simulateDelay) {
            this.simulateDelay = simulateDelay;
        }

        public long getDelayMs() {
            return delayMs;
        }

        public void setDelayMs(long delayMs) {
            this.delayMs = delayMs;
        }

        public void validate() {
            if (delayMs < 0) {
                throw new IllegalArgumentException(
                    "orchestration.template.delay-ms must be non-negative, got: " + delayMs
                );
            }
        }
    }

    /**
     * Context assembly configuration.
     */
    public static class ContextConfig {

        /**
         * Maximum number of reference files to include in context.
         * Default: 5
         */
        private int maxReferenceFiles = 5;

        /**
         * Maximum number of artifacts to include in context.
         * Default: 10
         */
        private int maxArtifacts = 10;

        /**
         * Maximum total context size in characters.
         * Default: 50,000 characters
         */
        private long maxContextChars = 50_000;

        public int getMaxReferenceFiles() {
            return maxReferenceFiles;
        }

        public void setMaxReferenceFiles(int maxReferenceFiles) {
            this.maxReferenceFiles = maxReferenceFiles;
        }

        public int getMaxArtifacts() {
            return maxArtifacts;
        }

        public void setMaxArtifacts(int maxArtifacts) {
            this.maxArtifacts = maxArtifacts;
        }

        public long getMaxContextChars() {
            return maxContextChars;
        }

        public void setMaxContextChars(long maxContextChars) {
            this.maxContextChars = maxContextChars;
        }

        public void validate() {
            if (maxReferenceFiles < 0) {
                throw new IllegalArgumentException(
                    "orchestration.context.max-reference-files must be non-negative, got: " + maxReferenceFiles
                );
            }
            if (maxArtifacts < 0) {
                throw new IllegalArgumentException(
                    "orchestration.context.max-artifacts must be non-negative, got: " + maxArtifacts
                );
            }
            if (maxContextChars < 1) {
                throw new IllegalArgumentException(
                    "orchestration.context.max-context-chars must be positive, got: " + maxContextChars
                );
            }
        }
    }
}
