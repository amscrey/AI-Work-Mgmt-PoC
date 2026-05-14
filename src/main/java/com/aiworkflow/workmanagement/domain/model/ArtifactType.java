package com.aiworkflow.workmanagement.domain.model;

/**
 * Represents the type of artifact in the workflow system.
 * Used to categorize and handle different kinds of work products.
 */
public enum ArtifactType {
    /**
     * Source code file
     */
    SOURCE_CODE,

    /**
     * Configuration file
     */
    CONFIGURATION,

    /**
     * Documentation file
     */
    DOCUMENTATION,

    /**
     * Test file
     */
    TEST,

    /**
     * Build or deployment script
     */
    SCRIPT,

    /**
     * Data file (JSON, XML, CSV, etc.)
     */
    DATA,

    /**
     * Image or diagram
     */
    IMAGE,

    /**
     * Other artifact type
     */
    OTHER;

    /**
     * Determines the artifact type from a file extension.
     *
     * @param extension The file extension (with or without leading dot)
     * @return The corresponding ArtifactType
     */
    public static ArtifactType fromExtension(String extension) {
        String ext = extension.toLowerCase().replace(".", "");

        return switch (ext) {
            case "java", "js", "ts", "py", "rb", "go", "rs", "c", "cpp", "h", "cs" -> SOURCE_CODE;
            case "json", "yaml", "yml", "xml", "properties", "conf", "config", "ini" -> CONFIGURATION;
            case "md", "txt", "rst", "adoc", "html" -> DOCUMENTATION;
            case "test.js", "test.ts", "spec.js", "spec.ts" -> TEST;
            case "sh", "bash", "ps1", "bat", "cmd" -> SCRIPT;
            case "csv", "tsv", "dat" -> DATA;
            case "png", "jpg", "jpeg", "gif", "svg", "bmp" -> IMAGE;
            default -> OTHER;
        };
    }

    /**
     * Determines if this artifact type represents executable code.
     */
    public boolean isExecutable() {
        return this == SOURCE_CODE || this == SCRIPT || this == TEST;
    }

    /**
     * Determines if this artifact type is human-readable text.
     */
    public boolean isTextBased() {
        return this != IMAGE;
    }
}
