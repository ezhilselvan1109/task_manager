package com.taskmanager.gateway.security;

/**
 * Result model representing the outcome of JWT perimeter validation.
 */
public class JwtValidationResult {

    private final boolean valid;
    private final UserIdentity identity;
    private final String errorCode;
    private final String errorMessage;

    private JwtValidationResult(boolean valid, UserIdentity identity, String errorCode, String errorMessage) {
        this.valid = valid;
        this.identity = identity;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public static JwtValidationResult success(UserIdentity identity) {
        return new JwtValidationResult(true, identity, null, null);
    }

    public static JwtValidationResult failure(String errorCode, String errorMessage) {
        return new JwtValidationResult(false, null, errorCode, errorMessage);
    }

    public boolean isValid() {
        return valid;
    }

    public UserIdentity getIdentity() {
        return identity;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

