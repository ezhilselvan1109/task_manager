package com.taskmanager.user.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request payload for user registration.
 */
public record RegisterRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @JsonProperty("email")
    String email,

    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])[^\\s]{8,100}$",
        message = "Password must be 8-100 characters long and include at least one uppercase letter, one lowercase letter, one digit, and one special character with no whitespace"
    )
    @JsonProperty("password")
    String password,

    @NotBlank(message = "Full name is required")
    @Size(min = 1, max = 100, message = "Full name must be between 1 and 100 characters")
    @JsonProperty("fullName")
    String fullName
) {}

