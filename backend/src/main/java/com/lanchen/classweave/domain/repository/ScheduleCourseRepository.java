package com.lanchen.classweave.domain.repository;

import com.lanchen.classweave.domain.entity.ScheduleCourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScheduleCourseRepository extends JpaRepository<ScheduleCourseEntity, Long> {

    List<ScheduleCourseEntity> findBySchedule_IdOrderByWeekdayAscStartPeriodAscStartWeekAscIdAsc(Long scheduleId);

    Optional<ScheduleCourseEntity> findByIdAndSchedule_Id(Long id, Long scheduleId);

    void deleteBySchedule_Id(Long scheduleId);
}
