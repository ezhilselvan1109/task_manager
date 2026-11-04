package com.taskmanager.user.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * Public response model for user profile information.
 * Omits all internal security credentials, password hashes, and database implementation details.
 */
public record UserProfileResponse(
    @JsonProperty("id")
    UUID id,

    @JsonProperty("email")
    String email,

    @JsonProperty("fullName")
    String fullName,

    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
    @JsonProperty("createdAt")
    Instant createdAt
) {}

