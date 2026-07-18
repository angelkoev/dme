package com.akoev.dme.infrastructure.persistence.repository;

import com.akoev.dme.infrastructure.persistence.entity.WorkoutLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface WorkoutLogJpaRepository extends JpaRepository<WorkoutLogEntity, Long> {

    List<WorkoutLogEntity> findAllByUser_IdAndPerformedAtAfterOrderByPerformedAtDesc(Long userId, Instant since);

    List<WorkoutLogEntity> findAllByUser_IdOrderByPerformedAtDesc(Long userId);
}
