package com.taskmanager.user.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordEncoderTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @Test
    @DisplayName("BCryptPasswordEncoder uses cost factor 12 ($2a$12$) and produces valid verification")
    void testBCryptCostFactor12() {
        String rawPassword = "Password123!";
        String encoded = passwordEncoder.encode(rawPassword);

        assertThat(encoded).startsWith("$2a$12$");
        assertThat(passwordEncoder.matches(rawPassword, encoded)).isTrue();
        assertThat(passwordEncoder.matches("WrongPassword!", encoded)).isFalse();
    }
}

