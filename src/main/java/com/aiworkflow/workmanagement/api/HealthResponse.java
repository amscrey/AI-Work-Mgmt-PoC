package com.aiworkflow.workmanagement.api;

/**
 * DTO returned by the health endpoint.
 */
public class HealthResponse {
    private final String status;
    private final String version;
    private final String buildTime;
    private final long uptimeSeconds;

    public HealthResponse(String status, String version, String buildTime, long uptimeSeconds) {
        this.status = status;
        this.version = version;
        this.buildTime = buildTime;
        this.uptimeSeconds = uptimeSeconds;
    }

    public String getStatus() {
        return status;
    }

    public String getVersion() {
        return version;
    }

    public String getBuildTime() {
        return buildTime;
    }

    public long getUptimeSeconds() {
        return uptimeSeconds;
    }
}

