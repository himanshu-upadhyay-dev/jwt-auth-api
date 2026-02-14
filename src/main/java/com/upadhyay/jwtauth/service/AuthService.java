package com.upadhyay.jwtauth.service;

import com.upadhyay.jwtauth.dto.request.LoginRequest;
import com.upadhyay.jwtauth.dto.request.RefreshTokenRequest;
import com.upadhyay.jwtauth.dto.request.SignupRequest;
import com.upadhyay.jwtauth.dto.response.AuthResponse;
import com.upadhyay.jwtauth.dto.response.UserResponse;

public interface AuthService {

    AuthResponse register(SignupRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    UserResponse getCurrentUser(String username);
}
