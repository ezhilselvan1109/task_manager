package com.taskmanager.common.error;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ErrorResponseTest {

    @Test
    @DisplayName("ErrorResponse factory method creates valid RFC 7807 problem details")
    void testFactoryMethodWithoutFieldErrors() {
        ErrorResponse response = ErrorResponse.of(
                404,
                "Not Found",
                "ERR_RESOURCE_NOT_FOUND",
                "Task not found",
                "/api/v1/tasks/123",
                "corr-abc-123"
        );

        assertThat(response.status()).isEqualTo(404);
        assertThat(response.error()).isEqualTo("Not Found");
        assertThat(response.errorCode()).isEqualTo("ERR_RESOURCE_NOT_FOUND");
        assertThat(response.message()).isEqualTo("Task not found");
        assertThat(response.path()).isEqualTo("/api/v1/tasks/123");
        assertThat(response.correlationId()).isEqualTo("corr-abc-123");
        assertThat(response.timestamp()).isNotNull();
        assertThat(response.fieldErrors()).isEmpty();
    }

    @Test
    @DisplayName("ErrorResponse factory method preserves field errors and enforces immutability")
    void testFactoryMethodWithFieldErrors() {
        List<FieldErrorDetail> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldErrorDetail("title", "   ", "Task title must not be blank"));

        ErrorResponse response = ErrorResponse.of(
                400,
                "Bad Request",
                "ERR_VALIDATION_FAILED",
                "Input validation failed",
                "/api/v1/tasks",
                "corr-xyz-789",
                fieldErrors
        );

        assertThat(response.fieldErrors()).hasSize(1);
        assertThat(response.fieldErrors().getFirst().field()).isEqualTo("title");
        assertThat(response.fieldErrors().getFirst().rejectedValue()).isEqualTo("   ");
        assertThat(response.fieldErrors().getFirst().message()).isEqualTo("Task title must not be blank");

        // Mutating external list must not affect response
        fieldErrors.add(new FieldErrorDetail("description", "", "Invalid"));
        assertThat(response.fieldErrors()).hasSize(1);

        // Mutating returned list directly should throw UnsupportedOperationException
        List<FieldErrorDetail> returnedErrors = response.fieldErrors();
        assertThatThrownBy(() -> returnedErrors.add(new FieldErrorDetail("status", "", "Invalid")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("ErrorResponse handles null timestamp and null fieldErrors gracefully")
    void testNullFieldErrorsAndTimestamp() {
        ErrorResponse response = new ErrorResponse(
                null,
                500,
                "Internal Server Error",
                "ERR_INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                "/api/v1/tasks",
                "corr-500",
                null
        );

        assertThat(response.timestamp()).isNotNull();
        assertThat(response.fieldErrors()).isNotNull().isEmpty();
    }
}

