package com.lanchen.classweave.dto.user;

public record UserResponse(
        Long id,
        String username,
        String displayName
) {
}
