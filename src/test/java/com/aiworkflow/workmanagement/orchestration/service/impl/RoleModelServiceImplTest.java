package com.aiworkflow.workmanagement.orchestration.service.impl;

import com.aiworkflow.workmanagement.orchestration.domain.RoleModelReport;
import com.aiworkflow.workmanagement.orchestration.service.RoleRegistry;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class RoleModelServiceImplTest {

    @Test
    void discoversRolesAndWritesReport() throws Exception {
        RoleRegistry registry = new InMemoryRoleRegistry();
        RoleModelServiceImpl service = new RoleModelServiceImpl(registry);

        Path rolesDir = Path.of(".agent", "roles");
        RoleModelReport report = service.discoverRoles(rolesDir);

        assertThat(report.getRoles()).isNotEmpty();
        assertThat(registry.getRoleCount()).isEqualTo(report.getRoles().size());
        assertThat(report.getReportPath()).isNotNull();
        assertThat(Files.exists(report.getReportPath())).isTrue();
        String reportContent = Files.readString(report.getReportPath());
        assertThat(reportContent).contains("Role Model Startup Report");
        assertThat(reportContent).contains("Execution Mode Notes");
    }
}
