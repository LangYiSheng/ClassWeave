package com.lanchen.classweave.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "oldPassword 不能为空")
        @Size(min = 8, max = 64, message = "oldPassword 长度应为 8-64")
        String oldPassword,
        @NotBlank(message = "newPassword 不能为空")
        @Size(min = 8, max = 64, message = "newPassword 长度应为 8-64")
        String newPassword
) {
}
