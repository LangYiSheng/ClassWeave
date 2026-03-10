package com.lanchen.classweave.dto.schedule;

import java.time.LocalTime;

public record TimeSlotDto(
        Long id,
        Integer periodIndex,
        LocalTime startTime,
        LocalTime endTime
) {
}
