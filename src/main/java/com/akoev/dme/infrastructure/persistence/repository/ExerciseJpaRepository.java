package com.akoev.dme.infrastructure.persistence.repository;

import com.akoev.dme.infrastructure.persistence.entity.ExerciseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseJpaRepository extends JpaRepository<ExerciseEntity, Long> {
}
