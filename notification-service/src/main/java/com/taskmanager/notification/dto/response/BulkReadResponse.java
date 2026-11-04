package com.taskmanager.notification.dto.response;

/**
 * Response DTO for bulk marking all notifications as read.
 */
public record BulkReadResponse(
        int updatedCount
) {
}

