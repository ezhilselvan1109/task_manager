package com.taskmanager.task.dto.request;

import com.taskmanager.task.domain.TaskStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for atomic task state transitions.
 */
public record ChangeTaskStatusRequest(
        @NotNull(message = "Target status is mandatory")
        TaskStatus status
) {
}

