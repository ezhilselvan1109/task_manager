package com.taskmanager.task.exception;

/**
 * Thrown when an invalid task status transition is attempted.
 * Mapped to HTTP 400 Bad Request with errorCode ERR_INVALID_STATUS.
 */
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}

