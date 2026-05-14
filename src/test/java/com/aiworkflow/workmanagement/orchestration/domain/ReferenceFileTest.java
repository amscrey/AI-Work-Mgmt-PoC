package com.aiworkflow.workmanagement.orchestration.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ReferenceFile Tests")
class ReferenceFileTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should create reference file from valid markdown file")
    void shouldCreateReferenceFileFromValidMarkdownFile() throws IOException {
        // Create temporary markdown file
        Path markdownFile = tempDir.resolve("reference.md");
        String content = "# Reference Document\n\nSome content here.";
        Files.writeString(markdownFile, content);

        ReferenceFile refFile = ReferenceFile.fromFile(markdownFile);

        assertThat(refFile.getFileName()).isEqualTo("reference.md");
        assertThat(refFile.getContent()).isEqualTo(content);
        assertThat(refFile.getFilePath()).isEqualTo(markdownFile);
        assertThat(refFile.getFileSize()).isEqualTo(content.getBytes().length);
        assertThat(refFile.getLastModified()).isNotNull();
    }

    @Test
    @DisplayName("Should identify markdown files correctly")
    void shouldIdentifyMarkdownFilesCorrectly() throws IOException {
        Path markdownFile = tempDir.resolve("doc.md");
        Files.writeString(markdownFile, "content");

        ReferenceFile refFile = ReferenceFile.fromFile(markdownFile);

        assertThat(refFile.isMarkdown()).isTrue();
        assertThat(refFile.getExtension()).isEqualTo("md");
    }

    @Test
    @DisplayName("Should identify non-markdown files correctly")
    void shouldIdentifyNonMarkdownFilesCorrectly() throws IOException {
        Path txtFile = tempDir.resolve("doc.txt");
        Files.writeString(txtFile, "content");

        ReferenceFile refFile = ReferenceFile.fromFile(txtFile);

        assertThat(refFile.isMarkdown()).isFalse();
        assertThat(refFile.getExtension()).isEqualTo("txt");
    }

    @Test
    @DisplayName("Should handle files with no extension")
    void shouldHandleFilesWithNoExtension() throws IOException {
        Path noExtFile = tempDir.resolve("README");
        Files.writeString(noExtFile, "content");

        ReferenceFile refFile = ReferenceFile.fromFile(noExtFile);

        assertThat(refFile.getExtension()).isEmpty();
        assertThat(refFile.isMarkdown()).isFalse();
    }

    @Test
    @DisplayName("Should reject null path")
    void shouldRejectNullPath() {
        assertThatThrownBy(() -> ReferenceFile.fromFile(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("File path cannot be null");
    }

    @Test
    @DisplayName("Should reject non-existent file")
    void shouldRejectNonExistentFile() {
        Path nonExistent = tempDir.resolve("does-not-exist.md");

        assertThatThrownBy(() -> ReferenceFile.fromFile(nonExistent))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("File does not exist");
    }

    @Test
    @DisplayName("Should reject directory path")
    void shouldRejectDirectoryPath() {
        assertThatThrownBy(() -> ReferenceFile.fromFile(tempDir))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Path is not a regular file");
    }

    @Test
    @DisplayName("Should return null for tryFromFile with invalid path")
    void shouldReturnNullForTryFromFileWithInvalidPath() {
        Path nonExistent = tempDir.resolve("does-not-exist.md");

        ReferenceFile refFile = ReferenceFile.tryFromFile(nonExistent);

        assertThat(refFile).isNull();
    }

    @Test
    @DisplayName("Should return reference file for tryFromFile with valid path")
    void shouldReturnReferenceFileForTryFromFileWithValidPath() throws IOException {
        Path markdownFile = tempDir.resolve("valid.md");
        Files.writeString(markdownFile, "content");

        ReferenceFile refFile = ReferenceFile.tryFromFile(markdownFile);

        assertThat(refFile).isNotNull();
        assertThat(refFile.getFileName()).isEqualTo("valid.md");
    }

    @Test
    @DisplayName("Should format file size correctly")
    void shouldFormatFileSizeCorrectly() throws IOException {
        // Small file (bytes)
        Path smallFile = tempDir.resolve("small.md");
        Files.writeString(smallFile, "Hi");
        ReferenceFile small = ReferenceFile.fromFile(smallFile);
        assertThat(small.getFormattedSize()).contains("bytes");

        // Medium file (KB)
        Path mediumFile = tempDir.resolve("medium.md");
        Files.writeString(mediumFile, "x".repeat(2048));
        ReferenceFile medium = ReferenceFile.fromFile(mediumFile);
        assertThat(medium.getFormattedSize()).contains("KB");

        // Large file (MB)
        Path largeFile = tempDir.resolve("large.md");
        Files.writeString(largeFile, "x".repeat(1024 * 1024 * 2));
        ReferenceFile large = ReferenceFile.fromFile(largeFile);
        assertThat(large.getFormattedSize()).contains("MB");
    }

    @Test
    @DisplayName("Should provide meaningful summary")
    void shouldProvideMeaningSummary() throws IOException {
        Path file = tempDir.resolve("summary-test.md");
        Files.writeString(file, "content");

        ReferenceFile refFile = ReferenceFile.fromFile(file);
        String summary = refFile.getSummary();

        assertThat(summary).contains("summary-test.md");
        assertThat(summary).contains("bytes");
        assertThat(summary).contains("modified");
    }

    @Test
    @DisplayName("Should have meaningful toString")
    void shouldHaveMeaningfulToString() throws IOException {
        Path file = tempDir.resolve("test.md");
        Files.writeString(file, "content");

        ReferenceFile refFile = ReferenceFile.fromFile(file);
        String toString = refFile.toString();

        assertThat(toString).contains("test.md");
        assertThat(toString).contains("fileSize");
        assertThat(toString).contains("lastModified");
    }

    @Test
    @DisplayName("Should implement equals and hashCode based on file path")
    void shouldImplementEqualsAndHashCodeBasedOnFilePath() throws IOException {
        Path file1 = tempDir.resolve("file1.md");
        Path file2 = tempDir.resolve("file2.md");
        Files.writeString(file1, "content1");
        Files.writeString(file2, "content2");

        ReferenceFile ref1a = ReferenceFile.fromFile(file1);
        ReferenceFile ref1b = ReferenceFile.fromFile(file1);
        ReferenceFile ref2 = ReferenceFile.fromFile(file2);

        assertThat(ref1a).isEqualTo(ref1b);
        assertThat(ref1a.hashCode()).isEqualTo(ref1b.hashCode());
        assertThat(ref1a).isNotEqualTo(ref2);
    }

    @Test
    @DisplayName("Should handle empty file")
    void shouldHandleEmptyFile() throws IOException {
        Path emptyFile = tempDir.resolve("empty.md");
        Files.writeString(emptyFile, "");

        ReferenceFile refFile = ReferenceFile.fromFile(emptyFile);

        assertThat(refFile.getContent()).isEmpty();
        assertThat(refFile.getFileSize()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle large files")
    void shouldHandleLargeFiles() throws IOException {
        Path largeFile = tempDir.resolve("large.md");
        String largeContent = "x".repeat(100000);
        Files.writeString(largeFile, largeContent);

        ReferenceFile refFile = ReferenceFile.fromFile(largeFile);

        assertThat(refFile.getContent()).hasSize(100000);
        assertThat(refFile.getFileSize()).isEqualTo(100000);
    }
}
