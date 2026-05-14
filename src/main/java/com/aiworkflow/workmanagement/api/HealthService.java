package com.aiworkflow.workmanagement.api;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Supplies health response data.
 */
@Service
public class HealthService {
    private final Instant startedAt;
    private final BuildProperties buildProperties;

    public HealthService(ObjectProvider<BuildProperties> buildPropertiesProvider) {
        this.startedAt = Instant.now();
        this.buildProperties = buildPropertiesProvider.getIfAvailable();
    }

    public HealthResponse getHealth() {
        String version = buildProperties != null ? buildProperties.getVersion() : "unknown";
        String buildTime = buildProperties != null && buildProperties.getTime() != null
            ? buildProperties.getTime().toString()
            : null;
        long uptimeSeconds = Duration.between(startedAt, Instant.now()).getSeconds();

        return new HealthResponse("UP", version, buildTime, uptimeSeconds);
    }
}

