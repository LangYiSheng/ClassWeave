package com.lanchen.classweave.dto.schedule;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ImportScheduleRequest(
        @Valid
        @NotNull(message = "schedule 不能为空")
        CreateScheduleRequest schedule,
        @Valid
        @NotEmpty(message = "timeSlots 不能为空")
        List<TimeSlotInput> timeSlots,
        @Valid
        @NotNull(message = "courses 不能为空")
        List<CourseUpsertRequest> courses
) {
}
