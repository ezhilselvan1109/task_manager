package com.taskmanager.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.user.dto.request.LoginRequest;
import com.taskmanager.user.dto.request.RefreshTokenRequest;
import com.taskmanager.user.dto.request.RegisterRequest;
import com.taskmanager.user.dto.response.AuthResponse;
import com.taskmanager.user.dto.response.UserProfileResponse;
import com.taskmanager.user.exception.AccountDisabledException;
import com.taskmanager.user.exception.EmailAlreadyExistsException;
import com.taskmanager.user.exception.GlobalExceptionHandler;
import com.taskmanager.user.exception.InvalidRefreshTokenException;
import com.taskmanager.user.security.JwtAuthenticationFilter;
import com.taskmanager.user.security.JwtTokenProvider;
import com.taskmanager.user.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("POST /api/v1/auth/register with valid input returns 201 Created")
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest("sarah.connor@example.com", "Password123!", "Sarah Connor");
        UserProfileResponse response = new UserProfileResponse(UUID.randomUUID(), "sarah.connor@example.com", "Sarah Connor", Instant.now());

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("sarah.connor@example.com"))
                .andExpect(jsonPath("$.fullName").value("Sarah Connor"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register with invalid email returns 400 Bad Request")
    void testRegisterInvalidEmail() throws Exception {
        RegisterRequest request = new RegisterRequest("invalid-email", "Password123!", "Sarah Connor");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("ERR_VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("email"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register with weak password returns 400 Bad Request")
    void testRegisterWeakPassword() throws Exception {
        RegisterRequest request = new RegisterRequest("sarah@example.com", "weak", "Sarah Connor");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("ERR_VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("password"))
                .andExpect(jsonPath("$.fieldErrors[0].rejectedValue").value("[REDACTED]"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register with duplicate email returns 409 Conflict")
    void testRegisterDuplicateEmail() throws Exception {
        RegisterRequest request = new RegisterRequest("sarah.connor@example.com", "Password123!", "Sarah Connor");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("Email is already registered: sarah.connor@example.com"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("ERR_EMAIL_ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login with valid credentials returns 200 OK and tokens")
    void testLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest("sarah.connor@example.com", "Password123!");
        AuthResponse response = AuthResponse.of("mock.jwt.token", "mock-refresh-token");

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock.jwt.token"))
                .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login with bad credentials returns 401 Unauthorized")
    void testLoginBadCredentials() throws Exception {
        LoginRequest request = new LoginRequest("sarah.connor@example.com", "WrongPassword!");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("ERR_INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login with disabled account returns 403 Forbidden")
    void testLoginDisabledAccount() throws Exception {
        LoginRequest request = new LoginRequest("sarah.connor@example.com", "Password123!");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AccountDisabledException("User account is deactivated or suspended"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ERR_ACCOUNT_DISABLED"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh with valid token returns 200 OK")
    void testRefreshSuccess() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        AuthResponse response = AuthResponse.of("new.jwt.token", "new-refresh-token");

        when(authService.refresh(any(RefreshTokenRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new.jwt.token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh with invalid token returns 401 Unauthorized")
    void testRefreshInvalidToken() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");

        when(authService.refresh(any(RefreshTokenRequest.class)))
                .thenThrow(new InvalidRefreshTokenException("Refresh token is invalid or expired"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("ERR_TOKEN_EXPIRED"));
    }
}

