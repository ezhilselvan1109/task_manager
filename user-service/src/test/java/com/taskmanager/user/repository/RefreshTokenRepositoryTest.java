package com.taskmanager.user.repository;

import com.taskmanager.user.entity.RefreshTokenEntity;
import com.taskmanager.user.entity.UserEntity;
import com.taskmanager.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "eureka.client.enabled=false",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(UserEntity.builder()
                .email("token.user@example.com")
                .passwordHash("$2a$12$hash")
                .fullName("Token User")
                .status(UserStatus.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("findByToken and findByTokenAndRevokedFalse operate accurately")
    void testFindByToken() {
        RefreshTokenEntity token = RefreshTokenEntity.builder()
                .user(testUser)
                .token("active-token-xyz")
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build();
        refreshTokenRepository.save(token);

        Optional<RefreshTokenEntity> found = refreshTokenRepository.findByToken("active-token-xyz");
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getEmail()).isEqualTo("token.user@example.com");

        Optional<RefreshTokenEntity> notRevoked = refreshTokenRepository.findByTokenAndRevokedFalse("active-token-xyz");
        assertThat(notRevoked).isPresent();

        token.setRevoked(true);
        refreshTokenRepository.save(token);

        assertThat(refreshTokenRepository.findByTokenAndRevokedFalse("active-token-xyz")).isEmpty();
    }

    @Test
    @DisplayName("revokeAllByUserId revokes all tokens belonging to specified user")
    void testRevokeAllByUserId() {
        RefreshTokenEntity token1 = RefreshTokenEntity.builder()
                .user(testUser)
                .token("token-1")
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build();
        RefreshTokenEntity token2 = RefreshTokenEntity.builder()
                .user(testUser)
                .token("token-2")
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build();
        refreshTokenRepository.save(token1);
        refreshTokenRepository.save(token2);

        refreshTokenRepository.revokeAllByUserId(testUser.getId());

        assertThat(refreshTokenRepository.findByTokenAndRevokedFalse("token-1")).isEmpty();
        assertThat(refreshTokenRepository.findByTokenAndRevokedFalse("token-2")).isEmpty();
    }
}

