package com.taskmanager.user.exception;

/**
 * Thrown when an email registration request conflicts with an existing user record.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}

