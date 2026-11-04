package com.taskmanager.task.exception;

/**
 * Thrown when outbox event JSON serialization fails.
 */
public class EventSerializationException extends RuntimeException {

    public EventSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}

