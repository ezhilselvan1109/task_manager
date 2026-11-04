package com.taskmanager.task.exception;

import com.taskmanager.common.error.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/tasks");
    }

    @Test
    @DisplayName("Should handle ResourceNotFoundException and return 404 with ERR_RESOURCE_NOT_FOUND")
    void testHandleResourceNotFound() {
        UUID taskId = UUID.randomUUID();
        ResourceNotFoundException ex = new ResourceNotFoundException("Task not found with ID: " + taskId);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().errorCode()).isEqualTo("ERR_RESOURCE_NOT_FOUND");
        assertThat(response.getBody().message()).contains(taskId.toString());
    }

    @Test
    @DisplayName("Should handle InvalidStatusTransitionException and return 400 with ERR_INVALID_STATUS")
    void testHandleInvalidStatusTransition() {
        InvalidStatusTransitionException ex = new InvalidStatusTransitionException("Cannot transition from COMPLETED to CANCELLED");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidStatusTransition(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().errorCode()).isEqualTo("ERR_INVALID_STATUS");
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException and return 400 with field errors")
    void testHandleValidationException() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "createTaskRequest");
        bindingResult.addError(new FieldError("createTaskRequest", "title", "must not be blank"));

        MethodParameter parameter = new MethodParameter(this.getClass().getDeclaredMethod("testHandleValidationException"), -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("ERR_VALIDATION_FAILED");
        assertThat(response.getBody().fieldErrors()).hasSize(1);
        assertThat(response.getBody().fieldErrors().get(0).field()).isEqualTo("title");
    }

    @Test
    @DisplayName("Should handle generic exception and return 500 Internal Server Error")
    void testHandleGenericException() {
        RuntimeException ex = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().errorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
    }

    @Test
    @DisplayName("Should handle ResponseStatusException for 401 UNAUTHORIZED and other statuses")
    void testHandleResponseStatusException() {
        // Test 401 UNAUTHORIZED with message
        org.springframework.web.server.ResponseStatusException ex401 =
                new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Custom Auth Failed");
        ResponseEntity<ErrorResponse> response401 = exceptionHandler.handleResponseStatusException(ex401, request);
        assertThat(response401.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response401.getBody().errorCode()).isEqualTo("UNAUTHORIZED");
        assertThat(response401.getBody().message()).isEqualTo("Custom Auth Failed");

        // Test 403 FORBIDDEN with null reason
        org.springframework.web.server.ResponseStatusException ex403 =
                new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
        ResponseEntity<ErrorResponse> response403 = exceptionHandler.handleResponseStatusException(ex403, request);
        assertThat(response403.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response403.getBody().errorCode()).isEqualTo("FORBIDDEN");
        assertThat(response403.getBody().message()).isEqualTo(HttpStatus.FORBIDDEN.getReasonPhrase());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException and IllegalStateException as 400 Bad Request")
    void testHandleBadRequestExceptions() {
        IllegalArgumentException ex1 = new IllegalArgumentException("Invalid state transition argument");
        ResponseEntity<ErrorResponse> resp1 = exceptionHandler.handleBadRequest(ex1, request);
        assertThat(resp1.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp1.getBody().errorCode()).isEqualTo("BAD_REQUEST");
        assertThat(resp1.getBody().message()).isEqualTo("Invalid state transition argument");

        IllegalStateException ex2 = new IllegalStateException("Illegal operation attempted");
        ResponseEntity<ErrorResponse> resp2 = exceptionHandler.handleBadRequest(ex2, request);
        assertThat(resp2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should resolve correlation ID from header, MDC, and fallback to random UUID")
    void testCorrelationIdResolution() {
        // 1. Resolve from header
        request.addHeader(com.taskmanager.common.constants.CorrelationConstants.HEADER_NAME, "header-corr-id");
        ResponseEntity<ErrorResponse> respFromHeader = exceptionHandler.handleBadRequest(new IllegalArgumentException("err"), request);
        assertThat(respFromHeader.getBody().correlationId()).isEqualTo("header-corr-id");

        // 2. Resolve from MDC when header is blank
        MockHttpServletRequest requestWithBlankHeader = new MockHttpServletRequest();
        requestWithBlankHeader.addHeader(com.taskmanager.common.constants.CorrelationConstants.HEADER_NAME, "   ");
        org.slf4j.MDC.put(com.taskmanager.common.constants.CorrelationConstants.MDC_KEY, "mdc-corr-id");
        try {
            ResponseEntity<ErrorResponse> respFromMdc = exceptionHandler.handleBadRequest(new IllegalArgumentException("err"), requestWithBlankHeader);
            assertThat(respFromMdc.getBody().correlationId()).isEqualTo("mdc-corr-id");
        } finally {
            org.slf4j.MDC.clear();
        }

        // 3. Fallback to random UUID when neither header nor MDC is provided
        MockHttpServletRequest emptyRequest = new MockHttpServletRequest();
        ResponseEntity<ErrorResponse> respFallback = exceptionHandler.handleBadRequest(new IllegalArgumentException("err"), emptyRequest);
        assertThat(respFallback.getBody().correlationId()).isNotNull().isNotBlank();
    }
}

