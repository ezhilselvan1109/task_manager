package com.taskmanager.user.exception;

import com.taskmanager.common.error.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleAccessDenied returns 403 Forbidden with sanitized problem details")
    void testHandleAccessDenied() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/me");
        AccessDeniedException ex = new AccessDeniedException("Access forbidden");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("ERR_FORBIDDEN");
        assertThat(response.getBody().path()).isEqualTo("/api/v1/users/me");
    }

    @Test
    @DisplayName("handleGenericException returns 500 Internal Error and redacts internal details")
    void testHandleGenericException() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        NullPointerException ex = new NullPointerException("Null internal pointer in DB pool");

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("ERR_INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().message()).doesNotContain("Null internal pointer");
        assertThat(response.getBody().message()).contains("An unexpected internal error occurred");
    }

    @Test
    @DisplayName("handleValidationException maps and redacts password fields while keeping non-password values")
    void testHandleValidationException() throws NoSuchMethodException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/register");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");

        bindingResult.addError(new FieldError("target", "password", "Secret123!", false, null, null, "Password too short"));
        bindingResult.addError(new FieldError("target", "email", "invalid-email", false, null, null, "Invalid email"));
        bindingResult.addError(new FieldError("target", "fullName", null, false, null, null, "Required"));

        Method method = this.getClass().getDeclaredMethod("testHandleValidationException");
        MethodParameter parameter = new MethodParameter(method, -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().fieldErrors()).hasSize(3);

        // Verify password redaction
        assertThat(response.getBody().fieldErrors().get(0).field()).isEqualTo("password");
        assertThat(response.getBody().fieldErrors().get(0).rejectedValue()).isEqualTo("[REDACTED]");

        // Verify non-password field preservation
        assertThat(response.getBody().fieldErrors().get(1).field()).isEqualTo("email");
        assertThat(response.getBody().fieldErrors().get(1).rejectedValue()).isEqualTo("invalid-email");

        // Verify null rejectedValue
        assertThat(response.getBody().fieldErrors().get(2).field()).isEqualTo("fullName");
        assertThat(response.getBody().fieldErrors().get(2).rejectedValue()).isEqualTo("null");
    }
}
