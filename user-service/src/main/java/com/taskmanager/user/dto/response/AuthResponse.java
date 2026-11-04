package com.taskmanager.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Public response model returned upon successful authentication or token refresh.
 */
public record AuthResponse(
    @JsonProperty("accessToken")
    String accessToken,

    @JsonProperty("refreshToken")
    String refreshToken,

    @JsonProperty("tokenType")
    String tokenType,

    @JsonProperty("expiresIn")
    long expiresIn
) {
    public static final String TOKEN_TYPE_BEARER = "Bearer";
    public static final long DEFAULT_EXPIRES_IN_SECONDS = 900L;

    public static AuthResponse of(String accessToken, String refreshToken) {
        return new AuthResponse(accessToken, refreshToken, TOKEN_TYPE_BEARER, DEFAULT_EXPIRES_IN_SECONDS);
    }
}

