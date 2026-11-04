package com.taskmanager.task.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.taskmanager.task.domain.TaskPriority;
import com.taskmanager.task.domain.TaskStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Standard Task response projection DTO.
 */
public record TaskResponse(
        UUID id,
        UUID userId,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant dueDate,
        boolean isOverdue,
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant updatedAt
) {
}

