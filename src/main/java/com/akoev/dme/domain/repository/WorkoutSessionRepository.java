package com.akoev.dme.domain.repository;

import com.akoev.dme.domain.model.WorkoutSession;

import java.util.Optional;

public interface WorkoutSessionRepository {

    Optional<WorkoutSession> findById(Long id);
}
