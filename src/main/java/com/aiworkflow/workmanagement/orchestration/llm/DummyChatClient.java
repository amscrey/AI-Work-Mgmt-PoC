package com.aiworkflow.workmanagement.orchestration.llm;

import com.aiworkflow.workmanagement.orchestration.domain.LlmChatResponse;
import com.aiworkflow.workmanagement.orchestration.domain.LlmUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Dummy LLM chat client used for tests and development.
 *
 * <p>File-bridge mode writes prompts to a predictable workspace directory and waits
 * for a human-provided response file. This enables manual tools (e.g., Copilot or
 * Claude Code) to participate without direct API integration.</p>
 */
public class DummyChatClient implements LLMChatClient {

    private static final Logger logger = LoggerFactory.getLogger(DummyChatClient.class);

    private static final String ROOT_PROPERTY = "orchestration.llm.dummy.bridge-root";
    private static final String FIXTURE_ROOT_PROPERTY = "orchestration.llm.dummy.fixture-root";
    private static final String POLL_MS_PROPERTY = "orchestration.llm.dummy.poll-ms";
    private static final String TIMEOUT_MS_PROPERTY = "orchestration.llm.dummy.timeout-ms";

    private static final long DEFAULT_POLL_MS = 250L;
    private static final long DEFAULT_TIMEOUT_MS = 900_000L;

    private static final String MODE_FILE_BRIDGE = "file-bridge";
    private static final String MODE_INSTANT = "instant";
    private static final String MODE_CLIPBOARD = "clipboard";
    private static final String MODE_FIXTURE = "fixture";

    private static final String DUMMY_PROMPT_FILENAME = "DummyLLMPrompt.md";
    private static final String DUMMY_RESPONSE_FILENAME = "DummyLLMResponse.md";

    private final String providerName;
    private final Path bridgeRoot;
    private final Path fixtureRoot;
    private final long pollIntervalMs;
    private final long timeoutMs;
    private final String mode;

    public DummyChatClient(String providerName, String model) {
        this.providerName = providerName;
        this.bridgeRoot = resolveBridgeRoot();
        this.fixtureRoot = resolveFixtureRoot();
        this.pollIntervalMs = readLongProperty(POLL_MS_PROPERTY, DEFAULT_POLL_MS);
        this.timeoutMs = readLongProperty(TIMEOUT_MS_PROPERTY, DEFAULT_TIMEOUT_MS);
        this.mode = normalizeModel(model);

        if (MODE_FILE_BRIDGE.equalsIgnoreCase(this.mode) || MODE_CLIPBOARD.equalsIgnoreCase(this.mode)) {
            ensureBridgeReady();
        }
    }

    @Override
    public LlmChatResponse generate(String roleName, String prompt) {
        if (MODE_INSTANT.equalsIgnoreCase(mode)) {
            return buildInstantResponse(roleName, prompt);
        }
        if (MODE_CLIPBOARD.equalsIgnoreCase(mode)) {
            return buildClipboardResponse(roleName, prompt);
        }
        if (MODE_FIXTURE.equalsIgnoreCase(mode)) {
            return buildFixtureResponse(roleName, prompt);
        }
        String correlationId = UUID.randomUUID().toString();
        Path requestDir = bridgeRoot.resolve(providerName).resolve(correlationId);
        Path promptPath = requestDir.resolve(DUMMY_PROMPT_FILENAME);
        Path responsePath = requestDir.resolve(DUMMY_RESPONSE_FILENAME);

        logger.info("Dummy file-bridge request created: provider={}, correlationId={}, promptPath={}, responsePath={}",
            providerName, correlationId, promptPath, responsePath);

        writePrompt(promptPath, roleName, prompt, correlationId);
        String responseText = waitForResponse(responsePath, correlationId);

        long promptTokens = estimateTokens(prompt);
        long completionTokens = estimateTokens(responseText);
        long totalTokens = promptTokens + completionTokens;

        Map<String, Object> rawMetadata = new HashMap<>();
        rawMetadata.put("correlationId", correlationId);
        rawMetadata.put("promptPath", promptPath.toString());
        rawMetadata.put("responsePath", responsePath.toString());
        rawMetadata.put("contextTokens", promptTokens);

        LlmUsage usage = new LlmUsage(
            promptTokens,
            completionTokens,
            totalTokens,
            0L,
            true,
            rawMetadata
        );

        return new LlmChatResponse(responseText, providerName, "file-bridge", usage);
    }

