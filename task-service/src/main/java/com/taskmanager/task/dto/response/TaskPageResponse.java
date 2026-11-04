package com.taskmanager.task.dto.response;

import java.util.List;

/**
 * Standard paginated response wrapper for task list queries.
 */
public record TaskPageResponse(
        List<TaskResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean isLast
) {
}

