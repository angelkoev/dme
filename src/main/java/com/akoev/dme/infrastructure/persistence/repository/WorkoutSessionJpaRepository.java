package com.akoev.dme.infrastructure.persistence.repository;

import com.akoev.dme.infrastructure.persistence.entity.WorkoutSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutSessionJpaRepository extends JpaRepository<WorkoutSessionEntity, Long> {
}
