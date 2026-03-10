package com.lanchen.classweave.dto.schedule;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ScheduleSummaryResponse(
        Long id,
        String name,
        String termLabel,
        String description,
        LocalDate startDate,
        Integer totalWeeks,
        Integer maxPeriodsPerDay,
        String defaultColor,
        Long ownerUserId,
        String ownerDisplayName,
        String accessRole,
        String accessSource,
        String displayNameOverride,
        String displayColor,
        BigDecimal displayOpacity,
        Boolean isVisibleDefault
) {
}
