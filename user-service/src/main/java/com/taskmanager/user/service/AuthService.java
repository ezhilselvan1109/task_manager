package com.taskmanager.user.service;

import com.taskmanager.user.dto.request.LoginRequest;
import com.taskmanager.user.dto.request.RefreshTokenRequest;
import com.taskmanager.user.dto.request.RegisterRequest;
import com.taskmanager.user.dto.response.AuthResponse;
import com.taskmanager.user.dto.response.UserProfileResponse;

public interface AuthService {

    UserProfileResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);
}

