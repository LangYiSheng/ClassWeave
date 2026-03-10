package com.lanchen.classweave.dto.schedule;

import jakarta.validation.constraints.Size;

public record UpdateDisplayNameRequest(
        @Size(max = 100, message = "displayNameOverride 长度不能超过 100")
        String displayNameOverride
) {
}
