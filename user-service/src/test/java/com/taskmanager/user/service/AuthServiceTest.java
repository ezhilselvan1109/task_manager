package com.taskmanager.user.service;

import com.taskmanager.user.dto.request.LoginRequest;
import com.taskmanager.user.dto.request.RefreshTokenRequest;
import com.taskmanager.user.dto.request.RegisterRequest;
import com.taskmanager.user.dto.response.AuthResponse;
import com.taskmanager.user.dto.response.UserProfileResponse;
import com.taskmanager.user.entity.RefreshTokenEntity;
import com.taskmanager.user.entity.UserEntity;
import com.taskmanager.user.entity.UserStatus;
import com.taskmanager.user.exception.AccountDisabledException;
import com.taskmanager.user.exception.EmailAlreadyExistsException;
import com.taskmanager.user.exception.InvalidRefreshTokenException;
import com.taskmanager.user.mapper.UserMapper;
import com.taskmanager.user.repository.RefreshTokenRepository;
import com.taskmanager.user.repository.UserRepository;
import com.taskmanager.user.security.JwtTokenProvider;
import com.taskmanager.user.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserEntity activeUser;

    @BeforeEach
    void setUp() {
        activeUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("sarah.connor@example.com")
                .passwordHash("$2a$12$encodedHash")
                .fullName("Sarah Connor")
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("register successfully encodes password, persists user, and returns UserProfileResponse")
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest("sarah.connor@example.com", "Password123!", "Sarah Connor");

        when(userRepository.existsByEmailIgnoreCase("sarah.connor@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("$2a$12$encodedHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(activeUser);
        when(userMapper.toResponse(activeUser))
                .thenReturn(new UserProfileResponse(activeUser.getId(), "sarah.connor@example.com", "Sarah Connor", activeUser.getCreatedAt()));

        UserProfileResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("sarah.connor@example.com");
        assertThat(response.fullName()).isEqualTo("Sarah Connor");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("register throws EmailAlreadyExistsException when email is already taken")
    void testRegisterDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("sarah.connor@example.com", "Password123!", "Sarah Connor");
        when(userRepository.existsByEmailIgnoreCase("sarah.connor@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    @DisplayName("login successfully verifies password, generates JWT, and saves refresh token")
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest("sarah.connor@example.com", "Password123!");

        when(userRepository.findByEmailIgnoreCase("sarah.connor@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("Password123!", activeUser.getPasswordHash())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(activeUser)).thenReturn("jwt.access.token");
        when(jwtTokenProvider.generateRefreshToken()).thenReturn("secure-refresh-token");

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("jwt.access.token");
        assertThat(response.refreshToken()).isEqualTo("secure-refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900L);
        verify(refreshTokenRepository).save(any(RefreshTokenEntity.class));
    }

    @Test
    @DisplayName("login throws BadCredentialsException on password mismatch")
    void testLoginPasswordMismatch() {
        LoginRequest request = new LoginRequest("sarah.connor@example.com", "WrongPassword!");

        when(userRepository.findByEmailIgnoreCase("sarah.connor@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("WrongPassword!", activeUser.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    @DisplayName("login throws BadCredentialsException when email not found")
    void testLoginEmailNotFound() {
        LoginRequest request = new LoginRequest("unknown@example.com", "Password123!");
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    @DisplayName("login throws AccountDisabledException when user status is DEACTIVATED")
    void testLoginDeactivatedAccount() {
        activeUser.setStatus(UserStatus.DEACTIVATED);
        LoginRequest request = new LoginRequest("sarah.connor@example.com", "Password123!");

        when(userRepository.findByEmailIgnoreCase("sarah.connor@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("Password123!", activeUser.getPasswordHash())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AccountDisabledException.class)
                .hasMessageContaining("deactivated or suspended");
    }

    @Test
    @DisplayName("refresh rotates refresh token and mints new access token")
    void testRefreshSuccess() {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        RefreshTokenEntity existingToken = RefreshTokenEntity.builder()
                .id(UUID.randomUUID())
                .user(activeUser)
                .token("valid-refresh-token")
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenAndRevokedFalse("valid-refresh-token")).thenReturn(Optional.of(existingToken));
        when(jwtTokenProvider.generateAccessToken(activeUser)).thenReturn("new.access.token");
        when(jwtTokenProvider.generateRefreshToken()).thenReturn("new-refresh-token");

        AuthResponse response = authService.refresh(request);

        assertThat(response.accessToken()).isEqualTo("new.access.token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(existingToken.isRevoked()).isTrue();
        verify(refreshTokenRepository, org.mockito.Mockito.times(2)).save(any(RefreshTokenEntity.class));
    }

    @Test
    @DisplayName("refresh throws InvalidRefreshTokenException when token is expired or revoked")
    void testRefreshInvalidToken() {
        RefreshTokenRequest request = new RefreshTokenRequest("expired-token");
        RefreshTokenEntity expiredToken = RefreshTokenEntity.builder()
                .id(UUID.randomUUID())
                .user(activeUser)
                .token("expired-token")
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenAndRevokedFalse("expired-token")).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }
}

