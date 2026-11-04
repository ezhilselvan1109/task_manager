package com.taskmanager.user.exception;

/**
 * Thrown when an entity lookup fails or a user cannot access another tenant's resource.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

