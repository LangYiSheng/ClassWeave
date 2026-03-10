package com.lanchen.classweave.service;

import com.lanchen.classweave.common.ErrorCode;
import com.lanchen.classweave.domain.entity.UserEntity;
import com.lanchen.classweave.domain.repository.UserRepository;
import com.lanchen.classweave.dto.auth.AuthResponse;
import com.lanchen.classweave.dto.auth.LoginRequest;
import com.lanchen.classweave.dto.auth.ResetPasswordRequest;
import com.lanchen.classweave.dto.auth.RegisterRequest;
import com.lanchen.classweave.exception.BusinessException;
import com.lanchen.classweave.security.JwtService;
import com.lanchen.classweave.security.UserPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UserService userService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        UserEntity saved = userRepository.save(user);
        UserPrincipal principal = new UserPrincipal(saved);
        return new AuthResponse(jwtService.generateToken(principal), userService.toUserResponse(saved));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        UserPrincipal principal = new UserPrincipal(user);
        return new AuthResponse(jwtService.generateToken(principal), userService.toUserResponse(user));
    }

    @Transactional
    public boolean resetPassword(Long userId, ResetPasswordRequest request) {
        UserEntity user = userService.findById(userId);
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "旧密码错误");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        return true;
    }
}
