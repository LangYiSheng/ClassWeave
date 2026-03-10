package com.lanchen.classweave.domain.repository;

import com.lanchen.classweave.domain.entity.ScheduleShareLinkEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScheduleShareLinkRepository extends JpaRepository<ScheduleShareLinkEntity, Long> {

    @EntityGraph(attributePaths = {"schedule", "schedule.ownerUser", "createdByUser"})
    List<ScheduleShareLinkEntity> findBySchedule_IdOrderByCreatedAtDesc(Long scheduleId);

    @EntityGraph(attributePaths = {"schedule", "schedule.ownerUser", "createdByUser"})
    Optional<ScheduleShareLinkEntity> findByShareToken(String shareToken);

    @Override
    @EntityGraph(attributePaths = {"schedule", "schedule.ownerUser", "createdByUser"})
    Optional<ScheduleShareLinkEntity> findById(Long id);

    void deleteBySchedule_Id(Long scheduleId);
}
