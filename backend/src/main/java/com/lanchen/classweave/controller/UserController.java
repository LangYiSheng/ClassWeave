package com.lanchen.classweave.controller;

import com.lanchen.classweave.common.ApiResponse;
import com.lanchen.classweave.dto.user.UpdateCurrentUserRequest;
import com.lanchen.classweave.dto.user.UserResponse;
import com.lanchen.classweave.security.UserPrincipal;
import com.lanchen.classweave.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success(userService.getCurrentUser(principal.getId()));
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateMe(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateCurrentUserRequest request
    ) {
        return ApiResponse.success(userService.updateCurrentUser(principal.getId(), request));
    }
}
