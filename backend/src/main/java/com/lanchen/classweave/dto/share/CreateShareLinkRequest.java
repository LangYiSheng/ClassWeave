package com.lanchen.classweave.dto.share;

import java.time.LocalDateTime;

public record CreateShareLinkRequest(
        LocalDateTime expiresAt
) {
}