    private LlmChatResponse buildInstantResponse(String roleName, String prompt) {
        String text = "dummy-response:" + providerName + ":" + (roleName != null ? roleName : "unknown");
        long promptTokens = estimateTokens(prompt);
        long completionTokens = estimateTokens(text);
        long totalTokens = promptTokens + completionTokens;

        Map<String, Object> rawMetadata = new HashMap<>();
        rawMetadata.put("contextTokens", promptTokens);
        rawMetadata.put("mode", MODE_INSTANT);

        LlmUsage usage = new LlmUsage(
            promptTokens,
            completionTokens,
            totalTokens,
            0L,
            true,
            rawMetadata
        );

        return new LlmChatResponse(text, providerName, "dummy", usage);
    }

    private LlmChatResponse buildClipboardResponse(String roleName, String prompt) {
        Path requestDir = bridgeRoot.resolve(providerName).resolve("clipboard");
        Path promptPath = requestDir.resolve(DUMMY_PROMPT_FILENAME);
        Path responsePath = requestDir.resolve(DUMMY_RESPONSE_FILENAME);

        writeClipboardPrompt(promptPath, roleName, prompt);
        String responseText = waitForResponse(responsePath, "clipboard");

        long promptTokens = estimateTokens(prompt);
        long completionTokens = estimateTokens(responseText);
        long totalTokens = promptTokens + completionTokens;

        Map<String, Object> rawMetadata = new HashMap<>();
        rawMetadata.put("promptPath", promptPath.toString());
        rawMetadata.put("responsePath", responsePath.toString());
        rawMetadata.put("contextTokens", promptTokens);
        rawMetadata.put("mode", MODE_CLIPBOARD);

        LlmUsage usage = new LlmUsage(
            promptTokens,
            completionTokens,
            totalTokens,
            0L,
            true,
            rawMetadata
        );

        return new LlmChatResponse(responseText, providerName, "clipboard", usage);
    }

    private LlmChatResponse buildFixtureResponse(String roleName, String prompt) {
        String responseText = loadFixture(roleName, prompt);

        long promptTokens = estimateTokens(prompt);
        long completionTokens = estimateTokens(responseText);
        long totalTokens = promptTokens + completionTokens;

        Map<String, Object> rawMetadata = new HashMap<>();
        rawMetadata.put("contextTokens", promptTokens);
        rawMetadata.put("mode", MODE_FIXTURE);

        LlmUsage usage = new LlmUsage(
            promptTokens,
            completionTokens,
            totalTokens,
            0L,
            true,
            rawMetadata
        );

        return new LlmChatResponse(responseText, providerName, "fixture", usage);
    }

    private String loadFixture(String roleName, String prompt) {
        Path providerDir = fixtureRoot.resolve(providerName);
        if (!Files.exists(providerDir)) {
            providerDir = fixtureRoot;
        }

        Path roleFixture = resolveRoleFixture(providerDir, roleName);
        if (roleFixture != null) {
            return readFixture(roleFixture);
        }

        Path keywordFixture = resolveKeywordFixture(providerDir, prompt);
        if (keywordFixture != null) {
            return readFixture(keywordFixture);
        }

        Path defaultFixture = providerDir.resolve("default.md");
        if (Files.exists(defaultFixture)) {
            return readFixture(defaultFixture);
        }

        return "dummy-response:fixture:" + providerName + ":" + (roleName != null ? roleName : "unknown");
    }

