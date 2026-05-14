package com.aiworkflow.workmanagement.orchestration.service;

import com.aiworkflow.workmanagement.orchestration.domain.RoleModelReport;

import java.nio.file.Path;

/**
 * Discovers roles from ROLE.md files and generates a startup report.
 */
public interface RoleModelService {

    RoleModelReport discoverRoles(Path rolesDirectory);

    RoleModelReport discoverRoles();
}

