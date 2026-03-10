package com.lanchen.classweave.domain.repository;

import com.lanchen.classweave.domain.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {

    @Override
    @EntityGraph(attributePaths = "ownerUser")
    Optional<ScheduleEntity> findById(Long id);
}
