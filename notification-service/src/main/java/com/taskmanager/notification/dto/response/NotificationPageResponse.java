package com.taskmanager.notification.dto.response;

import java.util.List;

/**
 * Paginated response DTO for user notifications matching OpenAPI specification.
 */
public record NotificationPageResponse(
        List<NotificationResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        long unreadCount
) {
}

