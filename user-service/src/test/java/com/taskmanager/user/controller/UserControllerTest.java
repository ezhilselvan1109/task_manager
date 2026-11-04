package com.taskmanager.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.common.constants.SecurityConstants;
import com.taskmanager.user.dto.request.UpdateProfileRequest;
import com.taskmanager.user.dto.response.UserProfileResponse;
import com.taskmanager.user.exception.GlobalExceptionHandler;
import com.taskmanager.user.exception.ResourceNotFoundException;
import com.taskmanager.user.security.JwtAuthenticationFilter;
import com.taskmanager.user.security.JwtTokenProvider;
import com.taskmanager.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserController userController;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private final UUID testUserId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");

    private UsernamePasswordAuthenticationToken createAuthToken() {
        return new UsernamePasswordAuthenticationToken(
                testUserId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    @DisplayName("GET /api/v1/users/me returns authenticated user's profile")
    void testGetCurrentProfileSuccess() throws Exception {
        UserProfileResponse response = new UserProfileResponse(
                testUserId,
                "sarah.connor@example.com",
                "Sarah Connor",
                Instant.now()
        );

        when(userService.getCurrentProfile(testUserId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/me")
                        .header(SecurityConstants.HEADER_USER_ID, testUserId.toString())
                        .with(authentication(createAuthToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.email").value("sarah.connor@example.com"))
                .andExpect(jsonPath("$.fullName").value("Sarah Connor"));
    }

    @Test
    @DisplayName("GET /api/v1/users/me returns 404 when user profile not found")
    void testGetCurrentProfileNotFound() throws Exception {
        when(userService.getCurrentProfile(testUserId))
                .thenThrow(new ResourceNotFoundException("User not found with id: " + testUserId));

        mockMvc.perform(get("/api/v1/users/me")
                        .header(SecurityConstants.HEADER_USER_ID, testUserId.toString())
                        .with(authentication(createAuthToken())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ERR_RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("PUT /api/v1/users/me updates profile successfully")
    void testUpdateProfileSuccess() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest("Sarah Connor-Reese");
        UserProfileResponse response = new UserProfileResponse(
                testUserId,
                "sarah.connor@example.com",
                "Sarah Connor-Reese",
                Instant.now()
        );

        when(userService.updateProfile(eq(testUserId), any(UpdateProfileRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/users/me")
                        .header(SecurityConstants.HEADER_USER_ID, testUserId.toString())
                        .with(authentication(createAuthToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Sarah Connor-Reese"));
    }

    @Test
    @DisplayName("PUT /api/v1/users/me with blank full name returns 400 Bad Request")
    void testUpdateProfileValidationFailure() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest("   ");

        mockMvc.perform(put("/api/v1/users/me")
                        .header(SecurityConstants.HEADER_USER_ID, testUserId.toString())
                        .with(authentication(createAuthToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("ERR_VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("fullName"));
    }

    @Test
    @DisplayName("extractUserId extracts from Authentication principal, Header, or throws")
    void testExtractUserIdBranches() {
        // Branch 1: Authentication principal is UUID
        UUID fromAuth = userController.extractUserId(new MockHttpServletRequest(), createAuthToken());
        assertThat(fromAuth).isEqualTo(testUserId);

        // Branch 2: Header-based identity
        MockHttpServletRequest requestWithHeader = new MockHttpServletRequest();
        requestWithHeader.addHeader(SecurityConstants.HEADER_USER_ID, testUserId.toString());
        UUID fromHeader = userController.extractUserId(requestWithHeader, null);
        assertThat(fromHeader).isEqualTo(testUserId);

        // Branch 3: Neither provided throws IllegalStateException
        assertThatThrownBy(() -> userController.extractUserId(new MockHttpServletRequest(), null))
                .isInstanceOf(IllegalStateException.class);
    }
}
