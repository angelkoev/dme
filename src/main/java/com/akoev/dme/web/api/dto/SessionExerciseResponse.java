package com.akoev.dme.web.api.dto;

import com.akoev.dme.domain.model.SessionExercise;

public record SessionExerciseResponse(
        Long id,
        ExerciseResponse exercise,
        int orderIndex,
        int sets,
        int repRangeMin,
        int repRangeMax,
        int restSeconds,
        String notes
) {

    public static SessionExerciseResponse from(SessionExercise sessionExercise) {
        return new SessionExerciseResponse(
                sessionExercise.getId(),
                ExerciseResponse.from(sessionExercise.getExercise()),
                sessionExercise.getOrderIndex(),
                sessionExercise.getSets(),
                sessionExercise.getRepRangeMin(),
                sessionExercise.getRepRangeMax(),
                sessionExercise.getRestSeconds(),
                sessionExercise.getNotes()
        );
    }
}
