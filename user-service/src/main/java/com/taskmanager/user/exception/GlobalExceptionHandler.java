package com.taskmanager.user.exception;

import com.taskmanager.common.constants.CorrelationConstants;
import com.taskmanager.common.error.ErrorResponse;
import com.taskmanager.common.error.FieldErrorDetail;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Centralized exception handler emitting RFC 7807 compliant Problem Details error responses.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("Conflict detected: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "ERR_EMAIL_ALREADY_EXISTS", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "ERR_INVALID_CREDENTIALS", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<ErrorResponse> handleAccountDisabled(AccountDisabledException ex, HttpServletRequest request) {
        log.warn("Access denied for disabled account: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "ERR_ACCOUNT_DISABLED", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(InvalidRefreshTokenException ex, HttpServletRequest request) {
        log.warn("Invalid refresh token attempt: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "ERR_TOKEN_EXPIRED", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "ERR_RESOURCE_NOT_FOUND", ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access forbidden: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "ERR_FORBIDDEN", "Access is denied", request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldErrorDetail> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::mapFieldError)
                .toList();

        log.warn("Validation failed for request {}: {} field error(s)", request.getRequestURI(), fieldErrors.size());
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "ERR_VALIDATION_FAILED",
                "Input validation failed for " + fieldErrors.size() + " field(s)",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled internal server error on URI {}: ", request.getRequestURI(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "ERR_INTERNAL_SERVER_ERROR",
                "An unexpected internal error occurred. Please contact support with correlationId.",
                request,
                List.of()
        );
    }

    private FieldErrorDetail mapFieldError(FieldError fieldError) {
        String rejectedValue = fieldError.getRejectedValue() != null ? String.valueOf(fieldError.getRejectedValue()) : "null";
        // Never echo password fields in rejected values
        if (fieldError.getField().toLowerCase().contains("password")) {
            rejectedValue = "[REDACTED]";
        }
        return new FieldErrorDetail(
                fieldError.getField(),
                rejectedValue,
                fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value"
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String errorCode,
            String message,
            HttpServletRequest request,
            List<FieldErrorDetail> errors
    ) {
        String correlationId = MDC.get(CorrelationConstants.MDC_KEY);
        ErrorResponse body = ErrorResponse.of(
                status.value(),
                status.getReasonPhrase(),
                errorCode,
                message,
                request.getRequestURI(),
                correlationId != null ? correlationId : "",
                errors
        );
        return ResponseEntity.status(status).body(body);
    }
}

