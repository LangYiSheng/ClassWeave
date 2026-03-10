package com.lanchen.classweave.dto.share;

public record SharePreviewResponse(
        Long scheduleId,
        String scheduleName,
        String ownerDisplayName,
        String termLabel,
        String description,
        Boolean isExpired,
        Boolean requiresLoginToAccept
) {
}
