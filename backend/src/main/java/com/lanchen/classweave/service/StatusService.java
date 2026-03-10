package com.lanchen.classweave.service;

import com.lanchen.classweave.common.ErrorCode;
import com.lanchen.classweave.domain.entity.ScheduleAccessEntity;
import com.lanchen.classweave.domain.entity.ScheduleCourseEntity;
import com.lanchen.classweave.domain.entity.ScheduleTimeSlotEntity;
import com.lanchen.classweave.dto.status.StatusPanelResponse;
import com.lanchen.classweave.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatusService {

    private final ScheduleService scheduleService;

    public StatusService(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @Transactional(readOnly = true)
    public StatusPanelResponse getStatusPanel(
            Long userId,
            List<Long> scheduleIds,
            LocalDate date,
            LocalTime time,
            Integer windowMinutes
    ) {
        if (scheduleIds == null || scheduleIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "scheduleIds 不能为空");
        }

        List<StatusPanelResponse.CurrentStatusItem> current = new ArrayList<>();
        List<StatusPanelResponse.FreeStatusItem> free = new ArrayList<>();
        List<StatusPanelResponse.SoonStartStatusItem> soonStart = new ArrayList<>();
        List<StatusPanelResponse.SoonEndStatusItem> soonEnd = new ArrayList<>();

        LocalDateTime now = LocalDateTime.of(date, time);
        for (Long scheduleId : scheduleIds) {
            ScheduleAccessEntity access = scheduleService.getAccessibleSchedule(userId, scheduleId);
            Map<Integer, ScheduleTimeSlotEntity> timeSlotMap = new HashMap<>();
            for (ScheduleTimeSlotEntity slot : scheduleService.getTimeSlots(scheduleId)) {
                timeSlotMap.put(slot.getPeriodIndex(), slot);
            }

            int weekNumber = resolveWeekNumber(access.getSchedule().getStartDate(), date);
            int weekday = date.getDayOfWeek().getValue();
            List<CourseWindow> matched = scheduleService.getCourses(scheduleId).stream()
                    .filter(course -> matchesDate(course, access.getSchedule().getTotalWeeks(), weekNumber, weekday))
                    .map(course -> toCourseWindow(course, timeSlotMap, date))
                    .filter(window -> window != null)
                    .sorted(Comparator.comparing(CourseWindow::startAt))
                    .toList();

            CourseWindow currentWindow = matched.stream()
                    .filter(window -> !now.isBefore(window.startAt()) && now.isBefore(window.endAt()))
                    .findFirst()
                    .orElse(null);

            String ownerName = displayOwner(access);
            if (currentWindow != null) {
                current.add(new StatusPanelResponse.CurrentStatusItem(
                        scheduleId,
                        access.getSchedule().getName(),
                        ownerName,
                        currentWindow.course().getId(),
                        currentWindow.course().getName(),
                        currentWindow.startAt().toLocalTime(),
                        currentWindow.endAt().toLocalTime(),
                        currentWindow.course().getLocation()
                ));
                long minutesLeft = Duration.between(now, currentWindow.endAt()).toMinutes();
                if (minutesLeft >= 0 && minutesLeft <= windowMinutes) {
                    soonEnd.add(new StatusPanelResponse.SoonEndStatusItem(
                            scheduleId,
                            access.getSchedule().getName(),
                            ownerName,
                            currentWindow.course().getId(),
                            currentWindow.course().getName(),
                            currentWindow.endAt().toLocalTime(),
                            minutesLeft
                    ));
                }
            } else {
                free.add(new StatusPanelResponse.FreeStatusItem(
                        scheduleId,
                        access.getSchedule().getName(),
                        ownerName
                ));
            }

            matched.stream()
                    .filter(window -> now.isBefore(window.startAt()))
                    .findFirst()
                    .ifPresent(window -> {
                        long minutesLeft = Duration.between(now, window.startAt()).toMinutes();
                        if (minutesLeft >= 0 && minutesLeft <= windowMinutes) {
                            soonStart.add(new StatusPanelResponse.SoonStartStatusItem(
                                    scheduleId,
                                    access.getSchedule().getName(),
                                    ownerName,
                                    window.course().getId(),
                                    window.course().getName(),
                                    window.startAt().toLocalTime(),
                                    minutesLeft
                            ));
                        }
                    });
        }

        return new StatusPanelResponse(date, time, windowMinutes, current, free, soonStart, soonEnd);
    }

    private boolean matchesDate(ScheduleCourseEntity course, Integer totalWeeks, int weekNumber, int weekday) {
        if (weekNumber < 1 || weekNumber > totalWeeks) {
            return false;
        }
        if (!course.getWeekday().equals(weekday)) {
            return false;
        }
        if (weekNumber < course.getStartWeek() || weekNumber > course.getEndWeek()) {
            return false;
        }
        return switch (course.getWeekType()) {
            case ALL -> true;
            case ODD -> weekNumber % 2 == 1;
            case EVEN -> weekNumber % 2 == 0;
        };
    }

    private CourseWindow toCourseWindow(
            ScheduleCourseEntity course,
            Map<Integer, ScheduleTimeSlotEntity> slotMap,
            LocalDate date
    ) {
        ScheduleTimeSlotEntity startSlot = slotMap.get(course.getStartPeriod());
        ScheduleTimeSlotEntity endSlot = slotMap.get(course.getEndPeriod());
        if (startSlot == null || endSlot == null) {
            return null;
        }
        LocalDateTime startAt = LocalDateTime.of(date, startSlot.getStartTime());
        LocalDateTime endAt = LocalDateTime.of(date, endSlot.getEndTime());
        return new CourseWindow(course, startAt, endAt);
    }

    private int resolveWeekNumber(LocalDate startDate, LocalDate date) {
        if (date.isBefore(startDate)) {
            return 0;
        }
        return Math.toIntExact(ChronoUnit.DAYS.between(startDate, date) / 7) + 1;
    }

    private String displayOwner(ScheduleAccessEntity access) {
        String displayName = access.getSchedule().getOwnerUser().getDisplayName();
        return displayName == null || displayName.isBlank()
                ? access.getSchedule().getOwnerUser().getUsername()
                : displayName;
    }

    private record CourseWindow(
            ScheduleCourseEntity course,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
    }
}
