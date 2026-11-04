package com.taskmanager.notification.exception;

/**
 * Exception thrown when a requested notification is missing or belongs to another user (anti-enumeration 404).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

