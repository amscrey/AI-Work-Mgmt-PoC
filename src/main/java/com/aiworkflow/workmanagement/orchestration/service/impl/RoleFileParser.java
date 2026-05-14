package com.aiworkflow.workmanagement.orchestration.service.impl;

import com.aiworkflow.workmanagement.orchestration.domain.AgentRole;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses ROLE.md files to extract keywords from Responsibilities and Skills sections.
 */
public class RoleFileParser {

    private static final Pattern SECTION_PATTERN = Pattern.compile("^##\\s+(Responsibilities|Skills Used)\\s*$", Pattern.MULTILINE);
    private static final Pattern BULLET_PATTERN = Pattern.compile("^[-*]\\s+(.+)$", Pattern.MULTILINE);
    private static final Set<String> STOP_WORDS = Set.of(
        "the", "and", "or", "to", "a", "an", "of", "for", "with", "in", "on", "by", "be", "is", "are",
        "that", "this", "as", "from", "at", "it", "into", "when", "use", "using", "used"
    );

    public RoleKeywords parse(Path roleFilePath) throws IOException {
        AgentRole role = AgentRole.fromFile(roleFilePath);
        String content = role.getFullContent();
        List<String> keywords = extractKeywords(content);
        return new RoleKeywords(role, keywords);
    }

    private List<String> extractKeywords(String content) {
        Set<String> keywords = new LinkedHashSet<>();
        Matcher sectionMatcher = SECTION_PATTERN.matcher(content);

        while (sectionMatcher.find()) {
            int sectionStart = sectionMatcher.end();
            int nextSection = findNextSectionStart(content, sectionStart);
            String sectionBody = content.substring(sectionStart, nextSection);
            Matcher bulletMatcher = BULLET_PATTERN.matcher(sectionBody);
            while (bulletMatcher.find()) {
                String line = bulletMatcher.group(1);
                keywords.addAll(splitKeywords(line));
            }
        }

        return new ArrayList<>(new TreeSet<>(keywords));
    }

    private int findNextSectionStart(String content, int fromIndex) {
        Matcher matcher = SECTION_PATTERN.matcher(content);
        if (matcher.find(fromIndex)) {
            return matcher.start();
        }
        return content.length();
    }

    private List<String> splitKeywords(String line) {
        String[] tokens = line.toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9\\s-]", " ")
            .split("\\s+");
        List<String> results = new ArrayList<>();
        for (String token : tokens) {
            if (token.isBlank() || token.length() < 3 || STOP_WORDS.contains(token)) {
                continue;
            }
            results.add(token);
        }
        return results;
    }

    public record RoleKeywords(AgentRole role, List<String> keywords) {}
}
