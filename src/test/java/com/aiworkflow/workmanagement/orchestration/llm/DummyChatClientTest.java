package com.aiworkflow.workmanagement.orchestration.llm;

import com.aiworkflow.workmanagement.orchestration.domain.LlmChatResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DummyChatClientTest {

    @Test
    void writesPromptAndReadsResponse(@TempDir Path tempDir) throws Exception {
        System.setProperty("orchestration.llm.dummy.bridge-root", tempDir.toString());
        System.setProperty("orchestration.llm.dummy.poll-ms", "10");
        System.setProperty("orchestration.llm.dummy.timeout-ms", "1000");

        DummyChatClient client = new DummyChatClient("dummy", "file-bridge");
        String prompt = "Provide a short summary.";

        Thread responder = new Thread(() -> {
            Path promptPath = waitForPrompt(tempDir, Duration.ofMillis(800));
            if (promptPath != null) {
                Path responsePath = promptPath.getParent().resolve("DummyLLMResponse.md");
                try {
                    Files.writeString(responsePath, "response text");
                } catch (Exception ignored) {
                    // Ignore errors in test responder.
                }
            }
        });
        responder.start();

        LlmChatResponse response = client.generate("coder", prompt);

        assertThat(response.getText()).isEqualTo("response text");
        assertThat(response.getUsage()).isNotNull();
        assertThat(response.getUsage().isEstimated()).isTrue();
        assertThat(response.getUsage().getRawMetadata()).containsKeys("promptPath", "responsePath");

        Path promptPath = Path.of(response.getUsage().getRawMetadata().get("promptPath").toString());
        String promptContent = Files.readString(promptPath);
        assertThat(promptContent).contains(prompt);

        clearDummyProperties();
    }

    @Test
    void writesClipboardPromptAndReadsResponse(@TempDir Path tempDir) throws Exception {
        System.setProperty("orchestration.llm.dummy.bridge-root", tempDir.toString());
        System.setProperty("orchestration.llm.dummy.poll-ms", "10");
        System.setProperty("orchestration.llm.dummy.timeout-ms", "1000");

        DummyChatClient client = new DummyChatClient("dummy", "clipboard");
        String prompt = "Clipboard prompt";

        Thread responder = new Thread(() -> {
            Path promptPath = waitForClipboardPrompt(tempDir, Duration.ofMillis(800));
            if (promptPath != null) {
                Path responsePath = promptPath.getParent().resolve("DummyLLMResponse.md");
                try {
                    Files.writeString(responsePath, "clipboard response");
                } catch (Exception ignored) {
                    // Ignore errors in test responder.
                }
            }
        });
        responder.start();

        LlmChatResponse response = client.generate("designer", prompt);

        assertThat(response.getText()).isEqualTo("clipboard response");
        assertThat(response.getUsage()).isNotNull();
        assertThat(response.getUsage().getRawMetadata()).containsKey("mode");

        Path promptPath = Path.of(response.getUsage().getRawMetadata().get("promptPath").toString());
        String promptContent = Files.readString(promptPath);
        assertThat(promptContent).contains(prompt);

        clearDummyProperties();
    }

    @Test
    void timesOutWhenResponseMissing(@TempDir Path tempDir) {
        System.setProperty("orchestration.llm.dummy.bridge-root", tempDir.toString());
        System.setProperty("orchestration.llm.dummy.poll-ms", "10");
        System.setProperty("orchestration.llm.dummy.timeout-ms", "50");

        DummyChatClient client = new DummyChatClient("dummy", "file-bridge");

        assertThatThrownBy(() -> client.generate("coder", "prompt"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Timed out");

        clearDummyProperties();
    }

    @Test
    void returnsFixtureForRole(@TempDir Path tempDir) throws Exception {
        Path fixtureRoot = tempDir.resolve("fixtures");
        Path roleFixture = fixtureRoot.resolve("dummy").resolve("roles").resolve("designer.md");
        Files.createDirectories(roleFixture.getParent());
        Files.writeString(roleFixture, "fixture response for role");

        System.setProperty("orchestration.llm.dummy.mode", "fixture");
        System.setProperty("orchestration.llm.dummy.fixture-root", fixtureRoot.toString());

        DummyChatClient client = new DummyChatClient("dummy", "fixture");
        LlmChatResponse response = client.generate("designer", "prompt");

        assertThat(response.getText()).isEqualTo("fixture response for role");
        assertThat(response.getUsage()).isNotNull();
        assertThat(response.getUsage().getRawMetadata()).containsKey("mode");

        clearDummyProperties();
        System.clearProperty("orchestration.llm.dummy.fixture-root");
    }

    @Test
    void fallsBackToDefaultFixtureWhenNoMatch(@TempDir Path tempDir) throws Exception {
        Path fixtureRoot = tempDir.resolve("fixtures");
        Path defaultFixture = fixtureRoot.resolve("default.md");
        Files.createDirectories(defaultFixture.getParent());
        Files.writeString(defaultFixture, "default fixture response");

        System.setProperty("orchestration.llm.dummy.mode", "fixture");
        System.setProperty("orchestration.llm.dummy.fixture-root", fixtureRoot.toString());

        DummyChatClient client = new DummyChatClient("missing", "fixture");
        LlmChatResponse response = client.generate("unknown", "prompt");

        assertThat(response.getText()).isEqualTo("default fixture response");

        clearDummyProperties();
        System.clearProperty("orchestration.llm.dummy.fixture-root");
    }

    private void clearDummyProperties() {
        System.clearProperty("orchestration.llm.dummy.bridge-root");
        System.clearProperty("orchestration.llm.dummy.poll-ms");
        System.clearProperty("orchestration.llm.dummy.timeout-ms");
    }

    private Path waitForPrompt(Path tempDir, Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            try {
                Path providerDir = tempDir.resolve("dummy");
                if (Files.exists(providerDir)) {
                    try (var stream = Files.list(providerDir)) {
                        Path requestDir = stream.filter(Files::isDirectory).findFirst().orElse(null);
                        if (requestDir != null) {
                            Path promptPath = requestDir.resolve("DummyLLMPrompt.md");
                            if (Files.exists(promptPath)) {
                                return promptPath;
                            }
                        }
                    }
                }
                Thread.sleep(10L);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private Path waitForClipboardPrompt(Path tempDir, Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        Path promptPath = tempDir.resolve("dummy").resolve("clipboard").resolve("DummyLLMPrompt.md");
        while (System.currentTimeMillis() < deadline) {
            if (Files.exists(promptPath)) {
                return promptPath;
            }
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }
}

