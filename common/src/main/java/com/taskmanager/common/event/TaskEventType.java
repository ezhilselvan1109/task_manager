package com.taskmanager.common.event;

/**
 * Enumeration of domain event types emitted by task-service across Kafka topic task-events.
 */
public enum TaskEventType {
    TASK_CREATED,
    TASK_UPDATED,
    TASK_COMPLETED,
    TASK_DELETED
}

