package com.aiworkflow.workmanagement.orchestration.service.impl;

import com.aiworkflow.workmanagement.orchestration.domain.AgentRole;
import com.aiworkflow.workmanagement.orchestration.service.RoleRegistry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of RoleRegistry.
 * <p>
 * This is a simple implementation that stores roles in a thread-safe map.
 * STORY-001A will enhance this with automatic role discovery and startup reporting.
 */
@Component
public class InMemoryRoleRegistry implements RoleRegistry {

    private final Map<String, AgentRole> roles = new ConcurrentHashMap<>();

    @Override
    public void register(AgentRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        String normalizedName = normalizeRoleName(role.getName());

        if (roles.containsKey(normalizedName)) {
            throw new IllegalArgumentException(
                String.format("Role '%s' is already registered", role.getName())
            );
        }

        roles.put(normalizedName, role);
    }

    @Override
    public Optional<AgentRole> findByName(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return Optional.empty();
        }

        String normalizedName = normalizeRoleName(roleName);
        return Optional.ofNullable(roles.get(normalizedName));
    }

    @Override
    public AgentRole getByName(String roleName) {
        return findByName(roleName)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Role '%s' not found in registry", roleName)
            ));
    }

    @Override
    public boolean exists(String roleName) {
        return findByName(roleName).isPresent();
    }

    @Override
    public List<AgentRole> getAllRoles() {
        return Collections.unmodifiableList(new ArrayList<>(roles.values()));
    }

    @Override
    public int getRoleCount() {
        return roles.size();
    }

    @Override
    public void clear() {
        roles.clear();
    }

    /**
     * Normalizes a role name for case-insensitive lookup.
     *
     * @param roleName the role name
     * @return normalized role name (lowercase)
     */
    private String normalizeRoleName(String roleName) {
        return roleName.toLowerCase().trim();
    }

    @Override
    public String toString() {
        return "InMemoryRoleRegistry{" +
            "roleCount=" + roles.size() +
            ", roles=" + roles.keySet() +
            '}';
    }
}
