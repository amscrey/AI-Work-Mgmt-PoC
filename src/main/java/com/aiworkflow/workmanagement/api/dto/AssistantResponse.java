package com.aiworkflow.workmanagement.api.dto;

import java.util.List;

public class AssistantResponse {
    private final String format;
    private final String summary;
    private final List<String> highlights;
    private final List<AssistantReference> references;

    public AssistantResponse(String format, String summary, List<String> highlights, List<AssistantReference> references) {
        this.format = format;
        this.summary = summary;
        this.highlights = highlights;
        this.references = references;
    }

    public String getFormat() {
        return format;
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getHighlights() {
        return highlights;
    }

    public List<AssistantReference> getReferences() {
        return references;
    }
}
