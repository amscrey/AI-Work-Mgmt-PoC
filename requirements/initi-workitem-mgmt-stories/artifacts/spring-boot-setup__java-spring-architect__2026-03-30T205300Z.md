# Spring Boot Project Setup

**Role**: java-spring-architect
**Story**: STORY-012
**Status**: Complete

## Project Configuration

### Maven pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.3</version>
        <relativePath/>
    </parent>

    <groupId>com.aiworkflow</groupId>
    <artifactId>work-management-platform</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <name>AI Work Management Platform</name>
    <description>Filesystem-based work management platform for AI agents</description>

    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
    </properties>

    <dependencies>
        <!-- Spring Boot Core -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- YAML/JSON Processing -->
        <dependency>
            <groupId>com.fasterxml.jackson.dataformat</groupId>
            <artifactId>jackson-dataformat-yaml</artifactId>
        </dependency>

        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
        </dependency>

        <!-- File I/O Utilities -->
        <dependency>
            <groupId>commons-io</groupId>
            <artifactId>commons-io</artifactId>
            <version>2.15.1</version>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- Development Tools -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>

        <!-- Lombok (optional, for reducing boilerplate) -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.0.0</version>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## Main Application Class

**Location**: `src/main/java/com/aiworkflow/workmanagement/WorkManagementApplication.java`

```java
package com.aiworkflow.workmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WorkManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkManagementApplication.class, args);
    }
}
```

---

## Application Configuration

**Location**: `src/main/resources/application.yml`

```yaml
spring:
  application:
    name: work-management-platform

  jackson:
    serialization:
      write-dates-as-timestamps: false
      indent-output: true
    deserialization:
      fail-on-unknown-properties: false
    default-property-inclusion: non_null

server:
  port: 8080

# Workspace Configuration
workspace:
  root-path: ${WORKSPACE_ROOT:/tmp/ai-workspace}
  logs-directory: logs
  templates-directory: templates

  validation:
    enabled: true
    strict-mode: true

  logging:
    format: json-lines
    rotation: daily
    retention-days: 30

# Logging Configuration
logging:
  level:
    root: INFO
    com.aiworkflow.workmanagement: DEBUG
    org.springframework.boot: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
  file:
    name: ${workspace.root-path}/logs/application.log

# Management/Actuator (for monitoring)
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: when-authorized
```

---

## Project Directory Structure

```
work-management-platform/
├── pom.xml
├── README.md
├── .gitignore
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── aiworkflow/
│   │   │           └── workmanagement/
│   │   │               ├── WorkManagementApplication.java
│   │   │               ├── domain/                    # Domain layer
│   │   │               │   ├── model/
│   │   │               │   │   ├── Story.java
│   │   │               │   │   ├── Task.java
│   │   │               │   │   ├── Artifact.java
│   │   │               │   │   └── Comment.java
│   │   │               │   ├── valueobject/
│   │   │               │   │   ├── StoryId.java
│   │   │               │   │   ├── TaskId.java
│   │   │               │   │   └── WorkflowState.java
│   │   │               │   ├── repository/
│   │   │               │   │   ├── StoryRepository.java
│   │   │               │   │   └── TaskRepository.java
│   │   │               │   └── service/
│   │   │               │       └── StateTransitionValidator.java
│   │   │               ├── application/                # Application layer
│   │   │               │   ├── service/
│   │   │               │   │   ├── StoryService.java
│   │   │               │   │   ├── TaskService.java
│   │   │               │   │   └── ValidationService.java
│   │   │               │   └── command/
│   │   │               │       ├── CreateStoryCommand.java
│   │   │               │       └── UpdateStoryCommand.java
│   │   │               ├── infrastructure/             # Infrastructure layer
│   │   │               │   ├── persistence/
│   │   │               │   │   ├── FileSystemStoryRepository.java
│   │   │               │   │   └── PathResolver.java
│   │   │               │   ├── logging/
│   │   │               │   │   ├── ActivityLogger.java
│   │   │               │   │   └── JSONLinesWriter.java
│   │   │               │   └── validation/
│   │   │               │       ├── StoryDirectoryNameValidator.java
│   │   │               │       └── ArtifactFileNameValidator.java
│   │   │               ├── presentation/               # Presentation layer
│   │   │               │   └── api/
│   │   │               │       ├── StoryController.java
│   │   │               │       └── dto/
│   │   │               │           └── StoryDTO.java
│   │   │               └── config/                     # Configuration
│   │   │                   ├── WorkspaceConfig.java
│   │   │                   └── ValidationConfig.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── logback-spring.xml
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── aiworkflow/
│       │           └── workmanagement/
│       │               ├── WorkManagementApplicationTests.java
│       │               ├── domain/
│       │               │   └── model/
│       │               │       └── StoryTest.java
│       │               ├── application/
│       │               │   └── service/
│       │               │       └── StoryServiceTest.java
│       │               └── infrastructure/
│       │                   └── persistence/
│       │                       └── FileSystemStoryRepositoryTest.java
│       └── resources/
│           ├── application-test.yml
│           └── fixtures/
│               └── test-workspace/
```

