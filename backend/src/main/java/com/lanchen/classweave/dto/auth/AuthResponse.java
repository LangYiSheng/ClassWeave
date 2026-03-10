package com.lanchen.classweave.dto.auth;

import com.lanchen.classweave.dto.user.UserResponse;

public record AuthResponse(
        String token,
        UserResponse user
) {
}