    private Path resolveRoleFixture(Path providerDir, String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return null;
        }
        Path roleFixture = providerDir.resolve("roles").resolve(roleName.toLowerCase() + ".md");
        return Files.exists(roleFixture) ? roleFixture : null;
    }

    private Path resolveKeywordFixture(Path providerDir, String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return null;
        }
        Path keywordsDir = providerDir.resolve("keywords");
        if (!Files.exists(keywordsDir)) {
            return null;
        }
        String lowerPrompt = prompt.toLowerCase();
        try {
            return Files.list(keywordsDir)
                .filter(path -> path.toString().endsWith(".md"))
                .sorted()
                .filter(path -> lowerPrompt.contains(stripExtension(path.getFileName().toString())))
                .findFirst()
                .orElse(null);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan fixture keywords", e);
        }
    }

    private String stripExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex <= 0) {
            return filename.toLowerCase();
        }
        return filename.substring(0, dotIndex).toLowerCase();
    }

    private String readFixture(Path fixturePath) {
        try {
            return Files.readString(fixturePath).trim();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read fixture response", e);
        }
    }

    private void writePrompt(Path promptPath, String roleName, String prompt, String correlationId) {
        try {
            Files.createDirectories(promptPath.getParent());
            StringBuilder content = new StringBuilder();
            content.append("# Dummy LLM File Bridge\n\n");
            content.append("CorrelationId: ").append(correlationId).append("\n");
            content.append("Provider: ").append(providerName).append("\n");
            content.append("Role: ").append(roleName != null ? roleName : "unknown").append("\n");
            content.append("Timestamp: ").append(Instant.now()).append("\n\n");
            content.append("---\n\n");
            content.append(prompt != null ? prompt : "");
            Files.writeString(promptPath, content.toString());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write dummy prompt", e);
        }
    }

    private void writeClipboardPrompt(Path promptPath, String roleName, String prompt) {
        try {
            Files.createDirectories(promptPath.getParent());
            StringBuilder content = new StringBuilder();
            content.append("PROMPT (copy into your tool):\n\n");
            content.append("Provider: ").append(providerName).append("\n");
            content.append("Role: ").append(roleName != null ? roleName : "unknown").append("\n\n");
            content.append(prompt != null ? prompt : "");
            Files.writeString(promptPath, content.toString());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write clipboard prompt", e);
        }
    }

    private String waitForResponse(Path responsePath, String correlationId) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (Files.exists(responsePath)) {
                try {
                    String response = Files.readString(responsePath).trim();
                    if (!response.isBlank()) {
                        return response;
                    }
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to read dummy response", e);
                }
            }
            sleep(pollIntervalMs);
        }
        logger.warn("Timed out waiting for dummy response for correlationId {}", correlationId);
        throw new IllegalStateException("Timed out waiting for dummy response");
    }

    private void sleep(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Path resolveBridgeRoot() {
        // Prefer explicit environment variable BRIDGE_ROOT (aligns with how WORKSPACE_ROOT is used)
        String envBridge = System.getenv("BRIDGE_ROOT");
        if (envBridge != null && !envBridge.isBlank()) {
            return Paths.get(envBridge);
        }

        // Fallback to legacy system property if provided (keeps backward compatibility)
        String configured = System.getProperty(ROOT_PROPERTY);
        if (configured != null && !configured.isBlank()) {
            return Paths.get(configured);
        }

        // Next fallback: derive from WORKSPACE_ROOT environment
        String workspaceRoot = System.getenv("WORKSPACE_ROOT");
        if (workspaceRoot != null && !workspaceRoot.isBlank()) {
            return Paths.get(workspaceRoot).resolve("dummy-bridge");
        }

        // Last resort: local workspaces/dummy-bridge under user.dir
        return Paths.get(System.getProperty("user.dir"), "workspaces", "dummy-bridge");
    }

    private Path resolveFixtureRoot() {
        String configured = System.getProperty(FIXTURE_ROOT_PROPERTY);
        if (configured != null && !configured.isBlank()) {
            return Paths.get(configured);
        }
        return Paths.get(System.getProperty("user.dir"), "workspaces", "dummy-fixtures");
    }

    private long readLongProperty(String key, long defaultValue) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String normalizeModel(String model) {
        if (model == null || model.isBlank()) {
            return MODE_FILE_BRIDGE;
        }
        String normalized = model.trim().toLowerCase();
        if (MODE_INSTANT.equals(normalized)) {
            return MODE_INSTANT;
        }
        if (MODE_CLIPBOARD.equals(normalized)) {
            return MODE_CLIPBOARD;
        }
        if (MODE_FIXTURE.equals(normalized)) {
            return MODE_FIXTURE;
        }
        return MODE_FILE_BRIDGE;
    }

    private long estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0L;
        }
        return text.trim().split("\\s+").length;
    }

    private void ensureBridgeReady() {
        try {
            Files.createDirectories(bridgeRoot);
            logger.info("Dummy bridge ready: mode={}, root={}", mode, bridgeRoot);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create dummy bridge root: " + bridgeRoot, e);
        }
    }
}
