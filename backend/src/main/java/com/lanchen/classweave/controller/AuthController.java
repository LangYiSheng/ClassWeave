package com.lanchen.classweave.controller;

import com.lanchen.classweave.common.ApiResponse;
import com.lanchen.classweave.dto.auth.AuthResponse;
import com.lanchen.classweave.dto.auth.LoginRequest;
import com.lanchen.classweave.dto.auth.ResetPasswordRequest;
import com.lanchen.classweave.dto.auth.RegisterRequest;
import com.lanchen.classweave.security.UserPrincipal;
import com.lanchen.classweave.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/reset-password")
    public ApiResponse<Boolean> resetPassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        return ApiResponse.success(authService.resetPassword(principal.getId(), request));
    }
}
