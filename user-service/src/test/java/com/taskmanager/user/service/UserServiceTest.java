package com.taskmanager.user.service;

import com.taskmanager.user.dto.request.UpdateProfileRequest;
import com.taskmanager.user.dto.response.UserProfileResponse;
import com.taskmanager.user.entity.UserEntity;
import com.taskmanager.user.entity.UserStatus;
import com.taskmanager.user.exception.ResourceNotFoundException;
import com.taskmanager.user.mapper.UserMapper;
import com.taskmanager.user.repository.UserRepository;
import com.taskmanager.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UserEntity user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = UserEntity.builder()
                .id(userId)
                .email("john.connor@example.com")
                .fullName("John Connor")
                .passwordHash("$2a$12$hash")
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("getCurrentProfile returns user profile for active user")
    void testGetCurrentProfileSuccess() {
        when(userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user))
                .thenReturn(new UserProfileResponse(userId, "john.connor@example.com", "John Connor", user.getCreatedAt()));

        UserProfileResponse response = userService.getCurrentProfile(userId);

        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.fullName()).isEqualTo("John Connor");
    }

    @Test
    @DisplayName("getCurrentProfile throws ResourceNotFoundException when user is inactive or not found")
    void testGetCurrentProfileNotFound() {
        when(userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentProfile(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("updateProfile updates user full name and persists changes")
    void testUpdateProfileSuccess() {
        UpdateProfileRequest request = new UpdateProfileRequest("General John Connor");

        when(userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);
        when(userMapper.toResponse(user))
                .thenReturn(new UserProfileResponse(userId, "john.connor@example.com", "General John Connor", user.getCreatedAt()));

        UserProfileResponse response = userService.updateProfile(userId, request);

        assertThat(response.fullName()).isEqualTo("General John Connor");
        assertThat(user.getFullName()).isEqualTo("General John Connor");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("updateProfile throws ResourceNotFoundException when user does not exist")
    void testUpdateProfileNotFound() {
        UpdateProfileRequest request = new UpdateProfileRequest("New Name");
        when(userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile(userId, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

