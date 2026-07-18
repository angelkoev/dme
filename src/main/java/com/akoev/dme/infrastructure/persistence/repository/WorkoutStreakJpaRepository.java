package com.akoev.dme.infrastructure.persistence.repository;

import com.akoev.dme.infrastructure.persistence.entity.WorkoutStreakEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutStreakJpaRepository extends JpaRepository<WorkoutStreakEntity, Long> {
}
