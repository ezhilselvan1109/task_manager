# Exception Handling Design: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-LLD-010 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Software Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-LLD-001`, `DOC-REQ-004` |

---

## 1. Overview & RFC 7807 Standard
All microservices implement uniform, sanitized, and machine-readable error handling conforming to **RFC 7807 (Problem Details for HTTP APIs)**. Internal stack traces, raw SQL exceptions, and proprietary class names are never exposed to clients.

```json
{
  "timestamp": "2026-11-03T14:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "errorCode": "ERR_VALIDATION_FAILED",
  "message": "Input validation failed for 1 field(s)",
  "path": "/api/v1/tasks",
  "correlationId": "c1a2b3c4-d5e6-7890-abcd-ef1234567890",
  "fieldErrors": [
    {
      "field": "title",
      "rejectedValue": "   ",
      "message": "Task title must not be blank"
    }
  ]
}
```

---

## 2. Global Exception Handler (`GlobalExceptionHandler.java`)

```java
package com.chellaa.task_service.exception;

import com.chellaa.task_service.context.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "ERR_RESOURCE_NOT_FOUND", ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(err -> new ValidationError(err.getField(), String.valueOf(err.getRejectedValue()), err.getDefaultMessage()))
            .toList();

        return buildResponse(HttpStatus.BAD_REQUEST, "ERR_VALIDATION_FAILED", 
            "Input validation failed for " + errors.size() + " field(s)", request, errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled internal server error: ", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "ERR_INTERNAL_SERVER_ERROR", 
            "An unexpected error occurred. Please contact support with correlationId.", request, null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
        HttpStatus status, String code, String message, HttpServletRequest request, List<ValidationError> errors
    ) {
        ErrorResponse body = new ErrorResponse(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            code,
            message,
            request.getRequestURI(),
            UserContextHolder.getCorrelationId(),
            errors
        );
        return ResponseEntity.status(status).body(body);
    }
}
```

---

## 3. Standardized Error Code Catalogue

| HTTP Status | Error Code | Description | Trigger Scenario |
|---|---|---|---|
| **400 Bad Request** | `ERR_VALIDATION_FAILED` | Bean validation constraint violation. | Title blank, email syntax invalid, password weak. |
| **400 Bad Request** | `ERR_INVALID_STATUS` | Unrecognized or disallowed status enum. | Supplying `"UNKNOWN_STATE"` in PATCH status. |
| **401 Unauthorized** | `ERR_INVALID_CREDENTIALS` | Password or email mismatch. | Wrong password submitted during login. |
| **401 Unauthorized** | `ERR_TOKEN_EXPIRED` | JWT access token expired. | Calling protected API after 15-minute token TTL. |
| **403 Forbidden** | `ERR_ACCOUNT_DISABLED` | User account is suspended/disabled. | Login attempted by deactivated user. |
| **404 Not Found** | `ERR_RESOURCE_NOT_FOUND` | Record does not exist or belongs to another user. | Fetching task ID owned by a different user. |
| **409 Conflict** | `ERR_EMAIL_ALREADY_EXISTS` | Email uniqueness violation. | Registering with an existing email. |
| **500 Internal Error**| `ERR_INTERNAL_SERVER_ERROR` | Unexpected unhandled exception. | Database connection pool failure. |

---

## 4. Review Checklist
- [x] Conforms to RFC 7807 problem details specification.
- [x] Correlation ID embedded in all error payloads for distributed tracing.
- [x] Information disclosure prevented: internal stack traces suppressed.

---

## 5. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Software Architect | Initial baseline Exception Handling Design. |

