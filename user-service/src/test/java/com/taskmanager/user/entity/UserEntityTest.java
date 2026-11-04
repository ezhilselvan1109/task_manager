package com.taskmanager.user.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    @DisplayName("UserEntity getters, setters, builder, and helper methods function as expected")
    void testUserEntity() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        UserEntity user = UserEntity.builder()
                .id(id)
                .email("sarah.connor@example.com")
                .passwordHash("$2a$12$e8y1...")
                .fullName("Sarah Connor")
                .status(UserStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail()).isEqualTo("sarah.connor@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("$2a$12$e8y1...");
        assertThat(user.getFullName()).isEqualTo("Sarah Connor");
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.isActive()).isTrue();
        assertThat(user.isSuspended()).isFalse();
        assertThat(user.isDeactivated()).isFalse();

        user.setStatus(UserStatus.DEACTIVATED);
        assertThat(user.isActive()).isFalse();
        assertThat(user.isDeactivated()).isTrue();

        user.setStatus(UserStatus.SUSPENDED);
        assertThat(user.isSuspended()).isTrue();
    }
}

