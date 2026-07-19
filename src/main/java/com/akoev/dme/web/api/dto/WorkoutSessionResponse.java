package com.akoev.dme.web.api.dto;

import com.akoev.dme.domain.model.WorkoutSession;

import java.time.DayOfWeek;
import java.util.List;

public record WorkoutSessionResponse(Long id, int sessionIndex, String name, DayOfWeek dayOfWeek,
                                      int estimatedDurationMinutes, List<SessionExerciseResponse> exercises) {

    public static WorkoutSessionResponse from(WorkoutSession session) {
        return new WorkoutSessionResponse(
                session.getId(),
                session.getSessionIndex(),
                session.getName(),
                session.getDayOfWeek(),
                session.getEstimatedDurationMinutes(),
                session.getExercises().stream().map(SessionExerciseResponse::from).toList()
        );
    }
}
