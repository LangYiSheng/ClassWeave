package com.lanchen.classweave.domain.repository;

import com.lanchen.classweave.domain.entity.ScheduleAccessEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScheduleAccessRepository extends JpaRepository<ScheduleAccessEntity, Long> {

    @EntityGraph(attributePaths = {"user", "schedule", "schedule.ownerUser", "shareLink"})
    List<ScheduleAccessEntity> findByUser_IdOrderByUpdatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"user", "schedule", "schedule.ownerUser", "shareLink"})
    Optional<ScheduleAccessEntity> findByUser_IdAndSchedule_Id(Long userId, Long scheduleId);

    boolean existsByUser_IdAndSchedule_Id(Long userId, Long scheduleId);

    void deleteBySchedule_Id(Long scheduleId);
}
