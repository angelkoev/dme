package com.akoev.dme.web.api.dto;

import com.akoev.dme.domain.model.WorkoutLog;

import java.time.Instant;

public record WorkoutLogResponse(
        Long id,
        Instant performedAt,
        int completionPercentage,
        Integer rating,
        Integer perceivedIntensity,
        String notes
) {

    public static WorkoutLogResponse from(WorkoutLog log) {
        return new WorkoutLogResponse(log.getId(), log.getPerformedAt(), log.getCompletionPercentage(),
                log.getRating(), log.getPerceivedIntensity(), log.getNotes());
    }
}
