package com.taskmanager.task.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.taskmanager.task.domain.TaskPriority;
import com.taskmanager.task.domain.TaskStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Event payload structure matching DOC-EVT-005 JSON Schema.
 */
public record TaskEventPayload(
        UUID id,
        UUID userId,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant dueDate
) {
}

