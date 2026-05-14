package com.aiworkflow.workmanagement.domain.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entity representing workspace configuration for the AI workflow management system.
 * Defines workspace structure, permissions, and behavioral settings.
 */
public class WorkspaceConfig {
    private final Path workspaceRoot;
    private String workspaceName;
    private boolean autoTransitionEnabled;
    private boolean requireApprovalForCompletion;
    private int maxConcurrentStories;
    private final List<String> allowedRoles;
    private final List<String> allowedFileExtensions;
    private Path archiveDirectory;
    private boolean enableNotifications;
    private String defaultAssignee;

    public WorkspaceConfig(Path workspaceRoot, String workspaceName) {
        if (workspaceRoot == null) {
            throw new IllegalArgumentException("Workspace root cannot be null");
        }
        if (workspaceName == null || workspaceName.isBlank()) {
            throw new IllegalArgumentException("Workspace name cannot be null or blank");
        }

        this.workspaceRoot = workspaceRoot;
        this.workspaceName = workspaceName;
        this.allowedRoles = new ArrayList<>();
        this.allowedFileExtensions = new ArrayList<>();

        // Set defaults
        this.autoTransitionEnabled = true;
        this.requireApprovalForCompletion = true;
        this.maxConcurrentStories = 5;
        this.enableNotifications = true;

        // Default allowed roles
        this.allowedRoles.add("coder");
        this.allowedRoles.add("reviewer");
        this.allowedRoles.add("designer");
        this.allowedRoles.add("tester");

        // Default allowed file extensions
        this.allowedFileExtensions.add("java");
        this.allowedFileExtensions.add("md");
        this.allowedFileExtensions.add("json");
        this.allowedFileExtensions.add("yaml");
    }

    // Getters
    public Path getWorkspaceRoot() {
        return workspaceRoot;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public boolean isAutoTransitionEnabled() {
        return autoTransitionEnabled;
    }

    public boolean isRequireApprovalForCompletion() {
        return requireApprovalForCompletion;
    }

    public int getMaxConcurrentStories() {
        return maxConcurrentStories;
    }

    public List<String> getAllowedRoles() {
        return Collections.unmodifiableList(allowedRoles);
    }

    public List<String> getAllowedFileExtensions() {
        return Collections.unmodifiableList(allowedFileExtensions);
    }

    public Path getArchiveDirectory() {
        return archiveDirectory;
    }

    public boolean isEnableNotifications() {
        return enableNotifications;
    }

    public String getDefaultAssignee() {
        return defaultAssignee;
    }

    // Business methods
    public void updateWorkspaceName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Workspace name cannot be null or blank");
        }
        this.workspaceName = name;
    }

    public void setAutoTransitionEnabled(boolean enabled) {
        this.autoTransitionEnabled = enabled;
    }

    public void setRequireApprovalForCompletion(boolean required) {
        this.requireApprovalForCompletion = required;
    }

    public void setMaxConcurrentStories(int max) {
        if (max < 1) {
            throw new IllegalArgumentException("Max concurrent stories must be at least 1");
        }
        this.maxConcurrentStories = max;
    }

    public void addAllowedRole(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role cannot be null or blank");
        }
        if (!allowedRoles.contains(role)) {
            allowedRoles.add(role);
        }
    }

    public void removeAllowedRole(String role) {
        allowedRoles.remove(role);
    }

    public boolean isRoleAllowed(String role) {
        return allowedRoles.contains(role);
    }

    public void addAllowedFileExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            throw new IllegalArgumentException("Extension cannot be null or blank");
        }
        String cleanExt = extension.replace(".", "").toLowerCase();
        if (!allowedFileExtensions.contains(cleanExt)) {
            allowedFileExtensions.add(cleanExt);
        }
    }

    public void removeAllowedFileExtension(String extension) {
        String cleanExt = extension.replace(".", "").toLowerCase();
        allowedFileExtensions.remove(cleanExt);
    }

    public boolean isFileExtensionAllowed(String extension) {
        String cleanExt = extension.replace(".", "").toLowerCase();
        return allowedFileExtensions.contains(cleanExt);
    }

    public void setArchiveDirectory(Path directory) {
        this.archiveDirectory = directory;
    }

    public void setEnableNotifications(boolean enabled) {
        this.enableNotifications = enabled;
    }

    public void setDefaultAssignee(String assignee) {
        this.defaultAssignee = assignee;
    }

    public Path getStoryDirectory(WorkflowState state, PrioritizationState prioritization) {
        return workspaceRoot
            .resolve(state.getDirectoryName())
            .resolve(prioritization.getDirectoryName());
    }

    public Path getArchivePath() {
        if (archiveDirectory != null) {
            return archiveDirectory;
        }
        return workspaceRoot.resolve("archive");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkspaceConfig that = (WorkspaceConfig) o;
        return Objects.equals(workspaceRoot, that.workspaceRoot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceRoot);
    }

    @Override
    public String toString() {
        return "WorkspaceConfig{" +
            "workspaceRoot=" + workspaceRoot +
            ", workspaceName='" + workspaceName + '\'' +
            ", autoTransitionEnabled=" + autoTransitionEnabled +
            ", requireApprovalForCompletion=" + requireApprovalForCompletion +
            ", maxConcurrentStories=" + maxConcurrentStories +
            '}';
    }
}
