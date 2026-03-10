package com.lanchen.classweave.dto.schedule;

import com.lanchen.classweave.domain.enums.WeekType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseUpsertRequest(
        @NotBlank(message = "name 不能为空")
        @Size(max = 100, message = "name 长度不能超过 100")
        String name,
        @NotNull(message = "weekday 不能为空")
        @Min(value = 1, message = "weekday 最小为 1")
        @Max(value = 7, message = "weekday 最大为 7")
        Integer weekday,
        @NotNull(message = "startWeek 不能为空")
        @Min(value = 1, message = "startWeek 最小为 1")
        Integer startWeek,
        @NotNull(message = "endWeek 不能为空")
        @Min(value = 1, message = "endWeek 最小为 1")
        Integer endWeek,
        @NotNull(message = "startPeriod 不能为空")
        @Min(value = 1, message = "startPeriod 最小为 1")
        @Max(value = 20, message = "startPeriod 最大为 20")
        Integer startPeriod,
        @NotNull(message = "endPeriod 不能为空")
        @Min(value = 1, message = "endPeriod 最小为 1")
        @Max(value = 20, message = "endPeriod 最大为 20")
        Integer endPeriod,
        WeekType weekType,
        @Size(max = 100, message = "teacher 长度不能超过 100")
        String teacher,
        @Size(max = 100, message = "location 长度不能超过 100")
        String location,
        @Size(max = 500, message = "note 长度不能超过 500")
        String note,
        Boolean isTemporary
) {
}
