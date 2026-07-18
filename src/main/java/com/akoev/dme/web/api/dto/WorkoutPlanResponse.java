package com.akoev.dme.web.api.dto;

import com.akoev.dme.domain.model.GenerationSource;
import com.akoev.dme.domain.model.TrainingGoal;
import com.akoev.dme.domain.model.WorkoutPlan;

import java.time.Instant;
import java.util.List;

public record WorkoutPlanResponse(
        Long id,
        TrainingGoal goal,
        Instant generatedAt,
        boolean active,
        GenerationSource generationSource,
        List<WorkoutSessionResponse> sessions
) {

    public static WorkoutPlanResponse from(WorkoutPlan plan) {
        return new WorkoutPlanResponse(
                plan.getId(),
                plan.getGoal(),
                plan.getGeneratedAt(),
                plan.isActive(),
                plan.getGenerationSource(),
                plan.getSessions().stream().map(WorkoutSessionResponse::from).toList()
        );
    }
}
