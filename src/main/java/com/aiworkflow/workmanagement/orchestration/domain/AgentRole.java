package com.aiworkflow.workmanagement.orchestration.domain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Value object representing an agent role loaded from a ROLE.md file.
 * <p>
 * Agent roles are NOT hardcoded enums - they are dynamically discovered from
 * .agent/roles/ directory ROLE.md files at startup. This allows for flexible role
 * definitions without code changes.
 * <p>
 * Each role has name (from YAML frontmatter), description (from YAML frontmatter),
 * full content (the complete ROLE.md file), and file path (where the role definition is located).
 */
public class AgentRole {
    private final String name;
    private final String description;
    private final String fullContent;
    private final Path roleFilePath;

    /**
     * Private constructor - use fromFile() factory method instead.
     */
    private AgentRole(String name, String description, String fullContent, Path roleFilePath) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Role name cannot be null or blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Role description cannot be null or blank");
        }
        if (fullContent == null) {
            throw new IllegalArgumentException("Role content cannot be null");
        }
        if (roleFilePath == null) {
            throw new IllegalArgumentException("Role file path cannot be null");
        }

        this.name = name;
        this.description = description;
        this.fullContent = fullContent;
        this.roleFilePath = roleFilePath;
    }

    /**
     * Factory method to create an AgentRole from a ROLE.md file.
     * Parses the YAML frontmatter to extract name and description.
     *
     * @param roleFilePath path to the ROLE.md file
     * @return a new AgentRole instance
     * @throws IOException if the file cannot be read
     * @throws IllegalArgumentException if the file doesn't have valid frontmatter
     */
    public static AgentRole fromFile(Path roleFilePath) throws IOException {
        if (roleFilePath == null) {
            throw new IllegalArgumentException("Role file path cannot be null");
        }
        if (!Files.exists(roleFilePath)) {
            throw new IllegalArgumentException("Role file does not exist: " + roleFilePath);
        }
        if (!Files.isRegularFile(roleFilePath)) {
            throw new IllegalArgumentException("Path is not a regular file: " + roleFilePath);
        }

        // Read entire file content
        String fullContent = Files.readString(roleFilePath);

        // Parse frontmatter
        String name = extractFrontmatterValue(fullContent, "name");
        String description = extractFrontmatterValue(fullContent, "description");

        if (name == null) {
            throw new IllegalArgumentException("Role file missing 'name' in frontmatter: " + roleFilePath);
        }
        if (description == null) {
            throw new IllegalArgumentException("Role file missing 'description' in frontmatter: " + roleFilePath);
        }

        return new AgentRole(name, description, fullContent, roleFilePath);
    }

    /**
     * Attempts to create an AgentRole from a file, returning null if it fails.
     * Useful for scenarios where role loading errors should be handled gracefully.
     *
     * @param roleFilePath path to the ROLE.md file
     * @return a new AgentRole instance, or null if the file cannot be parsed
     */
    public static AgentRole tryFromFile(Path roleFilePath) {
        try {
            return fromFile(roleFilePath);
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Extracts a value from YAML frontmatter.
     * Expects format:
     * ---
     * key: value
     * ---
     *
     * @param content the file content
     * @param key the frontmatter key to extract
     * @return the value, or null if not found
     */
    private static String extractFrontmatterValue(String content, String key) {
        // Match YAML frontmatter block
        Pattern frontmatterPattern = Pattern.compile("^---\\s*\\n(.*?)\\n---", Pattern.DOTALL);
        Matcher frontmatterMatcher = frontmatterPattern.matcher(content);

        if (!frontmatterMatcher.find()) {
            return null;
        }

        String frontmatter = frontmatterMatcher.group(1);

        // Match specific key-value pair
        Pattern keyPattern = Pattern.compile("^" + Pattern.quote(key) + ":\\s*(.+)$", Pattern.MULTILINE);
        Matcher keyMatcher = keyPattern.matcher(frontmatter);

        if (keyMatcher.find()) {
            return keyMatcher.group(1).trim();
        }

        return null;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getFullContent() {
        return fullContent;
    }

    public Path getRoleFilePath() {
        return roleFilePath;
    }

    /**
     * Returns the role name in lowercase hyphenated format.
     * Example: "art-designer", "game-rules-designer"
     *
     * @return lowercase hyphenated role name
     */
    public String getFileName() {
        return name.toLowerCase().replace(' ', '-');
    }

    /**
     * Checks if this role matches a given role name (case-insensitive).
     *
     * @param roleName the role name to check
     * @return true if names match (case-insensitive)
     */
    public boolean matches(String roleName) {
        return name.equalsIgnoreCase(roleName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentRole agentRole = (AgentRole) o;
        return Objects.equals(name, agentRole.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "AgentRole{" +
            "name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", roleFilePath=" + roleFilePath +
            '}';
    }
}
