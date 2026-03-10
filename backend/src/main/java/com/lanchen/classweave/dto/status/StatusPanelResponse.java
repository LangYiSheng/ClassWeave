package com.lanchen.classweave.dto.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record StatusPanelResponse(
        LocalDate date,
        LocalTime time,
        Integer windowMinutes,
        List<CurrentStatusItem> current,
        List<FreeStatusItem> free,
        List<SoonStartStatusItem> soonStart,
        List<SoonEndStatusItem> soonEnd
) {

    public record CurrentStatusItem(
            Long scheduleId,
            String scheduleName,
            String ownerDisplayName,
            Long courseId,
            String courseName,
            LocalTime startTime,
            LocalTime endTime,
            String location
    ) {
    }

    public record FreeStatusItem(
            Long scheduleId,
            String scheduleName,
            String ownerDisplayName
    ) {
    }

    public record SoonStartStatusItem(
            Long scheduleId,
            String scheduleName,
            String ownerDisplayName,
            Long courseId,
            String courseName,
            LocalTime startTime,
            Long minutesLeft
    ) {
    }

    public record SoonEndStatusItem(
            Long scheduleId,
            String scheduleName,
            String ownerDisplayName,
            Long courseId,
            String courseName,
            LocalTime endTime,
            Long minutesLeft
    ) {
    }
}
