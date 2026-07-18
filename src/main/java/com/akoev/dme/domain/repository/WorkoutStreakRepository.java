package com.akoev.dme.domain.repository;

import com.akoev.dme.domain.model.WorkoutStreak;

import java.util.Optional;

public interface WorkoutStreakRepository {

    Optional<WorkoutStreak> findByUserId(Long userId);

    WorkoutStreak save(WorkoutStreak streak);
}
