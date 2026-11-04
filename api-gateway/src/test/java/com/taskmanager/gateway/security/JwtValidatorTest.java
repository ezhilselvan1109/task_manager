package com.taskmanager.gateway.security;

import com.taskmanager.gateway.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtValidatorTest {

    private static final String SECRET = "dGFza21hbmFnZXItc3VwZXItc2VjcmV0LWtleS0yNTYtYml0cy1taW5pbXVtLWZvci1oczI1Ng==";
    private static final String ISSUER = "task-manager-auth";

    private JwtValidator jwtValidator;
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setIssuer(ISSUER);

        jwtValidator = new JwtValidator(properties);
        signingKey = Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(SECRET));
    }

    @Test
    @DisplayName("Should successfully validate a valid access token with all required claims")
    void testValidAccessToken() {
        UUID userId = UUID.randomUUID();
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(ISSUER)
                .subject("user@example.com")
                .claim("userId", userId.toString())
                .claim("role", "ROLE_USER")
                .claim("email", "user@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        JwtValidationResult result = jwtValidator.validate(token);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getIdentity()).isNotNull();
        assertThat(result.getIdentity().userId()).isEqualTo(userId);
        assertThat(result.getIdentity().role()).isEqualTo("ROLE_USER");
        assertThat(result.getIdentity().email()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("Should fallback to subject when email claim is not present")
    void testSubjectFallbackWhenEmailClaimAbsent() {
        UUID userId = UUID.randomUUID();
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(ISSUER)
                .subject("fallback@example.com")
                .claim("userId", userId.toString())
                .claim("role", "ROLE_USER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        JwtValidationResult result = jwtValidator.validate(token);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getIdentity().email()).isEqualTo("fallback@example.com");
    }

    @Test
    @DisplayName("Should support raw non-base64 secret and short secret padding in resolveSigningKey")
    void testRawSecretResolution() {
        JwtProperties rawProperties = new JwtProperties();
        rawProperties.setSecret("short-raw-secret");
        rawProperties.setIssuer(null); // tests null issuer branch

        JwtValidator rawValidator = new JwtValidator(rawProperties);
        assertThat(rawValidator).isNotNull();
    }

    @Test
    @DisplayName("Should reject null or blank token")
    void testNullOrBlankToken() {
        JwtValidationResult resultNull = jwtValidator.validate(null);
        assertThat(resultNull.isValid()).isFalse();
        assertThat(resultNull.getErrorCode()).isEqualTo("MISSING_TOKEN");

        JwtValidationResult resultBlank = jwtValidator.validate("   ");
        assertThat(resultBlank.isValid()).isFalse();
        assertThat(resultBlank.getErrorCode()).isEqualTo("MISSING_TOKEN");
    }

    @Test
    @DisplayName("Should reject malformed token")
    void testMalformedToken() {
        JwtValidationResult result = jwtValidator.validate("not.a.valid.jwt.token");
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("MALFORMED_TOKEN");
    }

    @Test
    @DisplayName("Should reject expired token")
    void testExpiredToken() {
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(ISSUER)
                .subject("user@example.com")
                .claim("userId", UUID.randomUUID().toString())
                .claim("role", "ROLE_USER")
                .claim("email", "user@example.com")
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        JwtValidationResult result = jwtValidator.validate(token);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("EXPIRED_TOKEN");
    }

    @Test
    @DisplayName("Should reject token with invalid signature")
    void testInvalidSignature() {
        SecretKey wrongKey = Keys.hmacShaKeyFor("different-super-secret-key-that-is-at-least-256-bits-long!!".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(ISSUER)
                .subject("user@example.com")
                .claim("userId", UUID.randomUUID().toString())
                .claim("role", "ROLE_USER")
                .claim("email", "user@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(wrongKey, Jwts.SIG.HS256)
                .compact();

        JwtValidationResult result = jwtValidator.validate(token);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("INVALID_SIGNATURE");
    }

    @Test
    @DisplayName("Should reject token with invalid issuer")
    void testInvalidIssuer() {
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer("rogue-auth-service")
                .subject("user@example.com")
                .claim("userId", UUID.randomUUID().toString())
                .claim("role", "ROLE_USER")
                .claim("email", "user@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        JwtValidationResult result = jwtValidator.validate(token);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("INVALID_ISSUER");
    }

    @Test
    @DisplayName("Should reject token missing userId claim")
    void testMissingUserIdClaim() {
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(ISSUER)
                .subject("user@example.com")
                .claim("role", "ROLE_USER")
                .claim("email", "user@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        JwtValidationResult result = jwtValidator.validate(token);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("MISSING_CLAIMS");
    }

    @Test
    @DisplayName("Should reject token with malformed UUID in userId claim")
    void testMalformedUserIdClaim() {
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(ISSUER)
                .subject("user@example.com")
                .claim("userId", "not-a-valid-uuid")
                .claim("role", "ROLE_USER")
                .claim("email", "user@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        JwtValidationResult result = jwtValidator.validate(token);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("INVALID_USER_ID");
    }

    @Test
    @DisplayName("Should reject token missing role claim")
    void testMissingRoleClaim() {
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(ISSUER)
                .subject("user@example.com")
                .claim("userId", UUID.randomUUID().toString())
                .claim("email", "user@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        JwtValidationResult result = jwtValidator.validate(token);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("MISSING_CLAIMS");
    }

    @Test
    @DisplayName("Should reject opaque refresh token or token marked as refresh token")
    void testRefreshTokenRejection() {
        // Test opaque refresh token string
        String opaqueRefreshToken = "dGFza21hbmFnZXItcmVmcmVzaC10b2tlbi1yYW5kb20tc3RyaW5n";
        JwtValidationResult resultOpaque = jwtValidator.validate(opaqueRefreshToken);
        assertThat(resultOpaque.isValid()).isFalse();

        // Test JWT with type=refresh claim
        String jwtRefreshToken = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(ISSUER)
                .subject("user@example.com")
                .claim("type", "refresh")
                .claim("userId", UUID.randomUUID().toString())
                .claim("role", "ROLE_USER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        JwtValidationResult resultType = jwtValidator.validate(jwtRefreshToken);
        assertThat(resultType.isValid()).isFalse();
        assertThat(resultType.getErrorCode()).isEqualTo("INVALID_TOKEN_TYPE");
    }
}
