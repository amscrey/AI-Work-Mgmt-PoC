package com.aiworkflow.workmanagement.config;

import com.aiworkflow.workmanagement.application.service.ActivityLogger;
import com.aiworkflow.workmanagement.application.service.CommentService;
import com.aiworkflow.workmanagement.application.service.StoryService;
import com.aiworkflow.workmanagement.application.service.TaskService;
import com.aiworkflow.workmanagement.application.service.impl.CommentServiceImpl;
import com.aiworkflow.workmanagement.application.service.impl.StoryServiceImpl;
import com.aiworkflow.workmanagement.application.service.impl.TaskServiceImpl;
import com.aiworkflow.workmanagement.domain.model.WorkspaceConfig;
import com.aiworkflow.workmanagement.domain.repository.CommentRepository;
import com.aiworkflow.workmanagement.domain.repository.StoryRepository;
import com.aiworkflow.workmanagement.domain.repository.TaskRepository;
import com.aiworkflow.workmanagement.domain.service.StateTransitionValidator;
import com.aiworkflow.workmanagement.infrastructure.persistence.FileBasedActivityLogger;
import com.aiworkflow.workmanagement.infrastructure.persistence.FileBasedCommentRepository;
import com.aiworkflow.workmanagement.infrastructure.persistence.FileBasedStoryRepository;
import com.aiworkflow.workmanagement.infrastructure.persistence.FileBasedTaskRepository;
import com.aiworkflow.workmanagement.orchestration.service.UsageBlockFormatter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WorkManagementConfig {

    @Bean
    public WorkspaceConfig workspaceConfig(
        @Value("${workmanagement.workspace-root:AI-Work-Mgmt-LLM-Orch-AddOn}") String workspaceRoot,
        @Value("${workmanagement.workspace-name:workspace}") String workspaceName
    ) {
        String normalizedName = workspaceName == null || workspaceName.isBlank() ? "workspace" : workspaceName.trim();
        Path root = Paths.get(workspaceRoot).resolve(normalizedName).toAbsolutePath().normalize();
        return new WorkspaceConfig(root, normalizedName);
    }

    @Bean
    public StoryRepository storyRepository(WorkspaceConfig workspaceConfig) {
        return new FileBasedStoryRepository(workspaceConfig.getWorkspaceRoot().toString());
    }

    @Bean
    public TaskRepository taskRepository(WorkspaceConfig workspaceConfig) {
        return new FileBasedTaskRepository(workspaceConfig.getWorkspaceRoot().toString());
    }

    @Bean
    public CommentRepository commentRepository(WorkspaceConfig workspaceConfig) {
        return new FileBasedCommentRepository(workspaceConfig.getWorkspaceRoot().toString());
    }

    @Bean
    public ActivityLogger activityLogger(WorkspaceConfig workspaceConfig) {
        return new FileBasedActivityLogger(workspaceConfig.getWorkspaceRoot().toString());
    }

    @Bean
    public StateTransitionValidator stateTransitionValidator() {
        return new StateTransitionValidator();
    }

    @Bean
    public StoryService storyService(
        StoryRepository storyRepository,
        StateTransitionValidator stateTransitionValidator,
        ActivityLogger activityLogger
    ) {
        return new StoryServiceImpl(storyRepository, stateTransitionValidator, activityLogger);
    }

    @Bean
    public TaskService taskService(
        TaskRepository taskRepository,
        StoryRepository storyRepository,
        ActivityLogger activityLogger
    ) {
        return new TaskServiceImpl(taskRepository, storyRepository, activityLogger);
    }

    @Bean
    public CommentService commentService(
        CommentRepository commentRepository,
        StoryRepository storyRepository,
        ActivityLogger activityLogger,
        ObjectProvider<UsageBlockFormatter> usageBlockFormatter
    ) {
        return new CommentServiceImpl(
            commentRepository,
            storyRepository,
            activityLogger,
            usageBlockFormatter.getIfAvailable()
        );
    }
}
