package com.taskmanager.task.dto.request;

import com.taskmanager.task.domain.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Request payload for creating a new task.
 */
public record CreateTaskRequest(
        @NotBlank(message = "Task title is mandatory")
        @Size(max = 255, message = "Task title must not exceed 255 characters")
        String title,

        @Size(max = 2000, message = "Task description must not exceed 2000 characters")
        String description,

        TaskPriority priority,

        Instant dueDate
) {
    public CreateTaskRequest {
        priority = priority != null ? priority : TaskPriority.MEDIUM;
    }
}

