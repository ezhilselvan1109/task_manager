package com.taskmanager.user.security;

import com.taskmanager.user.entity.UserEntity;
import com.taskmanager.user.entity.UserStatus;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtProperties jwtProperties;
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("dGFza21hbmFnZXItc3VwZXItc2VjcmV0LWtleS0yNTYtYml0cy1taW5pbXVtLWZvci1oczI1Ng==");
        jwtProperties.setIssuer("task-manager-auth");
        jwtProperties.setAccessTokenExpirationSeconds(900L);
        jwtProperties.setRefreshTokenExpirationDays(7L);

        jwtTokenProvider = new JwtTokenProvider(jwtProperties);

        testUser = UserEntity.builder()
                .id(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"))
                .email("sarah.connor@example.com")
                .fullName("Sarah Connor")
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("generateAccessToken creates valid token with expected claims")
    void testGenerateAccessToken() {
        String token = jwtTokenProvider.generateAccessToken(testUser);
        assertThat(token).isNotBlank();

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.extractUserId(token)).isEqualTo(testUser.getId());
        assertThat(jwtTokenProvider.extractEmail(token)).isEqualTo("sarah.connor@example.com");
        assertThat(jwtTokenProvider.extractRole(token)).isEqualTo("ROLE_USER");

        Claims claims = jwtTokenProvider.extractAllClaims(token);
        assertThat(claims.getIssuer()).isEqualTo("task-manager-auth");
        assertThat(claims.getSubject()).isEqualTo("sarah.connor@example.com");
        assertThat(claims.getId()).isNotBlank();
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    @DisplayName("validateToken rejects tampered or malformed tokens")
    void testTamperedAndMalformedToken() {
        String token = jwtTokenProvider.generateAccessToken(testUser);
        String tamperedToken = token.substring(0, token.length() - 5) + "abcde";

        assertThat(jwtTokenProvider.validateToken(tamperedToken)).isFalse();
        assertThat(jwtTokenProvider.validateToken("not-a-valid-jwt")).isFalse();
        assertThat(jwtTokenProvider.validateToken(null)).isFalse();
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
    }

    @Test
    @DisplayName("generateRefreshToken generates cryptographically secure token string")
    void testGenerateRefreshToken() {
        String refreshToken1 = jwtTokenProvider.generateRefreshToken();
        String refreshToken2 = jwtTokenProvider.generateRefreshToken();

        assertThat(refreshToken1).isNotBlank();
        assertThat(refreshToken2).isNotBlank();
        assertThat(refreshToken1).isNotEqualTo(refreshToken2);
    }

    @Test
    @DisplayName("JwtTokenProvider handles raw string secrets and short keys gracefully")
    void testShortAndNonBase64Secrets() {
        JwtProperties rawProps = new JwtProperties();
        rawProps.setSecret("short-raw-secret");
        JwtTokenProvider rawProvider = new JwtTokenProvider(rawProps);

        String token = rawProvider.generateAccessToken(testUser);
        assertThat(rawProvider.validateToken(token)).isTrue();
    }
}
