package com.akoev.dme.infrastructure.persistence.repository;

import com.akoev.dme.infrastructure.persistence.entity.WorkoutPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkoutPlanJpaRepository extends JpaRepository<WorkoutPlanEntity, Long> {

    List<WorkoutPlanEntity> findAllByUser_Id(Long userId);

    Optional<WorkoutPlanEntity> findFirstByUser_IdAndActiveTrue(Long userId);
}
