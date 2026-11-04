package com.taskmanager.user.service;

import com.taskmanager.user.dto.request.UpdateProfileRequest;
import com.taskmanager.user.dto.response.UserProfileResponse;

import java.util.UUID;

public interface UserService {

    UserProfileResponse getCurrentProfile(UUID userId);

    UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request);
}

