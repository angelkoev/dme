package com.akoev.dme.web.api.dto;

import com.akoev.dme.domain.model.WorkoutSession;

import java.util.List;

public record WorkoutSessionResponse(Long id, int sessionIndex, String name, List<SessionExerciseResponse> exercises) {

    public static WorkoutSessionResponse from(WorkoutSession session) {
        return new WorkoutSessionResponse(
                session.getId(),
                session.getSessionIndex(),
                session.getName(),
                session.getExercises().stream().map(SessionExerciseResponse::from).toList()
        );
    }
}
