package com.taskmanager.user.exception;

/**
 * Thrown when an authentication attempt is made against a suspended or deactivated account.
 */
public class AccountDisabledException extends RuntimeException {

    public AccountDisabledException(String message) {
        super(message);
    }
}

