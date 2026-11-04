package com.taskmanager.notification.exception;

import com.taskmanager.common.constants.CorrelationConstants;
import com.taskmanager.common.error.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/notifications");
    }

    @Test
    @DisplayName("Should handle ResourceNotFoundException and return 404 with ERR_RESOURCE_NOT_FOUND")
    void testHandleResourceNotFound() {
        UUID id = UUID.randomUUID();
        ResourceNotFoundException ex = new ResourceNotFoundException("Notification not found with ID: " + id);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().errorCode()).isEqualTo("ERR_RESOURCE_NOT_FOUND");
        assertThat(response.getBody().message()).contains(id.toString());
    }

    @Test
    @DisplayName("Should handle ResponseStatusException for 401 UNAUTHORIZED and other statuses")
    void testHandleResponseStatusException() {
        ResponseStatusException ex401 = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Custom Auth Failed");
        ResponseEntity<ErrorResponse> response401 = exceptionHandler.handleResponseStatusException(ex401, request);
        assertThat(response401.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response401.getBody().errorCode()).isEqualTo("UNAUTHORIZED");

        ResponseStatusException ex403 = new ResponseStatusException(HttpStatus.FORBIDDEN);
        ResponseEntity<ErrorResponse> response403 = exceptionHandler.handleResponseStatusException(ex403, request);
        assertThat(response403.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response403.getBody().errorCode()).isEqualTo("FORBIDDEN");
        assertThat(response403.getBody().message()).isEqualTo(HttpStatus.FORBIDDEN.getReasonPhrase());

        ResponseStatusException ex400 = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Custom Bad Request");
        ResponseEntity<ErrorResponse> response400 = exceptionHandler.handleResponseStatusException(ex400, request);
        assertThat(response400.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response400.getBody().errorCode()).isEqualTo("BAD_REQUEST");
        assertThat(response400.getBody().message()).isEqualTo("Custom Bad Request");
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException and return 400 with field errors")
    void testHandleValidationException() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "field", "must not be blank"));

        MethodParameter parameter = new MethodParameter(this.getClass().getDeclaredMethod("testHandleValidationException"), -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().errorCode()).isEqualTo("ERR_VALIDATION_FAILED");
        assertThat(response.getBody().fieldErrors()).hasSize(1);
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException and IllegalStateException as 400 Bad Request")
    void testHandleBadRequestExceptions() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().errorCode()).isEqualTo("BAD_REQUEST");
    }

    @Test
    @DisplayName("Should handle generic exception and return 500 Internal Server Error")
    void testHandleGenericException() {
        RuntimeException ex = new RuntimeException("Crash");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().errorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
    }

    @Test
    @DisplayName("Should resolve correlation ID from header, MDC, and fallback to random UUID")
    void testCorrelationIdResolution() {
        // 1. Resolve from header
        request.addHeader(CorrelationConstants.HEADER_NAME, "header-corr-id");
        ResponseEntity<ErrorResponse> r1 = exceptionHandler.handleBadRequest(new IllegalArgumentException("err"), request);
        assertThat(r1.getBody().correlationId()).isEqualTo("header-corr-id");

        // 2. Resolve from MDC when header is blank
        MockHttpServletRequest reqBlank = new MockHttpServletRequest();
        reqBlank.addHeader(CorrelationConstants.HEADER_NAME, "   ");
        MDC.put(CorrelationConstants.MDC_KEY, "mdc-corr-id");
        try {
            ResponseEntity<ErrorResponse> r2 = exceptionHandler.handleBadRequest(new IllegalArgumentException("err"), reqBlank);
            assertThat(r2.getBody().correlationId()).isEqualTo("mdc-corr-id");
        } finally {
            MDC.clear();
        }

        // 3. Fallback to random UUID
        MockHttpServletRequest emptyReq = new MockHttpServletRequest();
        ResponseEntity<ErrorResponse> r3 = exceptionHandler.handleBadRequest(new IllegalArgumentException("err"), emptyReq);
        assertThat(r3.getBody().correlationId()).isNotNull().isNotBlank();
    }
}

