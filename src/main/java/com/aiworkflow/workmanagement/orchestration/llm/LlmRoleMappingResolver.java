package com.aiworkflow.workmanagement.orchestration.llm;

import com.aiworkflow.workmanagement.orchestration.domain.LlmRoleResolution;

/**
 * Resolves role-to-provider mapping with precedence rules.
 */
public interface LlmRoleMappingResolver {
    LlmRoleResolution resolve(String roleName);
}

