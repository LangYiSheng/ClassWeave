package com.lanchen.classweave.dto.schedule;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateScheduleRequest(
        @NotBlank(message = "name 不能为空")
        @Size(max = 100, message = "name 长度不能超过 100")
        String name,
        @Size(max = 100, message = "termLabel 长度不能超过 100")
        String termLabel,
        @Size(max = 255, message = "description 长度不能超过 255")
        String description,
        @NotNull(message = "startDate 不能为空")
        LocalDate startDate,
        @NotNull(message = "totalWeeks 不能为空")
        @Min(value = 1, message = "totalWeeks 最小为 1")
        @Max(value = 30, message = "totalWeeks 最大为 30")
        Integer totalWeeks,
        @NotNull(message = "maxPeriodsPerDay 不能为空")
        @Min(value = 1, message = "maxPeriodsPerDay 最小为 1")
        @Max(value = 20, message = "maxPeriodsPerDay 最大为 20")
        Integer maxPeriodsPerDay,
        @Size(max = 20, message = "defaultColor 长度不能超过 20")
        String defaultColor
) {
}
