package com.aiworkflow.workmanagement.api.dto;

import com.aiworkflow.workmanagement.orchestration.domain.ProviderModelUsage;

public class ProviderModelUsageResponse {
    private final String provider;
    private final String model;
    private final TokenTotalsResponse exactTotals;
    private final TokenTotalsResponse estimatedTotals;

    public ProviderModelUsageResponse(
        String provider,
        String model,
        TokenTotalsResponse exactTotals,
        TokenTotalsResponse estimatedTotals
    ) {
        this.provider = provider;
        this.model = model;
        this.exactTotals = exactTotals;
        this.estimatedTotals = estimatedTotals;
    }

    public static ProviderModelUsageResponse from(ProviderModelUsage usage) {
        return new ProviderModelUsageResponse(
            usage.getProvider(),
            usage.getModel(),
            TokenTotalsResponse.from(usage.getExactTotals()),
            TokenTotalsResponse.from(usage.getEstimatedTotals())
        );
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }

    public TokenTotalsResponse getExactTotals() {
        return exactTotals;
    }

    public TokenTotalsResponse getEstimatedTotals() {
        return estimatedTotals;
    }
}

