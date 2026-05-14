package com.aiworkflow.workmanagement.orchestration.service;

import com.aiworkflow.workmanagement.orchestration.domain.AgentRole;

import java.util.List;
import java.util.Optional;

/**
 * Registry for agent roles discovered from ROLE.md files.
 * <p>
 * This service is responsible for discovering agent roles from .agent/roles/ directory
 * ROLE.md files at startup, registering roles in a central registry, looking up roles
 * by name, and providing access to all available roles.
 * <p>
 * Implementation note: The actual role discovery and registration logic
 * will be implemented in STORY-001A (RoleModelService). This interface
 * defines the contract for the domain model.
 */
public interface RoleRegistry {

    /**
     * Registers an agent role.
     *
     * @param role the role to register
     * @throws IllegalArgumentException if a role with the same name already exists
     */
    void register(AgentRole role);

    /**
     * Looks up a role by name (case-insensitive).
     *
     * @param roleName the role name
     * @return an Optional containing the role if found, empty otherwise
     */
    Optional<AgentRole> findByName(String roleName);

    /**
     * Gets a role by name, throwing an exception if not found.
     *
     * @param roleName the role name
     * @return the agent role
     * @throws IllegalArgumentException if the role is not found
     */
    AgentRole getByName(String roleName);

    /**
     * Checks if a role exists.
     *
     * @param roleName the role name
     * @return true if the role exists in the registry
     */
    boolean exists(String roleName);

    /**
     * Gets all registered roles.
     *
     * @return an unmodifiable list of all roles
     */
    List<AgentRole> getAllRoles();

    /**
     * Gets the number of registered roles.
     *
     * @return the role count
     */
    int getRoleCount();

    /**
     * Clears all registered roles.
     * Primarily used for testing.
     */
    void clear();
}
