package com.aiworkflow.workmanagement.startup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class StartupEnvironmentLogger {

    private static final Logger logger = LoggerFactory.getLogger(StartupEnvironmentLogger.class);
    private final Environment env;
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public StartupEnvironmentLogger(Environment env) {
        this.env = env;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent ev) {
        try {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("timestamp", Instant.now().toString());
            out.put("activeProfiles", env.getActiveProfiles());

            // Common devtools properties
            out.put("spring.devtools.restart.enabled", env.getProperty("spring.devtools.restart.enabled"));
            out.put("spring.devtools.remote.restart.enabled", env.getProperty("spring.devtools.remote.restart.enabled"));
            out.put("spring.devtools.add-properties", env.getProperty("spring.devtools.add-properties"));

            // Orchestration / LLM related properties often used by the app
            out.put("orchestration.llm.dummy.mode", env.getProperty("orchestration.llm.dummy.mode"));
            out.put("orchestration.llm.dummy.bridge-root", env.getProperty("orchestration.llm.dummy.bridge-root"));
            out.put("orchestration.llm.default-provider", env.getProperty("orchestration.llm.default-provider"));

            // Common environment variables
            out.put("env.WORKSPACE_ROOT", System.getenv("WORKSPACE_ROOT"));
            out.put("env.BRIDGE_ROOT", System.getenv("BRIDGE_ROOT"));
            out.put("user.dir", System.getProperty("user.dir"));
            out.put("java.version", System.getProperty("java.version"));

            String json = mapper.writeValueAsString(out);

            // Log a short summary
            logger.info("Startup environment snapshot:\n{}", json);

            // Write to target/startup-env.json so it's easy to find after build/run
            Path target = Path.of("target", "startup-env.json");
            Files.createDirectories(target.getParent());
            Files.writeString(target, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

        } catch (IOException e) {
            logger.warn("Unable to write startup environment file: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.warn("Unexpected error while creating startup environment snapshot: {}", e.getMessage(), e);
        }
    }
}

