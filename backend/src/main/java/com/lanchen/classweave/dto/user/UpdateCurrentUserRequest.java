package com.lanchen.classweave.dto.user;

import jakarta.validation.constraints.Size;

public record UpdateCurrentUserRequest(
        @Size(max = 50, message = "displayName 长度不能超过 50")
        String displayName
) {
}
