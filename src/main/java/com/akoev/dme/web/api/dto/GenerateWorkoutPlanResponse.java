package com.akoev.dme.web.api.dto;

import com.akoev.dme.application.service.GenerationResult;

public record GenerateWorkoutPlanResponse(WorkoutPlanResponse plan, String explanation, String motivationalMessage) {

    public static GenerateWorkoutPlanResponse from(GenerationResult result) {
        return new GenerateWorkoutPlanResponse(
                WorkoutPlanResponse.from(result.plan()),
                result.explanation(),
                result.motivationalMessage()
        );
    }
}
