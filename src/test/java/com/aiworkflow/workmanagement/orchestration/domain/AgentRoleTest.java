package com.aiworkflow.workmanagement.orchestration.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AgentRole Tests")
class AgentRoleTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should parse role from valid ROLE.md file")
    void shouldParseRoleFromValidRoleFile() throws IOException {
        Path roleFile = tempDir.resolve("ROLE.md");
        String content = """
            ---
            name: researcher
            description: Conducts research and analysis
            ---

            # Researcher Role

            More content here.
            """;
        Files.writeString(roleFile, content);

        AgentRole role = AgentRole.fromFile(roleFile);

        assertThat(role.getName()).isEqualTo("researcher");
        assertThat(role.getDescription()).isEqualTo("Conducts research and analysis");
        assertThat(role.getFullContent()).isEqualTo(content);
        assertThat(role.getRoleFilePath()).isEqualTo(roleFile);
    }

    @Test
    @DisplayName("Should parse role with complex description")
    void shouldParseRoleWithComplexDescription() throws IOException {
        Path roleFile = tempDir.resolve("ROLE.md");
        String content = """
            ---
            name: game-rules-designer
            description: Designs game mechanics, rules, balance systems, and gameplay dynamics.
            ---

            Content here.
            """;
        Files.writeString(roleFile, content);

        AgentRole role = AgentRole.fromFile(roleFile);

        assertThat(role.getName()).isEqualTo("game-rules-designer");
        assertThat(role.getDescription()).contains("game mechanics");
        assertThat(role.getDescription()).contains("balance systems");
    }

    @Test
    @DisplayName("Should extract name from frontmatter")
    void shouldExtractNameFromFrontmatter() throws IOException {
        Path roleFile = tempDir.resolve("ROLE.md");
        String content = """
            ---
            name: logician
            description: Solves logical problems
            ---

            # Content
            """;
        Files.writeString(roleFile, content);

        AgentRole role = AgentRole.fromFile(roleFile);

        assertThat(role.getName()).isEqualTo("logician");
    }

    @Test
    @DisplayName("Should reject file without frontmatter")
    void shouldRejectFileWithoutFrontmatter() throws IOException {
        Path roleFile = tempDir.resolve("ROLE.md");
        Files.writeString(roleFile, "# Just content, no frontmatter");

        assertThatThrownBy(() -> AgentRole.fromFile(roleFile))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("missing 'name' in frontmatter");
    }

    @Test
    @DisplayName("Should reject file without name in frontmatter")
    void shouldRejectFileWithoutNameInFrontmatter() throws IOException {
        Path roleFile = tempDir.resolve("ROLE.md");
        String content = """
            ---
            description: Missing name field
            ---
            """;
        Files.writeString(roleFile, content);

        assertThatThrownBy(() -> AgentRole.fromFile(roleFile))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("missing 'name' in frontmatter");
    }

    @Test
    @DisplayName("Should reject file without description in frontmatter")
    void shouldRejectFileWithoutDescriptionInFrontmatter() throws IOException {
        Path roleFile = tempDir.resolve("ROLE.md");
        String content = """
            ---
            name: researcher
            ---
            """;
        Files.writeString(roleFile, content);

        assertThatThrownBy(() -> AgentRole.fromFile(roleFile))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("missing 'description' in frontmatter");
    }

    @Test
    @DisplayName("Should reject null path")
    void shouldRejectNullPath() {
        assertThatThrownBy(() -> AgentRole.fromFile(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Role file path cannot be null");
    }

    @Test
    @DisplayName("Should reject non-existent file")
    void shouldRejectNonExistentFile() {
        Path nonExistent = tempDir.resolve("does-not-exist.md");

        assertThatThrownBy(() -> AgentRole.fromFile(nonExistent))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Role file does not exist");
    }

    @Test
    @DisplayName("Should reject directory path")
    void shouldRejectDirectoryPath() {
        assertThatThrownBy(() -> AgentRole.fromFile(tempDir))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Path is not a regular file");
    }

    @Test
    @DisplayName("Should return null for tryFromFile with invalid file")
    void shouldReturnNullForTryFromFileWithInvalidFile() {
        Path nonExistent = tempDir.resolve("does-not-exist.md");

        AgentRole role = AgentRole.tryFromFile(nonExistent);

        assertThat(role).isNull();
    }

    @Test
    @DisplayName("Should return role for tryFromFile with valid file")
    void shouldReturnRoleForTryFromFileWithValidFile() throws IOException {
        Path roleFile = tempDir.resolve("ROLE.md");
        String content = """
            ---
            name: tester
            description: Tests software
            ---
            """;
        Files.writeString(roleFile, content);

        AgentRole role = AgentRole.tryFromFile(roleFile);

        assertThat(role).isNotNull();
        assertThat(role.getName()).isEqualTo("tester");
    }

    @Test
    @DisplayName("Should return lowercase hyphenated file name")
    void shouldReturnLowercaseHyphenatedFileName() throws IOException {
        Path roleFile = tempDir.resolve("ROLE.md");
        String content = """
            ---
            name: Game Rules Designer
            description: Designs game rules
            ---
            """;
        Files.writeString(roleFile, content);

        AgentRole role = AgentRole.fromFile(roleFile);

        assertThat(role.getFileName()).isEqualTo("game-rules-designer");
    }

    @Test
    @DisplayName("Should match role name case-insensitively")
    void shouldMatchRoleNameCaseInsensitively() throws IOException {
        Path roleFile = tempDir.resolve("ROLE.md");
        String content = """
            ---
            name: Researcher
            description: Researches
            ---
            """;
        Files.writeString(roleFile, content);

        AgentRole role = AgentRole.fromFile(roleFile);

        assertThat(role.matches("researcher")).isTrue();
        assertThat(role.matches("RESEARCHER")).isTrue();
        assertThat(role.matches("Researcher")).isTrue();
        assertThat(role.matches("logician")).isFalse();
    }

    @Test
    @DisplayName("Should have meaningful toString")
    void shouldHaveMeaningfulToString() throws IOException {
        Path roleFile = tempDir.resolve("ROLE.md");
        String content = """
            ---
            name: orchestrator
            description: Coordinates work
            ---
            """;
        Files.writeString(roleFile, content);

        AgentRole role = AgentRole.fromFile(roleFile);
        String toString = role.toString();

        assertThat(toString).contains("orchestrator");
        assertThat(toString).contains("Coordinates work");
        assertThat(toString).contains("ROLE.md");
    }

    @Test
    @DisplayName("Should implement equals and hashCode based on name")
    void shouldImplementEqualsAndHashCodeBasedOnName() throws IOException {
        Path roleFile1 = tempDir.resolve("role1.md");
        Path roleFile2 = tempDir.resolve("role2.md");

        String content1 = """
            ---
            name: researcher
            description: Description 1
            ---
            """;
        String content2 = """
            ---
            name: researcher
            description: Description 2
            ---
            """;
        String content3 = """
            ---
            name: logician
            description: Description 3
            ---
            """;

        Files.writeString(roleFile1, content1);
        Files.writeString(roleFile2, content2);
        Path roleFile3 = tempDir.resolve("role3.md");
        Files.writeString(roleFile3, content3);

        AgentRole role1 = AgentRole.fromFile(roleFile1);
        AgentRole role2 = AgentRole.fromFile(roleFile2);
        AgentRole role3 = AgentRole.fromFile(roleFile3);

        // Same name means equal, even if description and file are different
        assertThat(role1).isEqualTo(role2);
        assertThat(role1.hashCode()).isEqualTo(role2.hashCode());
        assertThat(role1).isNotEqualTo(role3);
    }

    @Test
    @DisplayName("Should handle multi-line frontmatter")
    void shouldHandleMultiLineFrontmatter() throws IOException {
        Path roleFile = tempDir.resolve("ROLE.md");
        String content = """
            ---
            name: art-designer
            description: Creates UX/UI designs, visual art, graphics, and media assets.
            other_field: Some value
            ---

            # Role Content
            """;
        Files.writeString(roleFile, content);

        AgentRole role = AgentRole.fromFile(roleFile);

        assertThat(role.getName()).isEqualTo("art-designer");
        assertThat(role.getDescription()).contains("UX/UI designs");
    }

    @Test
    @DisplayName("Should preserve full content including frontmatter")
    void shouldPreserveFullContentIncludingFrontmatter() throws IOException {
        Path roleFile = tempDir.resolve("ROLE.md");
        String content = """
            ---
            name: developer
            description: Writes code
            ---

            # Developer Role

            ## Responsibilities
            - Write code
            - Test code
            """;
        Files.writeString(roleFile, content);

        AgentRole role = AgentRole.fromFile(roleFile);

        assertThat(role.getFullContent()).isEqualTo(content);
        assertThat(role.getFullContent()).contains("---");
        assertThat(role.getFullContent()).contains("Responsibilities");
    }
}
