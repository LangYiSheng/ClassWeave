package com.lanchen.classweave.domain.repository;

import com.lanchen.classweave.domain.entity.ScheduleTimeSlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleTimeSlotRepository extends JpaRepository<ScheduleTimeSlotEntity, Long> {

    List<ScheduleTimeSlotEntity> findBySchedule_IdOrderByPeriodIndexAsc(Long scheduleId);

    void deleteBySchedule_Id(Long scheduleId);
}
