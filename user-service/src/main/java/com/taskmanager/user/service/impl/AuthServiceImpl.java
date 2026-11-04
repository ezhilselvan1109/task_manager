package com.taskmanager.user.service.impl;

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
import com.taskmanager.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserProfileResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            log.warn("Registration rejected: email {} is already registered", normalizedEmail);
            throw new EmailAlreadyExistsException("Email is already registered: " + normalizedEmail);
        }

        UserEntity user = UserEntity.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName().trim())
                .status(UserStatus.ACTIVE)
                .build();

        UserEntity savedUser = userRepository.save(user);
        log.info("New user registered successfully with id: {}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        UserEntity user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("Login failed: email {} not found", normalizedEmail);
                    return new BadCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Login failed: password mismatch for user {}", user.getId());
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.isActive()) {
            log.warn("Login rejected: user account {} is {}", user.getId(), user.getStatus());
            throw new AccountDisabledException("User account is deactivated or suspended");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken();

        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("User {} successfully authenticated", user.getId());

        return AuthResponse.of(accessToken, refreshTokenValue);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshTokenEntity existingToken = refreshTokenRepository.findByTokenAndRevokedFalse(request.refreshToken())
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token is invalid or expired"));

        if (existingToken.isExpired()) {
            existingToken.setRevoked(true);
            refreshTokenRepository.save(existingToken);
            throw new InvalidRefreshTokenException("Refresh token has expired");
        }

        UserEntity user = existingToken.getUser();
        if (!user.isActive()) {
            throw new AccountDisabledException("User account is deactivated or suspended");
        }

        // Rotate token
        existingToken.setRevoked(true);
        refreshTokenRepository.save(existingToken);

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshTokenValue = jwtTokenProvider.generateRefreshToken();

        RefreshTokenEntity newRefreshToken = RefreshTokenEntity.builder()
                .user(user)
                .token(newRefreshTokenValue)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        refreshTokenRepository.save(newRefreshToken);
        log.info("Refreshed access token for user {}", user.getId());

        return AuthResponse.of(newAccessToken, newRefreshTokenValue);
    }
}

