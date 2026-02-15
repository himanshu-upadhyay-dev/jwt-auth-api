package com.upadhyay.jwtauth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.upadhyay.jwtauth.dto.common.ApiResponse;
import com.upadhyay.jwtauth.dto.request.LoginRequest;
import com.upadhyay.jwtauth.dto.request.RefreshTokenRequest;
import com.upadhyay.jwtauth.dto.request.SignupRequest;
import com.upadhyay.jwtauth.dto.response.AuthResponse;
import com.upadhyay.jwtauth.dto.response.UserResponse;
import com.upadhyay.jwtauth.security.CustomUserDetails;
import com.upadhyay.jwtauth.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration, login, token refresh and profile endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "Register a new user",
            description = "Creates a new user account with ROLE_USER and returns an access + refresh token pair")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.created(response, "User registered successfully")
        );
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user",
            description = "Authenticates a user by username/email + password and returns a JWT token pair")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token",
            description = "Issues a new access token (and rotates refresh token) using a valid refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get current user profile",
            description = "Returns profile of the currently authenticated user (requires valid access token)")
    public ResponseEntity<ApiResponse<UserResponse>> me(@AuthenticationPrincipal CustomUserDetails principal) {
        UserResponse user = authService.getCurrentUser(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(user, "Profile fetched successfully"));
    }
}
