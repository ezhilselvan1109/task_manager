package com.taskmanager.user.controller;

import com.taskmanager.common.constants.SecurityConstants;
import com.taskmanager.user.dto.request.UpdateProfileRequest;
import com.taskmanager.user.dto.response.UserProfileResponse;
import com.taskmanager.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Protected REST Controller managing user profile operations.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserProfileResponse getCurrentUser(HttpServletRequest request, Authentication authentication) {
        UUID userId = extractUserId(request, authentication);
        return userService.getCurrentProfile(userId);
    }

    @PutMapping("/me")
    public UserProfileResponse updateProfile(
            HttpServletRequest request,
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest updateRequest
    ) {
        UUID userId = extractUserId(request, authentication);
        return userService.updateProfile(userId, updateRequest);
    }

    public UUID extractUserId(HttpServletRequest request, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UUID uuid) {
            return uuid;
        }
        if (request != null) {
            String headerUserId = request.getHeader(SecurityConstants.HEADER_USER_ID);
            if (headerUserId != null && !headerUserId.isBlank()) {
                return UUID.fromString(headerUserId.trim());
            }
        }
        throw new IllegalStateException("Authenticated principal does not contain a valid UUID");
    }
}
