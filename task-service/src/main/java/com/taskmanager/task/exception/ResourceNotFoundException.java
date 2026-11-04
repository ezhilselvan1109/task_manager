package com.taskmanager.task.exception;

/**
 * Thrown when a requested task does not exist or does not belong to the calling user.
 * Mapped to HTTP 404 Not Found to enforce anti-enumeration (RULE-05).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

