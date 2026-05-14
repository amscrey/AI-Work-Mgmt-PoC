package com.aiworkflow.workmanagement.api;

import com.aiworkflow.workmanagement.api.dto.TaskCreateRequest;
import com.aiworkflow.workmanagement.application.command.CreateTaskCommand;
import com.aiworkflow.workmanagement.application.service.TaskService;
import com.aiworkflow.workmanagement.domain.model.Task;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
import com.aiworkflow.workmanagement.domain.valueobject.TaskId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @Test
    void createsTask() throws Exception {
        Task task = new Task(new TaskId("TASK-1"), new StoryId("STORY-1"), "Task");
        when(taskService.createTask(any(CreateTaskCommand.class))).thenReturn(task);

        TaskCreateRequest request = new TaskCreateRequest();
        request.setTaskId("TASK-1");
        request.setStoryId("STORY-1");
        request.setTitle("Task");

        mockMvc.perform(post("/api/v1/tasks")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.taskId").value("TASK-1"));
    }

    @Test
    void queriesTasksByStory() throws Exception {
        Task task = new Task(new TaskId("TASK-2"), new StoryId("STORY-2"), "Task");
        when(taskService.findByStoryId(new StoryId("STORY-2"))).thenReturn(List.of(task));

        mockMvc.perform(get("/api/v1/tasks").param("storyId", "STORY-2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].taskId").value("TASK-2"));
    }
}

