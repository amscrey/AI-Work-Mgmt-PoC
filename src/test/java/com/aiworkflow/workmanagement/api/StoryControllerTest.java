package com.aiworkflow.workmanagement.api;

import com.aiworkflow.workmanagement.api.dto.StoryCreateRequest;
import com.aiworkflow.workmanagement.application.command.CreateStoryCommand;
import com.aiworkflow.workmanagement.application.service.StoryService;
import com.aiworkflow.workmanagement.domain.model.PrioritizationState;
import com.aiworkflow.workmanagement.domain.model.Story;
import com.aiworkflow.workmanagement.domain.model.WorkflowState;
import com.aiworkflow.workmanagement.domain.valueobject.StoryId;
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

@WebMvcTest(StoryController.class)
class StoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StoryService storyService;

    @Test
    void createsStory() throws Exception {
        Story story = new Story(new StoryId("STORY-100"), "Title", "Summary", WorkflowState.TODO, PrioritizationState.BACKLOG);
        when(storyService.createStory(any(CreateStoryCommand.class))).thenReturn(story);

        StoryCreateRequest request = new StoryCreateRequest();
        request.setStoryId("STORY-100");
        request.setTitle("Title");
        request.setSummary("Summary");
        request.setAuthor("agent");
        request.setPrioritization(PrioritizationState.BACKLOG);

        mockMvc.perform(post("/api/v1/stories")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.storyId").value("STORY-100"));
    }

    @Test
    void queriesByState() throws Exception {
        Story story = new Story(new StoryId("STORY-200"), "Title", "Summary", WorkflowState.TODO, PrioritizationState.BACKLOG);
        when(storyService.findByState(WorkflowState.TODO)).thenReturn(List.of(story));

        mockMvc.perform(get("/api/v1/stories").param("state", "TODO"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].storyId").value("STORY-200"));
    }
}

