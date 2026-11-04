package com.taskmanager.task.dto.response;

/**
 * Summary metrics response for the user productivity dashboard.
 * Conforms to DOC-REQ-001 and DOC-API-003.
 */
public record TaskMetricsResponse(
        long totalTasks,
        long todoCount,
        long inProgressCount,
        long completedCount,
        long cancelledCount,
        long highPriorityCount,
        long overdueCount
) {
}

