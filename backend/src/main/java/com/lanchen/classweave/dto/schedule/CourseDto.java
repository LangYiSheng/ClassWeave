package com.lanchen.classweave.dto.schedule;

public record CourseDto(
        Long id,
        String name,
        Integer weekday,
        Integer startWeek,
        Integer endWeek,
        Integer startPeriod,
        Integer endPeriod,
        String weekType,
        String teacher,
        String location,
        String note,
        Boolean isTemporary
) {
}
