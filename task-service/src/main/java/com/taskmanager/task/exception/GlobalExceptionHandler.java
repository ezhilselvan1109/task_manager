package com.taskmanager.task.exception;

import com.taskmanager.common.constants.CorrelationConstants;
import com.taskmanager.common.error.ErrorResponse;
import com.taskmanager.common.error.FieldErrorDetail;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * Global exception handler formatting task-service exceptions into standardized RFC 7807 ErrorResponses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String errorCode = status == HttpStatus.UNAUTHORIZED ? "UNAUTHORIZED" : status.name();
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();

        ErrorResponse response = ErrorResponse.of(
                status.value(),
                status.getReasonPhrase(),
                errorCode,
                message,
                request.getRequestURI(),
                correlationId
        );
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        log.warn("Resource not found: {} (CorrelationId: {})", ex.getMessage(), correlationId);

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "ERR_RESOURCE_NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI(),
                correlationId
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatusTransition(InvalidStatusTransitionException ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        log.warn("Invalid task status transition: {} (CorrelationId: {})", ex.getMessage(), correlationId);

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "ERR_INVALID_STATUS",
                ex.getMessage(),
                request.getRequestURI(),
                correlationId
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);

        List<FieldErrorDetail> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new FieldErrorDetail(err.getField(), err.getDefaultMessage(), "[REDACTED]"))
                .toList();

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "ERR_VALIDATION_FAILED",
                "Validation failed for one or more fields",
                request.getRequestURI(),
                correlationId,
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        log.warn("Bad request: {} (CorrelationId: {})", ex.getMessage(), correlationId);

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "BAD_REQUEST",
                ex.getMessage(),
                request.getRequestURI(),
                correlationId
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        log.error("Unhandled internal server error: {} (CorrelationId: {})", ex.getMessage(), correlationId, ex);

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "INTERNAL_SERVER_ERROR",
                "An unexpected internal error occurred. Please contact support.",
                request.getRequestURI(),
                correlationId
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CorrelationConstants.HEADER_NAME);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = MDC.get(CorrelationConstants.MDC_KEY);
        }
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        return correlationId;
    }
}
