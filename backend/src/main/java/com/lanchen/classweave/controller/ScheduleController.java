package com.lanchen.classweave.controller;

import com.lanchen.classweave.common.ApiResponse;
import com.lanchen.classweave.common.IdResponse;
import com.lanchen.classweave.common.ScheduleIdResponse;
import com.lanchen.classweave.dto.schedule.CourseUpsertRequest;
import com.lanchen.classweave.dto.schedule.CreateScheduleRequest;
import com.lanchen.classweave.dto.schedule.ImportScheduleRequest;
import com.lanchen.classweave.dto.schedule.ReplaceTimeSlotsRequest;
import com.lanchen.classweave.dto.schedule.ScheduleDetailResponse;
import com.lanchen.classweave.dto.schedule.ScheduleSummaryResponse;
import com.lanchen.classweave.dto.schedule.UpdateDisplayNameRequest;
import com.lanchen.classweave.dto.schedule.UpdateDisplaySettingsRequest;
import com.lanchen.classweave.dto.schedule.UpdateScheduleRequest;
import com.lanchen.classweave.security.UserPrincipal;
import com.lanchen.classweave.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public ApiResponse<List<ScheduleSummaryResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "false") boolean includeArchived
    ) {
        return ApiResponse.success(scheduleService.listAccessibleSchedules(principal.getId(), includeArchived));
    }

    @PostMapping
    public ApiResponse<IdResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateScheduleRequest request
    ) {
        return ApiResponse.success(scheduleService.createSchedule(principal.getId(), request));
    }

    @GetMapping("/{scheduleId}")
    public ApiResponse<ScheduleDetailResponse> detail(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long scheduleId
    ) {
        return ApiResponse.success(scheduleService.getScheduleDetail(principal.getId(), scheduleId));
    }

    @PutMapping("/{scheduleId}")
    public ApiResponse<Void> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long scheduleId,
            @Valid @RequestBody UpdateScheduleRequest request
    ) {
        scheduleService.updateSchedule(principal.getId(), scheduleId, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{scheduleId}")
    public ApiResponse<Boolean> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long scheduleId
    ) {
        return ApiResponse.success(scheduleService.deleteSchedule(principal.getId(), scheduleId));
    }

    @DeleteMapping("/{scheduleId}/access")
    public ApiResponse<Boolean> removeSharedSchedule(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long scheduleId
    ) {
        return ApiResponse.success(scheduleService.removeSharedSchedule(principal.getId(), scheduleId));
    }

    @PutMapping("/{scheduleId}/display-settings")
    public ApiResponse<Void> updateDisplaySettings(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long scheduleId,
            @Valid @RequestBody UpdateDisplaySettingsRequest request
    ) {
        scheduleService.updateDisplaySettings(principal.getId(), scheduleId, request);
        return ApiResponse.success();
    }

    @PutMapping("/{scheduleId}/display-name")
    public ApiResponse<Void> updateDisplayName(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long scheduleId,
            @Valid @RequestBody UpdateDisplayNameRequest request
    ) {
        scheduleService.updateDisplayName(principal.getId(), scheduleId, request);
        return ApiResponse.success();
    }

    @PutMapping("/{scheduleId}/time-slots")
    public ApiResponse<Void> replaceTimeSlots(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long scheduleId,
            @Valid @RequestBody ReplaceTimeSlotsRequest request
    ) {
        scheduleService.replaceTimeSlots(principal.getId(), scheduleId, request);
        return ApiResponse.success();
    }

    @PostMapping("/{scheduleId}/courses")
    public ApiResponse<IdResponse> addCourse(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long scheduleId,
            @Valid @RequestBody CourseUpsertRequest request
    ) {
        return ApiResponse.success(scheduleService.addCourse(principal.getId(), scheduleId, request));
    }

    @PutMapping("/{scheduleId}/courses/{courseId}")
    public ApiResponse<Void> updateCourse(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long scheduleId,
            @PathVariable Long courseId,
            @Valid @RequestBody CourseUpsertRequest request
    ) {
        scheduleService.updateCourse(principal.getId(), scheduleId, courseId, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{scheduleId}/courses/{courseId}")
    public ApiResponse<Boolean> deleteCourse(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long scheduleId,
            @PathVariable Long courseId
    ) {
        return ApiResponse.success(scheduleService.deleteCourse(principal.getId(), scheduleId, courseId));
    }

    @PostMapping("/import/json")
    public ApiResponse<ScheduleIdResponse> importJson(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ImportScheduleRequest request
    ) {
        return ApiResponse.success(scheduleService.importScheduleFromJson(principal.getId(), request));
    }
}
