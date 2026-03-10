package com.lanchen.classweave.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "username 不能为空")
        @Size(max = 50, message = "username 长度不能超过 50")
        String username,
        @NotBlank(message = "password 不能为空")
        @Size(max = 64, message = "password 长度不能超过 64")
        String password
) {
}
