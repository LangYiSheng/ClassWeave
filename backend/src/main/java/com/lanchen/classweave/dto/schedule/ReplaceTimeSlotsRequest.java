package com.lanchen.classweave.dto.schedule;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReplaceTimeSlotsRequest(
        @Valid
        @NotEmpty(message = "timeSlots 不能为空")
        List<TimeSlotInput> timeSlots
) {
}
