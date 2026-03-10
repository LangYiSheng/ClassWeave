package com.lanchen.classweave.dto.share;

import java.time.LocalDateTime;

public record ShareLinkResponse(
        Long id,
        Long scheduleId,
        String shareToken,
        String permissionType,
        Boolean isActive,
        LocalDateTime expiresAt,
        String shareUrl
) {
}
