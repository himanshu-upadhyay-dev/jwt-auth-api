package com.upadhyay.jwtauth.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.upadhyay.jwtauth.dto.common.ApiResponse;
import com.upadhyay.jwtauth.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/test")
@Tag(name = "Test", description = "Sample protected endpoints to verify role-based access control")
public class TestController {

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "User-only endpoint", description = "Requires ROLE_USER")
    public ResponseEntity<ApiResponse<Map<String, String>>> userEndpoint(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("username", user.getUsername(), "access", "USER"),
                "Accessed USER resource"
        ));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Admin-only endpoint", description = "Requires ROLE_ADMIN")
    public ResponseEntity<ApiResponse<Map<String, String>>> adminEndpoint(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("username", user.getUsername(), "access", "ADMIN"),
                "Accessed ADMIN resource"
        ));
    }
}
