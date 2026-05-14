package com.aiworkflow.workmanagement.orchestration.domain;

/**
 * Usage totals per provider/model.
 */
public class ProviderModelUsage {
    private final String provider;
    private final String model;
    private final TokenTotals exactTotals;
    private final TokenTotals estimatedTotals;

    public ProviderModelUsage(String provider, String model) {
        this.provider = provider;
        this.model = model;
        this.exactTotals = new TokenTotals();
        this.estimatedTotals = new TokenTotals();
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }

    public TokenTotals getExactTotals() {
        return exactTotals;
    }

    public TokenTotals getEstimatedTotals() {
        return estimatedTotals;
    }
}

