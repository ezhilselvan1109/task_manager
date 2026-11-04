package com.taskmanager.notification.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

/**
 * Task event payload representation matching DOC-EVT-005 JSON Schema.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TaskEventPayload(
        UUID id,
        UUID userId,
        String title,
        String description,
        String status,
        String priority,
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant dueDate
) {
}

