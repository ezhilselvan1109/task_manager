package com.taskmanager.common.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Standardized RFC 7807 compliant error response model across all microservices.
 *
 * @param timestamp Time of occurrence in ISO-8601 UTC.
 * @param status HTTP response status code (e.g. 400, 404, 500).
 * @param error Short HTTP reason phrase (e.g. "Bad Request", "Not Found").
 * @param errorCode Machine-readable domain error code (e.g. "ERR_VALIDATION_FAILED").
 * @param message Sanitized, human-readable description of the error.
 * @param path Request URI path that originated the failure.
 * @param correlationId Distributed correlation identifier for end-to-end tracing.
 * @param fieldErrors Detailed list of property validation failures (empty when not applicable).
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
    @JsonProperty("timestamp")
    Instant timestamp,

    @JsonProperty("status")
    int status,

    @JsonProperty("error")
    String error,

    @JsonProperty("errorCode")
    String errorCode,

    @JsonProperty("message")
    String message,

    @JsonProperty("path")
    String path,

    @JsonProperty("correlationId")
    String correlationId,

    @JsonProperty("fieldErrors")
    List<FieldErrorDetail> fieldErrors
) {
    /**
     * Compact constructor enforcing null-safety and defensive copying of field errors.
     */
    public ErrorResponse {
        timestamp = timestamp != null ? timestamp : Instant.now();
        fieldErrors = (fieldErrors != null) ? List.copyOf(fieldErrors) : List.of();
    }

    /**
     * Factory method creating an ErrorResponse without field-level errors.
     */
    public static ErrorResponse of(int status, String error, String errorCode, String message, String path, String correlationId) {
        return new ErrorResponse(Instant.now(), status, error, errorCode, message, path, correlationId, List.of());
    }

    /**
     * Factory method creating an ErrorResponse with field-level errors.
     */
    public static ErrorResponse of(int status, String error, String errorCode, String message, String path, String correlationId, List<FieldErrorDetail> fieldErrors) {
        return new ErrorResponse(Instant.now(), status, error, errorCode, message, path, correlationId, fieldErrors);
    }
}

