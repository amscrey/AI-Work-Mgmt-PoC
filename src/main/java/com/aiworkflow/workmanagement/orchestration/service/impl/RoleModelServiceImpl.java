package com.aiworkflow.workmanagement.orchestration.service.impl;

import com.aiworkflow.workmanagement.orchestration.domain.AgentRole;
import com.aiworkflow.workmanagement.orchestration.domain.RoleModelReport;
import com.aiworkflow.workmanagement.orchestration.domain.WorkTypeAssociations;
import com.aiworkflow.workmanagement.orchestration.service.RoleModelService;
import com.aiworkflow.workmanagement.orchestration.service.RoleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Discovers roles at startup and writes a startup report.
 */
@Component
public class RoleModelServiceImpl implements RoleModelService, ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(RoleModelServiceImpl.class);
    private static final Path DEFAULT_ROLES_DIR = Path.of(".agent", "roles");
    private static final Path REPORT_PATH = Path.of("logs", "role-model-startup-report.md");

    private final RoleRegistry roleRegistry;
    private final RoleFileParser roleFileParser;
    private final RoleStartupReportWriter reportWriter;

    public RoleModelServiceImpl(RoleRegistry roleRegistry) {
        this.roleRegistry = roleRegistry;
        this.roleFileParser = new RoleFileParser();
        this.reportWriter = new RoleStartupReportWriter();
    }

    @Override
    public RoleModelReport discoverRoles() {
        return discoverRoles(DEFAULT_ROLES_DIR);
    }

    @Override
    public RoleModelReport discoverRoles(Path rolesDirectory) {
        List<String> warnings = new ArrayList<>();
        List<RoleFileParser.RoleKeywords> parsedRoles = new ArrayList<>();

        if (rolesDirectory == null || !Files.exists(rolesDirectory)) {
            warnings.add("Roles directory not found: " + rolesDirectory);
            return new RoleModelReport(List.of(), Map.of(), new WorkTypeAssociations(Map.of()), warnings, REPORT_PATH);
        }

        try {
            try (var stream = Files.walk(rolesDirectory)) {
                List<Path> roleFiles = stream
                    .filter(path -> path.getFileName().toString().equalsIgnoreCase("ROLE.md"))
                    .sorted()
                    .toList();

                for (Path roleFile : roleFiles) {
                    RoleFileParser.RoleKeywords roleKeywords = roleFileParser.parse(roleFile);
                    parsedRoles.add(roleKeywords);
                    roleRegistry.register(roleKeywords.role());
                }
            }
        } catch (IOException e) {
            warnings.add("Failed to scan roles: " + e.getMessage());
        }

        Map<String, List<String>> keywordMap = new HashMap<>();
        for (RoleFileParser.RoleKeywords entry : parsedRoles) {
            AgentRole role = entry.role();
            keywordMap.put(role.getName(), entry.keywords());
        }

        List<RoleFileParser.RoleKeywords> sortedRoles = parsedRoles.stream()
            .sorted((a, b) -> a.role().getName().compareToIgnoreCase(b.role().getName()))
            .toList();

        WorkTypeAssociations associations = WorkTypeAssociations.fromRoleKeywords(keywordMap);
        RoleModelReport report = new RoleModelReport(
            sortedRoles.stream().map(RoleFileParser.RoleKeywords::role).collect(Collectors.toList()),
            keywordMap,
            associations,
            warnings,
            REPORT_PATH
        );

        try {
            reportWriter.writeReport(report);
        } catch (IOException e) {
            logger.warn("Failed to write role model report: {}", e.getMessage());
        }

        return report;
    }

    @Override
    public void run(ApplicationArguments args) {
        logger.info("Starting role discovery for RoleModelService");
        discoverRoles();
    }
}
