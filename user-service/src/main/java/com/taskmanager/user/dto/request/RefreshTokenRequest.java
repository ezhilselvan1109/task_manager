package com.taskmanager.user.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for refreshing access tokens.
 */
public record RefreshTokenRequest(
    @NotBlank(message = "Refresh token is required")
    @JsonProperty("refreshToken")
    String refreshToken
) {}

