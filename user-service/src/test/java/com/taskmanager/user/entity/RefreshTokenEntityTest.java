package com.taskmanager.user.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenEntityTest {

    @Test
    @DisplayName("RefreshTokenEntity validation and helper methods operate correctly")
    void testRefreshTokenEntity() {
        UUID id = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("john@example.com")
                .fullName("John Doe")
                .status(UserStatus.ACTIVE)
                .build();

        RefreshTokenEntity token = RefreshTokenEntity.builder()
                .id(id)
                .user(user)
                .token("sample-refresh-token")
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .createdAt(Instant.now())
                .build();

        assertThat(token.getId()).isEqualTo(id);
        assertThat(token.getUser()).isEqualTo(user);
        assertThat(token.getToken()).isEqualTo("sample-refresh-token");
        assertThat(token.isRevoked()).isFalse();
        assertThat(token.isExpired()).isFalse();
        assertThat(token.isValid()).isTrue();

        token.setRevoked(true);
        assertThat(token.isValid()).isFalse();

        token.setRevoked(false);
        token.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        assertThat(token.isExpired()).isTrue();
        assertThat(token.isValid()).isFalse();
    }
}

