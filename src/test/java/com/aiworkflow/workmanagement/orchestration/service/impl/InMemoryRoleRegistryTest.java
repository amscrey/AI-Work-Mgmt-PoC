package com.aiworkflow.workmanagement.orchestration.service.impl;

import com.aiworkflow.workmanagement.orchestration.domain.AgentRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("InMemoryRoleRegistry Tests")
class InMemoryRoleRegistryTest {

    @TempDir
    Path tempDir;

    private InMemoryRoleRegistry registry;
    private AgentRole researcherRole;
    private AgentRole logicianRole;

    @BeforeEach
    void setUp() throws IOException {
        registry = new InMemoryRoleRegistry();

        // Create researcher role
        Path researcherFile = tempDir.resolve("researcher.md");
        Files.writeString(researcherFile, """
            ---
            name: researcher
            description: Conducts research
            ---
            """);
        researcherRole = AgentRole.fromFile(researcherFile);

        // Create logician role
        Path logicianFile = tempDir.resolve("logician.md");
        Files.writeString(logicianFile, """
            ---
            name: logician
            description: Solves logical problems
            ---
            """);
        logicianRole = AgentRole.fromFile(logicianFile);
    }

    @Test
    @DisplayName("Should start with empty registry")
    void shouldStartWithEmptyRegistry() {
        assertThat(registry.getRoleCount()).isEqualTo(0);
        assertThat(registry.getAllRoles()).isEmpty();
    }

    @Test
    @DisplayName("Should register role successfully")
    void shouldRegisterRoleSuccessfully() {
        registry.register(researcherRole);

        assertThat(registry.getRoleCount()).isEqualTo(1);
        assertThat(registry.exists("researcher")).isTrue();
    }

    @Test
    @DisplayName("Should find role by exact name")
    void shouldFindRoleByExactName() {
        registry.register(researcherRole);

        Optional<AgentRole> found = registry.findByName("researcher");

        assertThat(found).isPresent();
        assertThat(found.get()).isEqualTo(researcherRole);
    }

    @Test
    @DisplayName("Should find role case-insensitively")
    void shouldFindRoleCaseInsensitively() {
        registry.register(researcherRole);

        assertThat(registry.findByName("researcher")).isPresent();
        assertThat(registry.findByName("RESEARCHER")).isPresent();
        assertThat(registry.findByName("Researcher")).isPresent();
        assertThat(registry.findByName("ReSeArChEr")).isPresent();
    }

    @Test
    @DisplayName("Should return empty optional for non-existent role")
    void shouldReturnEmptyOptionalForNonExistentRole() {
        Optional<AgentRole> found = registry.findByName("non-existent");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should get role by name")
    void shouldGetRoleByName() {
        registry.register(researcherRole);

        AgentRole found = registry.getByName("researcher");

        assertThat(found).isEqualTo(researcherRole);
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent role")
    void shouldThrowExceptionWhenGettingNonExistentRole() {
        assertThatThrownBy(() -> registry.getByName("non-existent"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Role 'non-existent' not found in registry");
    }

    @Test
    @DisplayName("Should check if role exists")
    void shouldCheckIfRoleExists() {
        registry.register(researcherRole);

        assertThat(registry.exists("researcher")).isTrue();
        assertThat(registry.exists("RESEARCHER")).isTrue();
        assertThat(registry.exists("logician")).isFalse();
    }

    @Test
    @DisplayName("Should reject duplicate role registration")
    void shouldRejectDuplicateRoleRegistration() {
        registry.register(researcherRole);

        assertThatThrownBy(() -> registry.register(researcherRole))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Role 'researcher' is already registered");
    }

    @Test
    @DisplayName("Should reject null role registration")
    void shouldRejectNullRoleRegistration() {
        assertThatThrownBy(() -> registry.register(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Role cannot be null");
    }

    @Test
    @DisplayName("Should return all registered roles")
    void shouldReturnAllRegisteredRoles() {
        registry.register(researcherRole);
        registry.register(logicianRole);

        List<AgentRole> allRoles = registry.getAllRoles();

        assertThat(allRoles).hasSize(2);
        assertThat(allRoles).contains(researcherRole, logicianRole);
    }

    @Test
    @DisplayName("Should return unmodifiable list of roles")
    void shouldReturnUnmodifiableListOfRoles() {
        registry.register(researcherRole);

        List<AgentRole> allRoles = registry.getAllRoles();

        assertThatThrownBy(() -> allRoles.add(logicianRole))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Should return correct role count")
    void shouldReturnCorrectRoleCount() {
        assertThat(registry.getRoleCount()).isEqualTo(0);

        registry.register(researcherRole);
        assertThat(registry.getRoleCount()).isEqualTo(1);

        registry.register(logicianRole);
        assertThat(registry.getRoleCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should clear all roles")
    void shouldClearAllRoles() {
        registry.register(researcherRole);
        registry.register(logicianRole);

        registry.clear();

        assertThat(registry.getRoleCount()).isEqualTo(0);
        assertThat(registry.getAllRoles()).isEmpty();
        assertThat(registry.exists("researcher")).isFalse();
    }

    @Test
    @DisplayName("Should handle null role name in findByName")
    void shouldHandleNullRoleNameInFindByName() {
        Optional<AgentRole> found = registry.findByName(null);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should handle blank role name in findByName")
    void shouldHandleBlankRoleNameInFindByName() {
        Optional<AgentRole> found = registry.findByName("   ");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should trim role names when registering and searching")
    void shouldTrimRoleNamesWhenRegisteringAndSearching() {
        registry.register(researcherRole);

        assertThat(registry.findByName("  researcher  ")).isPresent();
        assertThat(registry.exists("  researcher  ")).isTrue();
    }

    @Test
    @DisplayName("Should have meaningful toString")
    void shouldHaveMeaningfulToString() {
        registry.register(researcherRole);
        registry.register(logicianRole);

        String toString = registry.toString();

        assertThat(toString).contains("roleCount=2");
        assertThat(toString).contains("roles");
    }

    @Test
    @DisplayName("Should handle rapid concurrent registrations")
    void shouldHandleRapidConcurrentRegistrations() throws IOException, InterruptedException {
        // Create multiple roles
        AgentRole[] roles = new AgentRole[10];
        for (int i = 0; i < 10; i++) {
            Path roleFile = tempDir.resolve("role" + i + ".md");
            Files.writeString(roleFile, String.format("""
                ---
                name: role%d
                description: Role %d
                ---
                """, i, i));
            roles[i] = AgentRole.fromFile(roleFile);
        }

        // Register roles concurrently
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            final int index = i;
            threads[i] = new Thread(() -> registry.register(roles[index]));
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // All roles should be registered
        assertThat(registry.getRoleCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should allow re-registration after clear")
    void shouldAllowReRegistrationAfterClear() {
        registry.register(researcherRole);
        registry.clear();
        registry.register(researcherRole);

        assertThat(registry.getRoleCount()).isEqualTo(1);
        assertThat(registry.exists("researcher")).isTrue();
    }
}
