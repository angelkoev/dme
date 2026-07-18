package com.akoev.dme.web.api.dto;

import com.akoev.dme.domain.model.TrainingGoal;

public record GenerateWorkoutPlanRequest(TrainingGoal goalOverride) {
}
