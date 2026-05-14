package com.aiworkflow.workmanagement.api;

import com.aiworkflow.workmanagement.api.dto.TaskAssignRequest;
import com.aiworkflow.workmanagement.api.dto.TaskCreateRequest;
import com.aiworkflow.workmanagement.api.dto.TaskResponse;
import com.aiworkflow.workmanagement.api.dto.TaskTransitionRequest;
import com.aiworkflow.workmanagement.api.dto.TaskUpdateRequest;
import com.aiworkflow.workmanagement.application.command.CreateTaskCommand;
import com.aiworkflow.workmanagement.application.service.TaskService;
import com.aiworkflow.workmanagement.domain.model.TaskState;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.domain.valueobject.TaskId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST endpoints for tasks.
 */
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskCreateRequest request) {
        CreateTaskCommand command = new CreateTaskCommand(
            request.getTaskId(),
            request.getStoryId(),
            request.getTitle(),
            request.getDescription(),
            request.getAssignedTo(),
            request.getEstimatedHours()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(taskService.createTask(command)));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable String taskId) {
        return taskService.findById(new TaskId(taskId))
            .map(task -> ResponseEntity.ok(TaskResponse.from(task)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> queryTasks(
        @RequestParam String storyId,
        @RequestParam(required = false) TaskState state
    ) {
        List<com.aiworkflow.workmanagement.domain.model.Task> tasks = taskService.findByStoryId(new StoryId(storyId));
        if (state != null) {
            tasks = tasks.stream().filter(task -> task.getState() == state).collect(Collectors.toList());
        }
        return ResponseEntity.ok(tasks.stream().map(TaskResponse::from).collect(Collectors.toList()));
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
        @PathVariable String taskId,
        @Valid @RequestBody TaskUpdateRequest request
    ) {
        return ResponseEntity.ok(TaskResponse.from(
            taskService.updateTask(new TaskId(taskId), request.getTitle(), request.getDescription())
        ));
    }

    @PostMapping("/{taskId}/transition")
    public ResponseEntity<TaskResponse> transitionTask(
        @PathVariable String taskId,
        @Valid @RequestBody TaskTransitionRequest request
    ) {
        return ResponseEntity.ok(TaskResponse.from(
            taskService.transitionTask(new TaskId(taskId), request.getTargetState())
        ));
    }

    @PostMapping("/{taskId}/assign")
    public ResponseEntity<TaskResponse> assignTask(
        @PathVariable String taskId,
        @Valid @RequestBody TaskAssignRequest request
    ) {
        return ResponseEntity.ok(TaskResponse.from(
            taskService.assignTask(new TaskId(taskId), request.getAssignedTo())
        ));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable String taskId) {
        taskService.deleteTask(new TaskId(taskId));
        return ResponseEntity.noContent().build();
    }
}

