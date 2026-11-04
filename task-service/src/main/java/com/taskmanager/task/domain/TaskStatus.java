package com.taskmanager.task.domain;

/**
 * Enumeration of task lifecycle states.
 * Allowed transitions are governed by TaskStateMachine.
 */
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