---

## .gitignore

```gitignore
# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties

# IDE
.idea/
*.iml
.vscode/
.classpath
.project
.settings/

# Logs
*.log
logs/

# OS
.DS_Store
Thumbs.db

# Workspace (external)
/workspace/
```

---

## README.md

```markdown
# AI Work Management Platform

Filesystem-based work management platform enabling AI agents to collaborate on software projects.

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- External workspace directory

### Build
```bash
mvn clean install
```

### Run
```bash
export WORKSPACE_ROOT=/path/to/external/workspace
mvn spring-boot:run
```

### Test
```bash
mvn test
```

## Configuration

Set the workspace root via:
1. Environment variable: `WORKSPACE_ROOT=/path/to/workspace`
2. Application property: `workspace.root-path=/path/to/workspace`
3. Default: `/tmp/ai-workspace`

## Architecture

- **Domain Layer**: Entities, value objects, repository interfaces (no framework dependencies)
- **Application Layer**: Services, commands, use cases
- **Infrastructure Layer**: Filesystem persistence, logging, validators
- **Presentation Layer**: REST API (future)
- **Configuration**: Spring Boot configuration, workspace setup

## Testing Strategy

- **Unit Tests**: Domain logic, service layer (with mocks)
- **Integration Tests**: Repository layer (with @TempDir)
- **Test Coverage Target**: 80%+ for domain and application layers

## External Workspace Structure

```
workspace/
├── workspace-config.yml
├── backlog/
├── todo/
├── in-progress/
├── awaiting-approval/
├── done/
├── logs/
└── templates/
```

See [Workspace Documentation](docs/workspace-structure.md) for details.
```

---

## Dependency Justification

| Dependency | Purpose | Layer |
|------------|---------|-------|
| spring-boot-starter | Core Spring Boot functionality | All |
| spring-boot-starter-web | REST API (future), embedded server | Presentation |
| spring-boot-starter-validation | Bean validation (@Valid, @NotNull) | Application |
| jackson-dataformat-yaml | Parse YAML frontmatter in story/task files | Infrastructure |
| jackson-datatype-jsr310 | Java 8+ date/time serialization | Infrastructure |
| commons-io | File utilities (FileUtils, IOUtils) | Infrastructure |
| spring-boot-starter-test | JUnit 5, Spring Test | Test |
| assertj-core | Fluent assertions | Test |
| mockito-core | Mocking framework | Test |
| lombok | Reduce boilerplate (optional) | All |

---

## Build & Run Commands

```bash
# Build
mvn clean package

# Run
java -jar target/work-management-platform-0.1.0-SNAPSHOT.jar

# Run with custom workspace
java -jar target/work-management-platform-0.1.0-SNAPSHOT.jar \
  --workspace.root-path=/path/to/workspace

# Run tests
mvn test

# Run specific test
mvn test -Dtest=StoryServiceTest

# Run with dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Generate coverage report
mvn verify
```

---

## Next Steps After Setup

1. **Create domain entities** (Story, Task, Artifact, Comment)
2. **Implement repository interfaces** (FileSystemStoryRepository)
3. **Implement service layer** (StoryService, ValidationService)
4. **Write tests** (unit tests, integration tests)
5. **Implement validation** (naming validators, state machine)
6. **Implement logging** (ActivityLogger, JSONLinesWriter)

---

## Summary

- **Spring Boot 3.2.3** with Java 21
- **Maven** build system
- **Complete dependency set** for filesystem-based operations
- **Clean architecture** package structure ready
- **Testing infrastructure** with JUnit 5, AssertJ, Mockito
- **Configuration** via application.yml with environment variable support
- **Ready for implementation** of Phase 1 domain and infrastructure code
