package com.lanchen.classweave.service;

import com.lanchen.classweave.common.ErrorCode;
import com.lanchen.classweave.common.IdResponse;
import com.lanchen.classweave.common.ScheduleIdResponse;
import com.lanchen.classweave.domain.entity.ScheduleAccessEntity;
import com.lanchen.classweave.domain.entity.ScheduleCourseEntity;
import com.lanchen.classweave.domain.entity.ScheduleEntity;
import com.lanchen.classweave.domain.entity.ScheduleTimeSlotEntity;
import com.lanchen.classweave.domain.entity.UserEntity;
import com.lanchen.classweave.domain.enums.AccessRole;
import com.lanchen.classweave.domain.enums.AccessSource;
import com.lanchen.classweave.domain.enums.WeekType;
import com.lanchen.classweave.domain.repository.ScheduleAccessRepository;
import com.lanchen.classweave.domain.repository.ScheduleCourseRepository;
import com.lanchen.classweave.domain.repository.ScheduleRepository;
import com.lanchen.classweave.domain.repository.ScheduleShareLinkRepository;
import com.lanchen.classweave.domain.repository.ScheduleTimeSlotRepository;
import com.lanchen.classweave.dto.schedule.CourseDto;
import com.lanchen.classweave.dto.schedule.CourseUpsertRequest;
import com.lanchen.classweave.dto.schedule.CreateScheduleRequest;
import com.lanchen.classweave.dto.schedule.ImportScheduleRequest;
import com.lanchen.classweave.dto.schedule.ReplaceTimeSlotsRequest;
import com.lanchen.classweave.dto.schedule.ScheduleDetailResponse;
import com.lanchen.classweave.dto.schedule.ScheduleSummaryResponse;
import com.lanchen.classweave.dto.schedule.TimeSlotDto;
import com.lanchen.classweave.dto.schedule.TimeSlotInput;
import com.lanchen.classweave.dto.schedule.UpdateDisplayNameRequest;
import com.lanchen.classweave.dto.schedule.UpdateDisplaySettingsRequest;
import com.lanchen.classweave.dto.schedule.UpdateScheduleRequest;
import com.lanchen.classweave.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ScheduleService {

    private static final String DEFAULT_COLOR = "#1F6FEB";
    private static final BigDecimal DEFAULT_OPACITY = new BigDecimal("0.85");

    private final ScheduleRepository scheduleRepository;
    private final ScheduleAccessRepository scheduleAccessRepository;
    private final ScheduleTimeSlotRepository timeSlotRepository;
    private final ScheduleCourseRepository courseRepository;
    private final ScheduleShareLinkRepository shareLinkRepository;
    private final UserService userService;

    public ScheduleService(
            ScheduleRepository scheduleRepository,
            ScheduleAccessRepository scheduleAccessRepository,
            ScheduleTimeSlotRepository timeSlotRepository,
            ScheduleCourseRepository courseRepository,
            ScheduleShareLinkRepository shareLinkRepository,
            UserService userService
    ) {
        this.scheduleRepository = scheduleRepository;
        this.scheduleAccessRepository = scheduleAccessRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.courseRepository = courseRepository;
        this.shareLinkRepository = shareLinkRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<ScheduleSummaryResponse> listAccessibleSchedules(Long userId, boolean includeArchived) {
        return scheduleAccessRepository.findByUser_IdOrderByUpdatedAtDesc(userId).stream()
                .filter(access -> includeArchived || !Boolean.TRUE.equals(access.getSchedule().getArchived()))
                .map(this::toScheduleSummary)
                .toList();
    }

    @Transactional
    public IdResponse createSchedule(Long userId, CreateScheduleRequest request) {
        UserEntity user = userService.findById(userId);
        ScheduleEntity schedule = buildScheduleEntity(request, user);
        schedule = scheduleRepository.save(schedule);

        ScheduleAccessEntity access = new ScheduleAccessEntity();
        access.setUser(user);
        access.setSchedule(schedule);
        access.setAccessRole(AccessRole.OWNER);
        access.setAccessSource(AccessSource.OWNER);
        access.setDisplayColor(schedule.getDefaultColor());
        access.setDisplayOpacity(DEFAULT_OPACITY);
        access.setVisibleDefault(true);
        scheduleAccessRepository.save(access);
        return new IdResponse(schedule.getId());
    }

    @Transactional(readOnly = true)
    public ScheduleDetailResponse getScheduleDetail(Long userId, Long scheduleId) {
        ScheduleAccessEntity access = getAccessibleSchedule(userId, scheduleId);
        List<TimeSlotDto> timeSlots = timeSlotRepository.findBySchedule_IdOrderByPeriodIndexAsc(scheduleId).stream()
                .map(this::toTimeSlotDto)
                .toList();
        List<CourseDto> courses = courseRepository.findBySchedule_IdOrderByWeekdayAscStartPeriodAscStartWeekAscIdAsc(scheduleId)
                .stream()
                .map(this::toCourseDto)
                .toList();
        return new ScheduleDetailResponse(toScheduleSummary(access), timeSlots, courses);
    }

    @Transactional
    public void updateSchedule(Long userId, Long scheduleId, UpdateScheduleRequest request) {
        ScheduleAccessEntity access = getOwnerAccess(userId, scheduleId);
        ScheduleEntity schedule = access.getSchedule();
        validateScheduleBounds(scheduleId, request.totalWeeks(), request.maxPeriodsPerDay());
        schedule.setName(request.name());
        schedule.setTermLabel(request.termLabel());
        schedule.setDescription(request.description());
        schedule.setStartDate(request.startDate());
        schedule.setTotalWeeks(request.totalWeeks());
        schedule.setMaxPeriodsPerDay(request.maxPeriodsPerDay());
        schedule.setDefaultColor(defaultColor(request.defaultColor()));
    }

    @Transactional
    public boolean deleteSchedule(Long userId, Long scheduleId) {
        getOwnerAccess(userId, scheduleId);
        scheduleAccessRepository.deleteBySchedule_Id(scheduleId);
        shareLinkRepository.deleteBySchedule_Id(scheduleId);
        courseRepository.deleteBySchedule_Id(scheduleId);
        timeSlotRepository.deleteBySchedule_Id(scheduleId);
        scheduleRepository.deleteById(scheduleId);
        return true;
    }

    @Transactional
    public boolean removeSharedSchedule(Long userId, Long scheduleId) {
        ScheduleAccessEntity access = getAccessibleSchedule(userId, scheduleId);
        if (access.getAccessRole() != AccessRole.VIEWER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "自己的课表不能通过该接口移除");
        }
        scheduleAccessRepository.delete(access);
        return true;
    }

    @Transactional
    public void updateDisplaySettings(Long userId, Long scheduleId, UpdateDisplaySettingsRequest request) {
        ScheduleAccessEntity access = getAccessibleSchedule(userId, scheduleId);
        access.setDisplayColor(defaultColor(request.displayColor()));
        access.setDisplayOpacity(request.displayOpacity());
        access.setVisibleDefault(request.isVisibleDefault());
    }

    @Transactional
    public void updateDisplayName(Long userId, Long scheduleId, UpdateDisplayNameRequest request) {
        ScheduleAccessEntity access = getAccessibleSchedule(userId, scheduleId);
        String value = request.displayNameOverride();
        access.setDisplayNameOverride(value == null || value.isBlank() ? null : value.trim());
    }

    @Transactional
    public void replaceTimeSlots(Long userId, Long scheduleId, ReplaceTimeSlotsRequest request) {
        ScheduleAccessEntity access = getOwnerAccess(userId, scheduleId);
        validateTimeSlots(request.timeSlots(), access.getSchedule().getMaxPeriodsPerDay());
        validateExistingCoursesForTimeSlots(scheduleId, request.timeSlots());
        timeSlotRepository.deleteBySchedule_Id(scheduleId);
        List<ScheduleTimeSlotEntity> entities = request.timeSlots().stream()
                .sorted(Comparator.comparing(TimeSlotInput::periodIndex))
                .map(item -> toTimeSlotEntity(access.getSchedule(), item))
                .toList();
        timeSlotRepository.saveAll(entities);
    }

    @Transactional
    public IdResponse addCourse(Long userId, Long scheduleId, CourseUpsertRequest request) {
        ScheduleAccessEntity access = getOwnerAccess(userId, scheduleId);
        validateCourse(access.getSchedule(), request);
        ScheduleCourseEntity course = toCourseEntity(access.getSchedule(), request);
        course = courseRepository.save(course);
        return new IdResponse(course.getId());
    }

    @Transactional
    public void updateCourse(Long userId, Long scheduleId, Long courseId, CourseUpsertRequest request) {
        ScheduleAccessEntity access = getOwnerAccess(userId, scheduleId);
        validateCourse(access.getSchedule(), request);
        ScheduleCourseEntity course = courseRepository.findByIdAndSchedule_Id(courseId, scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "课程不存在"));
        applyCourse(course, request);
    }

    @Transactional
    public boolean deleteCourse(Long userId, Long scheduleId, Long courseId) {
        getOwnerAccess(userId, scheduleId);
        ScheduleCourseEntity course = courseRepository.findByIdAndSchedule_Id(courseId, scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "课程不存在"));
        courseRepository.delete(course);
        return true;
    }

    @Transactional
    public ScheduleIdResponse importScheduleFromJson(Long userId, ImportScheduleRequest request) {
        IdResponse idResponse = createSchedule(userId, request.schedule());
        Long scheduleId = idResponse.id();
        replaceTimeSlots(userId, scheduleId, new ReplaceTimeSlotsRequest(request.timeSlots()));
        for (CourseUpsertRequest course : request.courses()) {
            addCourse(userId, scheduleId, course);
        }
        return new ScheduleIdResponse(scheduleId);
    }

    @Transactional(readOnly = true)
    public ScheduleAccessEntity getAccessibleSchedule(Long userId, Long scheduleId) {
        return scheduleAccessRepository.findByUser_IdAndSchedule_Id(userId, scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "课程表不存在或无权访问"));
    }

    @Transactional(readOnly = true)
    public ScheduleAccessEntity getOwnerAccess(Long userId, Long scheduleId) {
        ScheduleAccessEntity access = getAccessibleSchedule(userId, scheduleId);
        if (access.getAccessRole() != AccessRole.OWNER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅 OWNER 可执行该操作");
        }
        return access;
    }

    @Transactional(readOnly = true)
    public List<ScheduleTimeSlotEntity> getTimeSlots(Long scheduleId) {
        return timeSlotRepository.findBySchedule_IdOrderByPeriodIndexAsc(scheduleId);
    }

    @Transactional(readOnly = true)
    public List<ScheduleCourseEntity> getCourses(Long scheduleId) {
        return courseRepository.findBySchedule_IdOrderByWeekdayAscStartPeriodAscStartWeekAscIdAsc(scheduleId);
    }

    private ScheduleEntity buildScheduleEntity(CreateScheduleRequest request, UserEntity owner) {
        ScheduleEntity schedule = new ScheduleEntity();
        schedule.setOwnerUser(owner);
        schedule.setName(request.name());
        schedule.setTermLabel(request.termLabel());
        schedule.setDescription(request.description());
        schedule.setStartDate(request.startDate());
        schedule.setTotalWeeks(request.totalWeeks());
        schedule.setMaxPeriodsPerDay(request.maxPeriodsPerDay());
        schedule.setDefaultColor(defaultColor(request.defaultColor()));
        schedule.setArchived(false);
        return schedule;
    }

    private ScheduleSummaryResponse toScheduleSummary(ScheduleAccessEntity access) {
        ScheduleEntity schedule = access.getSchedule();
        UserEntity owner = schedule.getOwnerUser();
        String ownerDisplayName = owner.getDisplayName() == null || owner.getDisplayName().isBlank()
                ? owner.getUsername()
                : owner.getDisplayName();
        return new ScheduleSummaryResponse(
                schedule.getId(),
                schedule.getName(),
                schedule.getTermLabel(),
                schedule.getDescription(),
                schedule.getStartDate(),
                schedule.getTotalWeeks(),
                schedule.getMaxPeriodsPerDay(),
                schedule.getDefaultColor(),
                owner.getId(),
                ownerDisplayName,
                access.getAccessRole().name(),
                access.getAccessSource().name(),
                access.getDisplayNameOverride(),
                access.getDisplayColor(),
                access.getDisplayOpacity(),
                access.getVisibleDefault()
        );
    }

    private TimeSlotDto toTimeSlotDto(ScheduleTimeSlotEntity entity) {
        return new TimeSlotDto(entity.getId(), entity.getPeriodIndex(), entity.getStartTime(), entity.getEndTime());
    }

    private CourseDto toCourseDto(ScheduleCourseEntity entity) {
        return new CourseDto(
                entity.getId(),
                entity.getName(),
                entity.getWeekday(),
                entity.getStartWeek(),
                entity.getEndWeek(),
                entity.getStartPeriod(),
                entity.getEndPeriod(),
                entity.getWeekType().name(),
                entity.getTeacher(),
                entity.getLocation(),
                entity.getNote(),
                entity.getTemporary()
        );
    }

    private ScheduleTimeSlotEntity toTimeSlotEntity(ScheduleEntity schedule, TimeSlotInput input) {
        ScheduleTimeSlotEntity entity = new ScheduleTimeSlotEntity();
        entity.setSchedule(schedule);
        entity.setPeriodIndex(input.periodIndex());
        entity.setStartTime(input.startTime());
        entity.setEndTime(input.endTime());
        return entity;
    }

    private ScheduleCourseEntity toCourseEntity(ScheduleEntity schedule, CourseUpsertRequest request) {
        ScheduleCourseEntity entity = new ScheduleCourseEntity();
        entity.setSchedule(schedule);
        applyCourse(entity, request);
        return entity;
    }

    private void applyCourse(ScheduleCourseEntity entity, CourseUpsertRequest request) {
        entity.setName(request.name());
        entity.setWeekday(request.weekday());
        entity.setStartWeek(request.startWeek());
        entity.setEndWeek(request.endWeek());
        entity.setStartPeriod(request.startPeriod());
        entity.setEndPeriod(request.endPeriod());
        entity.setWeekType(request.weekType() == null ? WeekType.ALL : request.weekType());
        entity.setTeacher(request.teacher());
        entity.setLocation(request.location());
        entity.setNote(request.note());
        entity.setTemporary(Boolean.TRUE.equals(request.isTemporary()));
    }

    private void validateTimeSlots(List<TimeSlotInput> inputs, Integer maxPeriodsPerDay) {
        Set<Integer> periods = new HashSet<>();
        for (TimeSlotInput input : inputs) {
            if (!periods.add(input.periodIndex())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "periodIndex 不能重复");
            }
            if (input.periodIndex() > maxPeriodsPerDay) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "periodIndex 超出课程表最大节次");
            }
            if (!input.startTime().isBefore(input.endTime())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "节次开始时间必须早于结束时间");
            }
        }
    }

    private void validateCourse(ScheduleEntity schedule, CourseUpsertRequest request) {
        if (request.startWeek() > request.endWeek()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "startWeek 不能大于 endWeek");
        }
        if (request.endWeek() > schedule.getTotalWeeks()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "课程周次超出课程表总周数");
        }
        if (request.startPeriod() > request.endPeriod()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "startPeriod 不能大于 endPeriod");
        }
        if (request.endPeriod() > schedule.getMaxPeriodsPerDay()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "课程节次超出课程表最大节次");
        }

        List<ScheduleTimeSlotEntity> timeSlots = getTimeSlots(schedule.getId());
        Set<Integer> definedPeriods = new HashSet<>();
        for (ScheduleTimeSlotEntity slot : timeSlots) {
            definedPeriods.add(slot.getPeriodIndex());
        }
        for (int period = request.startPeriod(); period <= request.endPeriod(); period++) {
            if (!definedPeriods.contains(period)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "课程节次未定义时间: period " + period);
            }
        }
    }

    private void validateScheduleBounds(Long scheduleId, Integer totalWeeks, Integer maxPeriodsPerDay) {
        for (ScheduleCourseEntity course : getCourses(scheduleId)) {
            if (course.getEndWeek() > totalWeeks) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "已有课程周次超出新的 totalWeeks");
            }
            if (course.getEndPeriod() > maxPeriodsPerDay) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "已有课程节次超出新的 maxPeriodsPerDay");
            }
        }
        for (ScheduleTimeSlotEntity slot : getTimeSlots(scheduleId)) {
            if (slot.getPeriodIndex() > maxPeriodsPerDay) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "已有节次定义超出新的 maxPeriodsPerDay");
            }
        }
    }

    private void validateExistingCoursesForTimeSlots(Long scheduleId, List<TimeSlotInput> timeSlots) {
        Set<Integer> periods = new HashSet<>();
        for (TimeSlotInput slot : timeSlots) {
            periods.add(slot.periodIndex());
        }
        for (ScheduleCourseEntity course : getCourses(scheduleId)) {
            for (int period = course.getStartPeriod(); period <= course.getEndPeriod(); period++) {
                if (!periods.contains(period)) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST, "新节次定义会导致已有课程缺少时间范围");
                }
            }
        }
    }

    private String defaultColor(String color) {
        return color == null || color.isBlank() ? DEFAULT_COLOR : color;
    }
}
