package com.taskmanager.task.domain;

import com.taskmanager.task.exception.InvalidStatusTransitionException;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Domain state machine governing task lifecycle status transitions.
 * Conforms strictly to DOC-BA-007.
 */
public final class TaskStateMachine {

    private static final Set<TaskStatus> VALID_CREATION_STATUSES = EnumSet.of(
            TaskStatus.TODO,
            TaskStatus.IN_PROGRESS
    );

    private static final Map<TaskStatus, Set<TaskStatus>> ALLOWED_TRANSITIONS = Map.of(
            TaskStatus.TODO, EnumSet.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS, TaskStatus.COMPLETED, TaskStatus.CANCELLED),
            TaskStatus.IN_PROGRESS, EnumSet.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS, TaskStatus.COMPLETED, TaskStatus.CANCELLED),
            TaskStatus.COMPLETED, EnumSet.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS, TaskStatus.COMPLETED),
            TaskStatus.CANCELLED, EnumSet.of(TaskStatus.TODO, TaskStatus.CANCELLED)
    );

    private TaskStateMachine() {
    }

    /**
     * Checks if a transition from source to target status is valid.
     */
    public static boolean isValidTransition(TaskStatus source, TaskStatus target) {
        if (source == null || target == null) {
            return false;
        }
        Set<TaskStatus> allowed = ALLOWED_TRANSITIONS.get(source);
        return allowed != null && allowed.contains(target);
    }

    /**
     * Validates that a status transition is permitted, throwing InvalidStatusTransitionException on breach.
     */
    public static void validateTransition(TaskStatus source, TaskStatus target) {
        if (source == null || target == null) {
            throw new InvalidStatusTransitionException("Task source and target statuses must not be null");
        }
        if (!isValidTransition(source, target)) {
            throw new InvalidStatusTransitionException(
                    String.format("Cannot transition task from %s to %s. Allowed transitions: %s",
                            source, target, ALLOWED_TRANSITIONS.getOrDefault(source, Set.of()))
            );
        }
    }

    /**
     * Checks whether the specified status is valid upon task creation.
     */
    public static boolean isValidCreationStatus(TaskStatus status) {
        return status != null && VALID_CREATION_STATUSES.contains(status);
    }
}

