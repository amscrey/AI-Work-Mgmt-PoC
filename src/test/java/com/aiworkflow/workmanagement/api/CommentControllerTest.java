package com.aiworkflow.workmanagement.api;

import com.aiworkflow.workmanagement.api.dto.CommentCreateRequest;
import com.aiworkflow.workmanagement.application.command.CreateCommentCommand;
import com.aiworkflow.workmanagement.application.service.CommentService;
import com.aiworkflow.workmanagement.domain.model.Comment;
import com.aiworkflow.workmanagement.domain.model.CommentType;
import com.aiworkflow.workmanagement.domain.valueobject.CommentId;
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

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @Test
    void createsComment() throws Exception {
        Comment comment = new Comment(new CommentId("comment__agent__2026-04-05T000000Z.md"), new StoryId("STORY-1"), "agent", "content", CommentType.NOTE);
        when(commentService.createComment(any(CreateCommentCommand.class))).thenReturn(comment);

        CommentCreateRequest request = new CommentCreateRequest();
        request.setCommentId("comment__agent__2026-04-05T000000Z.md");
        request.setStoryId("STORY-1");
        request.setAuthor("agent");
        request.setType(CommentType.NOTE);
        request.setContent("content");

        mockMvc.perform(post("/api/v1/comments")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.commentId").value("comment__agent__2026-04-05T000000Z.md"));
    }

    @Test
    void listsCommentsByStory() throws Exception {
        Comment comment = new Comment(new CommentId("comment__agent__2026-04-05T000000Z.md"), new StoryId("STORY-2"), "agent", "content", CommentType.NOTE);
        when(commentService.findByStoryId(new StoryId("STORY-2"))).thenReturn(List.of(comment));

        mockMvc.perform(get("/api/v1/stories/STORY-2/comments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].commentId").value("comment__agent__2026-04-05T000000Z.md"));
    }
}

