package com.lanchen.classweave.dto.schedule;

import java.util.List;

public record ScheduleDetailResponse(
        ScheduleSummaryResponse schedule,
        List<TimeSlotDto> timeSlots,
        List<CourseDto> courses
) {
}
