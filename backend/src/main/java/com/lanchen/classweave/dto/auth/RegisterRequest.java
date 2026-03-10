package com.lanchen.classweave.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "username 不能为空")
        @Size(max = 50, message = "username 长度不能超过 50")
        String username,
        @NotBlank(message = "password 不能为空")
        @Size(min = 8, max = 64, message = "password 长度应为 8-64")
        String password,
        @Size(max = 50, message = "displayName 长度不能超过 50")
        String displayName
) {
}
