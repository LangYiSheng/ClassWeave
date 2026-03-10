package com.lanchen.classweave.dto.schedule;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record TimeSlotInput(
        @NotNull(message = "periodIndex 不能为空")
        @Min(value = 1, message = "periodIndex 最小为 1")
        @Max(value = 20, message = "periodIndex 最大为 20")
        Integer periodIndex,
        @NotNull(message = "startTime 不能为空")
        LocalTime startTime,
        @NotNull(message = "endTime 不能为空")
        LocalTime endTime
) {
}
