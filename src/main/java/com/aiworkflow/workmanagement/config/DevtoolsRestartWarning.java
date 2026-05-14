package com.aiworkflow.workmanagement.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Logs a warning when Spring Boot devtools restart is enabled.
 */
@Component
public class DevtoolsRestartWarning {

    private static final Logger logger = LoggerFactory.getLogger(DevtoolsRestartWarning.class);

    private final Environment environment;

    public DevtoolsRestartWarning(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void warnIfRestartEnabled() {
        String restartRaw = environment.getProperty("spring.devtools.restart.enabled");
        String remoteRaw = environment.getProperty("spring.devtools.remote.restart.enabled");
        String addPropsRaw = environment.getProperty("spring.devtools.add-properties");

        boolean restartEnabled = Boolean.parseBoolean(environment.getProperty("spring.devtools.restart.enabled", "false"));
        boolean remoteEnabled = Boolean.parseBoolean(environment.getProperty("spring.devtools.remote.restart.enabled", "false"));

        logger.info("Devtools properties resolved: restart.enabled={} (raw='{}'), remote.restart.enabled={} (raw='{}'), add-properties={} (raw='{}')",
            restartEnabled, restartRaw, remoteEnabled, remoteRaw, addPropsRaw, addPropsRaw);

        if (restartEnabled || remoteEnabled) {
            logger.warn("Spring devtools restart is enabled (restart={}, remote={}). " +
                "This can cause classloader ClassCastException issues. " +
                "Disable with spring.devtools.restart.enabled=false.",
                restartEnabled, remoteEnabled);
        }
    }
}
