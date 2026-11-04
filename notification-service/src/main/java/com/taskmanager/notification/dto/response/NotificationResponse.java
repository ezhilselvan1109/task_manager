package com.taskmanager.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.UUID;

/**
 * REST API response DTO for a single in-app notification.
 * Conforms to DOC-API-003 and OpenAPI specification.
 */
public record NotificationResponse(
        UUID id,
        UUID userId,
        UUID taskId,
        String eventType,
        String title,
        String message,
        boolean read,
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant readAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
        Instant createdAt
) {
}

