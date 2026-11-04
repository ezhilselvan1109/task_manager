package com.taskmanager.user.exception;

/**
 * Thrown when a refresh token is expired, revoked, or non-existent.
 */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}

