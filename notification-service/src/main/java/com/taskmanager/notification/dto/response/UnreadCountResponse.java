package com.taskmanager.notification.dto.response;

/**
 * Response DTO for unread notification count badge.
 */
public record UnreadCountResponse(
        long unreadCount
) {
}

