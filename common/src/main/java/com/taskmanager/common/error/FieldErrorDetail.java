package com.taskmanager.common.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Immutable representation of a single field-level validation failure conforming to RFC 7807 extensions.
 *
 * @param field The field path or property name that failed validation.
 * @param rejectedValue The rejected value as a sanitized string representation.
 * @param message The human-readable constraint failure description.
 */
public record FieldErrorDetail(
    @NotBlank
    @JsonProperty("field")
    String field,

    @JsonProperty("rejectedValue")
    String rejectedValue,

    @NotBlank
    @JsonProperty("message")
    String message
) {}

