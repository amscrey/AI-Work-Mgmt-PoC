package com.aiworkflow.workmanagement.domain.model;

/**
 * Represents supported file formats in the workflow system.
 * Used for file validation and processing.
 */
public enum FileFormat {
    JSON("json", "application/json"),
    YAML("yaml", "application/yaml"),
    MARKDOWN("md", "text/markdown"),
    TEXT("txt", "text/plain"),
    JAVA("java", "text/x-java"),
    JAVASCRIPT("js", "text/javascript"),
    TYPESCRIPT("ts", "text/typescript"),
    PYTHON("py", "text/x-python"),
    XML("xml", "application/xml"),
    HTML("html", "text/html"),
    CSS("css", "text/css"),
    SHELL("sh", "text/x-shellscript"),
    SQL("sql", "application/sql"),
    CSV("csv", "text/csv");

    private final String extension;
    private final String mimeType;

    FileFormat(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }

    public String getExtension() {
        return extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    /**
     * Determines the file format from a file extension.
     *
     * @param extension The file extension (with or without leading dot)
     * @return The corresponding FileFormat, or null if not found
     */
    public static FileFormat fromExtension(String extension) {
        String ext = extension.toLowerCase().replace(".", "");

        for (FileFormat format : values()) {
            if (format.extension.equals(ext)) {
                return format;
            }
        }

        return null;
    }

    /**
     * Determines if this file format is a programming language.
     */
    public boolean isProgrammingLanguage() {
        return this == JAVA || this == JAVASCRIPT || this == TYPESCRIPT ||
               this == PYTHON || this == SHELL || this == SQL;
    }

    /**
     * Determines if this file format is a markup language.
     */
    public boolean isMarkupLanguage() {
        return this == HTML || this == XML || this == MARKDOWN;
    }

    /**
     * Determines if this file format is structured data.
     */
    public boolean isStructuredData() {
        return this == JSON || this == YAML || this == XML || this == CSV;
    }
}
