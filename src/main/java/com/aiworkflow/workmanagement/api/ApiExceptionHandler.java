package com.aiworkflow.workmanagement.api;

import com.aiworkflow.workmanagement.application.service.impl.CommentServiceImpl;
import com.aiworkflow.workmanagement.application.service.impl.StoryServiceImpl;
import com.aiworkflow.workmanagement.application.service.impl.TaskServiceImpl;
import com.aiworkflow.workmanagement.domain.service.InvalidStateTransitionException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.UUID;

/**
 * Maps exceptions to standard API error responses.
 */
@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({
        StoryServiceImpl.StoryNotFoundException.class,
        TaskServiceImpl.TaskNotFoundException.class,
        CommentServiceImpl.StoryNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, ex, request);
    }

    @ExceptionHandler({InvalidStateTransitionException.class})
    public ResponseEntity<ErrorResponse> handleConflict(InvalidStateTransitionException ex, HttpServletRequest request) {
        return buildError(HttpStatus.CONFLICT, ex, request);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleValidation(Exception ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, ex, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex, request);
    }

    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, Exception ex, HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-Id");
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        ErrorResponse payload = new ErrorResponse(
            Instant.now().toString(),
            status.value(),
            status.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI(),
            correlationId
        );
        return ResponseEntity.status(status).body(payload);
    }
}
