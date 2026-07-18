package com.akoev.dme.domain.repository;

import com.akoev.dme.domain.model.WorkoutLog;

import java.time.Instant;
import java.util.List;

public interface WorkoutLogRepository {

    WorkoutLog save(WorkoutLog log);

    List<WorkoutLog> findRecentByUserId(Long userId, Instant since);

    List<WorkoutLog> findAllByUserId(Long userId);
}
