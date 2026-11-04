package com.taskmanager.user.service.impl;

import com.taskmanager.user.dto.request.UpdateProfileRequest;
import com.taskmanager.user.dto.response.UserProfileResponse;
import com.taskmanager.user.entity.UserEntity;
import com.taskmanager.user.entity.UserStatus;
import com.taskmanager.user.exception.ResourceNotFoundException;
import com.taskmanager.user.mapper.UserMapper;
import com.taskmanager.user.repository.UserRepository;
import com.taskmanager.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentProfile(UUID userId) {
        UserEntity user = userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        UserEntity user = userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setFullName(request.fullName().trim());
        UserEntity savedUser = userRepository.save(user);

        log.info("Profile updated for user id: {}", userId);
        return userMapper.toResponse(savedUser);
    }
}

