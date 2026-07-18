package com.akoev.dme.domain.repository;

import com.akoev.dme.domain.model.WorkoutPlan;

import java.util.List;
import java.util.Optional;

public interface WorkoutPlanRepository {

    WorkoutPlan save(WorkoutPlan plan);

    Optional<WorkoutPlan> findById(Long id);

    List<WorkoutPlan> findAllByUserId(Long userId);

    Optional<WorkoutPlan> findActiveByUserId(Long userId);
}
