package com.aiworkflow.workmanagement.orchestration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration properties for LLM providers and role mappings.
 */
@Configuration
@ConfigurationProperties(prefix = "orchestration.llm")
public class OrchestrationLlmProperties {

    private String defaultProvider = "anthropic";
    private Map<String, Provider> providers = new HashMap<>();
    private Map<String, RoleMapping> roleMappings = new HashMap<>();
    private List<String> dummyProviders = new ArrayList<>();

    public String getDefaultProvider() {
        return defaultProvider;
    }

    public void setDefaultProvider(String defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public Map<String, Provider> getProviders() {
        return providers;
    }

    public void setProviders(Map<String, Provider> providers) {
        this.providers = providers;
    }

    public Map<String, RoleMapping> getRoleMappings() {
        return roleMappings;
    }

    public void setRoleMappings(Map<String, RoleMapping> roleMappings) {
        this.roleMappings = roleMappings;
    }

    public List<String> getDummyProviders() {
        return dummyProviders;
    }

    public void setDummyProviders(List<String> dummyProviders) {
        this.dummyProviders = dummyProviders;
    }

    public static class Provider {
        private String apiKey;
        private String model;
        private boolean enabled = true;
        private String tokenizerId;
        private String tokenizerVersion;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getTokenizerId() {
            return tokenizerId;
        }

        public void setTokenizerId(String tokenizerId) {
            this.tokenizerId = tokenizerId;
        }

        public String getTokenizerVersion() {
            return tokenizerVersion;
        }

        public void setTokenizerVersion(String tokenizerVersion) {
            this.tokenizerVersion = tokenizerVersion;
        }
    }

    public static class RoleMapping {
        private String provider;
        private String model;
        private List<String> fallbackProviders = new ArrayList<>();

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public List<String> getFallbackProviders() {
            return fallbackProviders;
        }

        public void setFallbackProviders(List<String> fallbackProviders) {
            this.fallbackProviders = fallbackProviders != null ? fallbackProviders : new ArrayList<>();
        }
    }
}
